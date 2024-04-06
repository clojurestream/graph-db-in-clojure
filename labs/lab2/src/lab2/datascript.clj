(ns lab2.datascript
  (:require [datascript.core :as d]
            [clojure.edn :as edn]))


(def starwars-schema {:interacts-with {:db/cardinality :db.cardinality/many}
                      :end {:db/cardinality :db.cardinality/many}})

(defn create-db []
  (d/create-conn starwars-schema))

(defn triples->datoms
  [triples]
  (let [idents (set (map first triples))
        ids (zipmap idents (range -1 (- (inc (count idents))) -1))
        ident-dec (map (fn [[ident id]] [:db/add id :db/ident ident]) ids)
        data (map (fn [[e a v]] [:db/add e a v]) triples)]
    (concat ident-dec data)))

(defn insert-triples
  [conn triples]
  (d/transact conn (triples->datoms triples)))

(defn read-triples
  [filename]
  (edn/read-string (slurp filename)))


(comment
  (def triples (ds/read-triples "../../graphs/starwars.edn"))
  (def conn (ds/insert-triples (ds/create-db) triples))
  (d/q '[:find ?name :where [:yoda :name ?name]] @conn)
  (d/q '[:find ?character ?name :where [?character :name ?name]] @conn)
  (d/q '[:find ?character ?name :where [?character :color "#000000"][?character :name ?name]] @conn)
)
