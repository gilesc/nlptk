(ns nlptk
  (:use
   clojure.contrib.def)
  (:import
   (com.aliasi.dict ExactDictionaryChunker MapDictionary DictionaryEntry)
   (com.aliasi.tokenizer IndoEuropeanTokenizerFactory)
   (com.aliasi.chunk ChunkingImpl)))

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
