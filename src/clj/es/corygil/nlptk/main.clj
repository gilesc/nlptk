(ns es.corygil.nlptk.main
  (:gen-class)
  (:require
   [es.corygil.nlptk corpus split]
   [clojure.contrib.string :as string]))

(defn help []
  (println "TODO: HELP"))

(defn -main [& args]
  (try
    (let [command (-> (str "es.corygil.nlptk." (first args) "/-main") symbol resolve)]
      (apply command (rest args)))
    (catch Exception e
      (prn e)
        (do (println "Command not recognized.")
            (help)))))
