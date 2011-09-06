(ns es.corygil.nlptk.parser
  (:require
   [es.corygil.nlptk.sexp :as sexp])
  (:use
   [clojure.contrib.probabilities.random-numbers :only [rand-stream]]
   [clojure.contrib.probabilities.finite-distributions :only [normalize-cond]]
   clojure.contrib.def))

(defn sample-distribution [n pdist]
  (take n
        (seq (random-stream pdist rand-stream))))

(defn terminal? [x]
  (and
   (string? (first x))
   (= (count x) 1)))

(defn merge-cdists [& cdists]
  (reduce (fn [acc m] (merge-with #(merge-with + %1 %2) acc m))
          {}
          cdists))

(defnk rules [tree :lex? false]
  (let [[head & tail] tree]
    (if (terminal? tail)
      (if lex?
        [head tail])
      (conj
       (apply concat (map #(rules % :lex? lex?) tail))
       [head (map first tail)]))))

(defnk pcfg [tree :lex? false]
  (into {}
   (for [[head patterns] (group-by first (rules tree))]
     [head (frequencies (map second patterns))])))

(defn train! [treebank-dir]
  (def *pcfg*
    (normalize-cond
     (apply merge-cdists
             (for [file (take 5
                              (.listFiles (java.io.File. treebank-dir)))
                   tree (map first (sexp/parse (slurp file)))]
               (pcfg tree))))))


(defn generate [element]
  (let [result (rand-nth (keys (*pcfg* element)))]
    (if (string? result)
      result
      (map generate result))))

(defn generate-sentence []
  (generate 'S))

(defn tag [words])

(defn parse [words])

(defn -main []
  (train! "data/treebank/combined")
  (prn (generate-sentence)))
