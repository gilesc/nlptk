(ns es.corygil.nlptk.corpus.sord
  (:require
   [clojure.contrib.io :as io]
   [clojure.contrib.seq :as seq]
   [clojure.contrib.json :as json]))

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
     (zipmap [:synonyms-cs :synonyms]
             (map #(map :Objectsynonym %)
                    (seq/separate :CAPS_flag records))))))

(defn -main [sord-file]
  (doseq [record (read-sord-terms sord-file)]
    (println (json/json-str record))))
