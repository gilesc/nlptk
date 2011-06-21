(ns es.corygil.nlptk.corpus
  (:require
   [clojure.contrib.io :as io]
   [clojure.contrib.seq :as seq]))

(defn- to-input-strm [file]
  (if (.endsWith (str file) "gz")
    (java.util.zip.GZIPInputStream.
     (io/input-stream file))
    (io/input-stream file)))

(defn parse-medline-xml [file]
  (for [citation (es.corygil.nlptk.corpus.medline.MedlineParser/parse
                  (to-input-strm file))]
    {:pmid (.pmid citation)
     :title (.title citation)
     :abstract (.abstrct citation)}))

(defn read-sord-terms [sord-file]
  "Read Named Entities from SORD (streamlined object relational database)"
  (for [[id records] (group-by :RecordID
                               (map #(zipmap (map keyword (keys %)) (vals %))
                                    (iterator-seq
                                     (.iterator
                                      (.getTable
                                       (com.healthmarketscience.jackcess.Database/open
                                        (io/file sord-file))
                                       "tblObjectSynonyms")))))]
    (merge
     {:id id :name (:Objectname (first records))
      :ontology (ffirst
                 (reverse
                  (sort-by second
                           (frequencies (map :ObjectType records)))))
      :sources (set (map :SourceID records))
      :birth-date (apply min (map :Birth_Date records))
      :frequency (apply max (map :Occurances records))}
     (zipmap [:synonyms-ci :synonyms]
             (map #(map :Objectsynonym %)
                    (seq/separate :CAPS_flag records))))))
