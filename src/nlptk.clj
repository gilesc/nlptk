(ns nlptk
  (:gen-class)
  (:use
   clojure.contrib.def)
  (:require
   [clojure.contrib.str-utils :as str-utils])
  (:import
   (com.aliasi.dict ExactDictionaryChunker MapDictionary DictionaryEntry)
   (com.aliasi.tokenizer IndoEuropeanTokenizerFactory)
   (com.aliasi.chunk ChunkingImpl)
   aiiaadi.util.Utility
   edu.stanford.nlp.parser.lexparser.LexicalizedParser))

(defn- clojurify-chunking [chunking]
  (map #(hash-map :id (try (Integer/parseInt (.type %))
                           (catch Exception _ (.type %)))
                  :start (.start %)
                  :end (.end %))
       chunking))

(defnk make-chunker [term-ids :case-sensitive false :allow-overlaps false]
  "Given a sequence of id-term pairs, returns a function that
   chunks input text for all matches of terms contained in the chunker
   using the Aho-Corasick algorithm."
  
  (let [md (MapDictionary.)
        tf (IndoEuropeanTokenizerFactory/INSTANCE)]
    (doseq [[term id] term-ids]
      (.addEntry md
                 (DictionaryEntry. term (str id))))
    (let [chunker (ExactDictionaryChunker. md tf allow-overlaps case-sensitive)]
      (with-meta
        (fn [text]
          (clojurify-chunking
           (.chunk chunker text)))
        {:chunkers [chunker]}))))

(defn combine-chunkers [& chunkers]
  "Combines multiple chunkers into one."
  (let [chunker-objs (apply concat (map #(:chunkers (meta %)) chunkers))]
    (with-meta
     (fn [text]
       (clojurify-chunking 
        (reduce (fn [chunking chunker]
                  (ChunkingImpl/merge chunking
                                      (.chunk chunker text)))
                (.chunk (first chunker-objs) text)
                (rest chunker-objs))))
     {:chunkers chunker-objs})))

 
(defn find-acronyms [text]
  "Finds acronyms within text (whose long forms are also included)
  and returns a map of {Short-forms Long-forms}"
  (for [acro (Utility/performTest "" text)]
    (let [[sf lf _] (str-utils/re-split #"[|]" acro)]
      {sf lf})))

(defn replace-acronyms [text]
  "Finds all acronyms within the text and replaces them with expansions."
  (reduce
   #(str-utils/re-gsub (re-pattern (ffirst %2)) (second (first %2)) %1)
   text (find-acronyms text)))

(defn load-parser [path-to-serialized-parser]
  (let [parser (LexicalizedParser.
                (java.io.ObjectInputStream.
                 (java.util.zip.GZIPInputStream.
                  path-to-serialized-parser)))]
    #(.apply parser %)))





