(defproject es.corygil/nlptk "1.0.0-SNAPSHOT"
  :description "NLP utilities (NER, parsers, corpus readers)"
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [com.aliasi/lingpipe "4.0.1"]
                 [org.clojars.gilesc/stanford-parser "1.6.7"]
                 [edu.sinica.bioagent/bioadi "0.1.0"]
                 [com.healthmarketscience/jackcess "1.2.4"]]
  :source-path "src/clj"
  :java-source-path "src/java")
