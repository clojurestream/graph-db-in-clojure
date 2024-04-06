(ns lab1.json
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [lab1.util :refer [keyname node-id capitalize-name]]))

(defn read-json
  [filename]
  ;; TODO read a JSON file, with keyword keys
  )

(defn link->triples
  [nodes link-index {:keys [source target value] :as link}]
  ;; TODO convert a link to a seq of triples
  ;; the nodes are provided since links connect nodes
  )

(defn node->triples
  [node-index {:keys [name value colour] :as node}]
  ;; TODO convert a node to a seq of triples
  )

(defn to-triples
  [{:keys [nodes links]}]
  (let [link-function #(link->triples nodes %)]
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

