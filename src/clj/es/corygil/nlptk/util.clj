(ns es.corygil.nlptk.util)

(defn stdin []
  (line-seq
   (java.io.BufferedReader. *in*)))
