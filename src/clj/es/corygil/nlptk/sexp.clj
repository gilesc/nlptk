(ns es.corygil.nlptk.sexp)

(defn tokenize [s]
  (re-seq
   #"\S+"
   (.replace
    (.replace s "(" " ( ")
    ")" " ) ")))

(defn parse-tokens [tokens]
  "Simple shift-reduce sexp parser"
  (loop [stack [] tokens tokens]
    (if (empty? tokens)
      stack
      (let [t (first tokens)]
        (recur (if (= t ")")
                 (let [[pre post] (split-with #(not= % "(") stack)]
                   (concat (list (reverse pre)) (rest post)))
                 (cons t stack))
               (rest tokens))))))

(defn parse [s]
  (parse-tokens (tokenize s)))
