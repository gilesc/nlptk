(ns es.corygil.nlptk.corpus
  (:require
   [clojure.contrib.io :as io]))

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


