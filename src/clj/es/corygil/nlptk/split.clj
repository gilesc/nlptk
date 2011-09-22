(ns es.corygil.nlptk.split
  (:use
   es.corygil.nlptk.util)
  (:import
   (com.aliasi.tokenizer IndoEuropeanTokenizerFactory)
   (com.aliasi.sentences SentenceChunker MedlineSentenceModel)))

(let [sc (SentenceChunker.
          (IndoEuropeanTokenizerFactory/INSTANCE)
          (MedlineSentenceModel/INSTANCE))]
  (defn split-sentences [text]
    (map #(subs text (.start %) (.end %))
         (.chunkSet (.chunk sc text)))))

(defn -main []
  (doseq [line (stdin)
          sentence (split-sentences line)]
    (println sentence)))
