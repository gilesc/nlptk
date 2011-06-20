(ns es.corygil.nlptk.parser
  (:use
   clojure.contrib.def))

(defn sample-pdist [pdist] ;TODO:
  (vals pdist))

(defn replace-commas [tree]
  (for [child tree]
    (if (list? child)
      (if (empty? child)
        '(COMMA COMMA)
        (replace-commas child))
      child)))

(defn read-parse [treebank-file]
  (prn treebank-file)
  (map #(replace-commas (first %))
   (loop [result nil
          reader (java.io.PushbackReader.
                  (java.io.StringReader.
                   (.replaceAll
                    (.replaceAll
                     (.replaceAll
                      (slurp treebank-file)
                      "\\\\+" "&")
                     "[0-9]" "&")
                    "[\\@#^:`*%$'*;]" "&")))] ;;TODO: fix reader
     (if-let [form (read reader false nil)]
       (recur (cons form result) reader)
       result))))

(defnk rules [tree :lex? false]
  (let [head (first tree)]
    (if (symbol? (second tree))
      (if lex?
        (vector head (.toLowerCase (str (second tree)))))
      (conj
       (map #(rules % :lex? lex?) (rest tree))
       [head (map first (rest tree))]))))

(defnk make-pcfg [treebank-dir :lex? false]
  (into {}
   (for [[head patterns]
         (group-by first
                   (filter vector?
                           (tree-seq seq? seq
                                     (for [file (take 5
                                                      (.listFiles (java.io.File. treebank-dir)))]
                                       (map #(rules % :lex? lex?) (read-parse file))))))
         :let [freqs (frequencies (map fnext patterns))
               n (count patterns)]]
     [head (zipmap (keys freqs) (map #(/ % n) (vals freqs)))])))

(defn train! [treebank-dir]
  (def *pcfg*
   (make-pcfg treebank-dir :lex? true)))

(defn generate [element]
  (let [result (rand-nth (keys (*pcfg* element)))]
    (prn result)
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
