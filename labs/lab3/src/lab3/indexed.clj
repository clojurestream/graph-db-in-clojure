(ns lab3.indexed
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn add-to-index
  [index [_ _ c :as triple]]
  (assoc-in index triple c))

(defprotocol Index
  (add [this triple])
  (match [this pattern]))

(defrecord IndexedGraph [spo pos osp]
  Index
  (add [this [s p o :as triple]]
    (IndexedGraph. (add-to-index spo triple)
                   (add-to-index pos [p o s])
                   (add-to-index osp [o s p]))))

(defn load-data
  [filename]
  (let [data (edn/read-string (slurp filename))]
    (reduce add (IndexedGraph. {} {} {}) data)))


(comment
  ;; Add statements to an SPO index
  (def i (assoc-in {} [:yoda :interacts-with :luke] :luke))
  (assoc-in i [:yoda :interacts-with :obi-wan] :obi-wan)

  ;; Add statements to a full index
  (def fi0 (IndexedGraph. {} {} {}))
  (def fi1 (add fi0 [:yoda :interacts-with :luke]))
  (def fi2 (add fi1 [:yoda :interacts-with :obi-wan]))

  ;; Load the starwars graph
  (def data (load-data "../../graphs/starwars.edn"))
)
