(ns lab3.match
  (:require [clojure.edn :as edn]))

(defn add-to-index
  [index triple]
  (assoc-in index triple triple))

(defprotocol Index
  (add [this triple])
  (match [this pattern])
  (bind [this pattern]))

(defrecord IndexedGraph [spo pos osp]
  (add [this [s p o :as triple]]
    (IndexedGraph. (add-to-index spo triple)
                   (add-to-index pos [p o s])
                   (add-to-index osp [o s p])))
  )

(defn load-data
  [filename]
  (let [data (edn/read-string (slurp filename))]
    (reduce add (IndexedGraph. {} {} {}) data)))

(defn variable?
  [x]
  (and (symbol? x) (= \? (first (name x)))))

;; TODO

(defn match
  [data pattern]
  )

(defn bind
  [data pattern]
  )

(comment
  (def data (load-data "../../graphs/starwars.edn"))
  (match data '[?yoda :name "Yoda"])
  (match data '[?character :color "#000000"])
  (match data '[:yoda :interacts-with ?character])
  (bind data '[?yoda :name "Yoda"])
  (bind data '[?character :color "#000000"])
  (bind data '[:yoda :interacts-with ?character])
)
