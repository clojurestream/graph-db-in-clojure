(ns lab3.flat
  (:require [clojure.edn :as edn]))

(defn load-data
  [filename]
  (edn/read-string (slurp filename)))


(comment
  (def data (load-data "../../graphs/starwars.edn"))
)
