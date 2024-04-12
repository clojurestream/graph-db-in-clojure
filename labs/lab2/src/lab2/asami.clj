(ns lab2.asami
  (:require [asami.core :as d]
            [clojure.edn :as edn]))


(defn create-db []
  (d/connect "asami:mem://dbname"))

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
  (def triples (read-triples "../../graphs/starwars.edn")) 
  (def conn (create-db)) 
  (def tx (insert-triples conn triples)) 
  (d/q '[:find ?name :where [:yoda :name ?name]] conn) 
  (d/q '[:find ?character ?name :where [?character :name ?name]] conn) 
  (d/q '[:find ?character ?name :where [?character :color "#000000"][?character :name ?name]] conn) 
  (d/q '[:find ?character :name ?name :where [?character :name ?name]] conn) 

  (def c2 (create-db)) 
  (def tx (d/transact c2 {:tx-triples triples})) 
  (d/q '[:find ?character ?name :where [?character :color "#000000"][?character :name ?name]] c2)  
)

