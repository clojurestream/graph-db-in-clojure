(ns lab1.json
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [lab1.util :refer [keyname node-id capitalize-name]]))

(defn read-json
  [filename]
  (json/read-str (slurp filename) :key-fn keyword))

(defn link->triples
  [nodes link-index {:keys [source target value] :as link}]
  (let [source-id (node-id (get nodes source))
        target-id (node-id (get nodes target))]
    [[source-id :interacts-with target-id] 
     [target-id :interacts-with source-id]]))

(defn node->triples
  [node-index {:keys [name value colour] :as node}]
  (let [id (node-id node)]
    [[id :name (capitalize-name name)]
     [id :value value]
     [id :color colour]]))

(defn to-triples
  [{:keys [nodes links]}]
  (let [link-function #(link->triples nodes %1 %2)]
    (-> []
        (into (apply concat (map-indexed link-function links)))
        (into (apply concat (map-indexed node->triples nodes))))))

(defn write-triples
  [filename triples]
  (with-open [writer (io/writer filename)]
    (binding [*out* writer]
      (prn triples))))

(defn convert-json
  [filename]
  (->> filename
       read-json
       to-triples
       (write-triples (str/replace filename #"\.json$" ".edn"))))

(comment
  (convert-json "../../data/starwars-social/starwars-full-interactions-allCharacters-merged.json")
)
