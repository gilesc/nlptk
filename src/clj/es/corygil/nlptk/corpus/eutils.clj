(ns es.corygil.nlptk.corpus.eutils
  (:use
   clojure.contrib.def
   [clojure.xml :only [parse]]
   [clojure.zip :only [xml-zip node]]
   [clojure.java.io :only [input-stream]])
  (:require
   [clojure.contrib.zip-filter.xml :as zf]
   [clojure.contrib.string :as string]))


(defn- ncbi-query [method & kwargs]
  (input-stream
   (format "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/%s.fcgi?db=pubmed&%s"
           (name method)
           (string/join "&"
                        (for [[k v] (partition 2 kwargs)]
                          (str (name k) "=" v))))))

(defnk esearch [query :n 20]
  (map #(Integer/parseInt %)
   (zf/xml->
    (xml-zip (parse (ncbi-query :esearch :term query :retmax n)))
    :IdList :Id zf/text)))

(defn efetch [& ids]
  {:pre [(<= (count ids) 50)]}
  (let [zipper (xml-zip (parse (ncbi-query :efetch :id (string/join "," ids) :retmode "xml")))]
    (for [tree (map xml-zip (zf/xml-> zipper :PubmedArticle :MedlineCitation node))]
      {:id (Integer/parseInt (first (zf/xml-> tree :PMID zf/text)))
       :title (first (zf/xml-> tree :Article :ArticleTitle zf/text))
       :abstract (first (zf/xml-> tree :Article :Abstract :AbstractText zf/text))})))
