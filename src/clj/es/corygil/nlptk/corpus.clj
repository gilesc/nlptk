(ns es.corygil.nlptk.corpus
  (:use
   es.corygil.nlptk.util)
  (:require
   [clojure.contrib.io :as io]
   [clojure.zip :as zip]
   [clojure.xml :as xml]
   [clojure.contrib.zip-filter.xml :as zf]
   [clojure.contrib.string :as string]))

(defn- to-input-strm [file]
  (if (.endsWith (str file) "gz")
    (java.util.zip.GZIPInputStream.
     (io/input-stream file))
    (io/input-stream file)))

(defprotocol DocumentP
  (id [this])
  (text [this]))

(defrecord PubmedDocument [pmid title abstract]
  DocumentP
  (id [this] pmid)
  (text [this] (str title abstract)))

(defrecord PMCDocument [pmcid text]
  DocumentP
  (id [this] pmcid)
  (text [this] text))


(def read-corpus nil)

(defmulti read-corpus
  (fn [type & _]
    (keyword type)))

(defmethod read-corpus :pubmed [_ file]
  (for [citation (es.corygil.nlptk.corpus.MedlineParser/parse
                  (to-input-strm file))]
    (PubmedDocument. (.pmid citation)
                     (.title citation)
                     (.abstrct citation))))


(defmethod read-corpus :pmc [_ file]
  (let [article
        (-> (java.io.ByteArrayInputStream. (.getBytes
                                    (->> (io/read-lines file)
                                         (drop-while #(not (.startsWith % "<article")))
                                         (string/join "\n"))))
            xml/parse
            zip/xml-zip)]
    [(PMCDocument. (str "PMC" (first (zf/xml-> article :id zf/text)))
                   (string/join " "
                                (zf/xml-> article :body :sec :p zf/text)))]))

(defn -main [type & file-args]
  (let [files (if (seq file-args)
                file-args
                (stdin))]
    (doseq [file files
            document (read-corpus type file)]
      (println (text document)))))

