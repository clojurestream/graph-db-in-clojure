(ns lab5.indexed
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn variable? [x] (and (symbol? x) (= \? (first (name x)))))

(def ? '?)
(def x 'x)

(defn normalize [_ pattern] (mapv #(if (variable? %) ? x) pattern))

(defmulti get-from-index normalize)
(defmethod get-from-index [x x x] [{idx :spo} [s p o]] (let [os (get-in idx [s p])] (if (get os o) [[]] [])))
(defmethod get-from-index [x x ?] [{idx :spo} [s p o]] (map vector (keys (get-in idx [s p]))))
(defmethod get-from-index [x ? x] [{idx :osp} [s p o]] (map vector (keys (get-in idx [o s]))))
(defmethod get-from-index [x ? ?] [{idx :spo} [s p o]] (let [edx (idx s)] (for [[p om] edx o (keys om)] [p o])))
(defmethod get-from-index [? x x] [{idx :pos} [s p o]] (map vector (keys (get-in idx [p o]))))
(defmethod get-from-index [? x ?] [{idx :pos} [s p o]] (let [edx (idx p)] (for [[o sm] edx s (keys sm)] [s o])))
(defmethod get-from-index [? ? x] [{idx :osp} [s p o]] (let [edx (idx o)] (for [[s pm] edx p (keys pm)] [s p])))
(defmethod get-from-index [? ? ?] [{idx :spo} [s p o]] (for [[s pom] idx [p om] pom o (keys om)] [s p o]))

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
                   (add-to-index osp [o s p])))
  (match [this pattern]
    {:vars (vec (filter variable? pattern))
     :bindings (get-from-index this pattern)}))

(defn load-data
  [filename]
  (let [data (edn/read-string (slurp filename))]
    (reduce add (IndexedGraph. {} {} {}) data)))

(defn export-data
  [filename data]
  (let [triples (:bindings (match data '[?s ?p ?o]))]
    (spit filename (pr-str (vec triples)))))

(defn do-filter
  [match-result filter-expr]
  (let [vars (:vars match-result)
        data (:bindings match-result)
        fltr (first filter-expr)
        filter-fn (eval `(fn [~vars] ~fltr))]
    {:vars vars
     :bindings (filter filter-fn data)}))

(defn rewrite-pattern
  [vars binding-data pattern]
  (let [binding-map (zipmap vars binding-data)]
    (mapv #(if (variable? %) (get binding-map % %) %) pattern)))

(defn join
  [graph part-result pattern]
  (let [vars (:vars part-result)
        new-vars (->> pattern
                      (filter variable?)
                      (remove (set vars)))
        result (for [binding (:bindings part-result)
                     new-values (:bindings (match graph (rewrite-pattern vars binding pattern)))]
                 (vec (concat binding new-values)))]
    {:vars (vec (concat vars new-vars))
     :bindings result}))

(comment
  ;; Load the starwars graph
  (def data (load-data "../../graphs/starwars.edn")) 

  ;; join [?character :color "#000000"] [?character :name ?name]
  (def color-pattern '[?character :color "#000000"]) 
  (def name-pattern '[?character :name ?name]) 

  (def color-result (match data color-pattern)) 
  (def vars (:vars color-result)) 

  (for [binding (:bindings color-result)]
    binding) 

  (for [binding (:bindings color-result)]
    (rewrite-pattern vars binding '[?character :name ?name])) 

  (for [binding (:bindings color-result)]
    (match data (rewrite-pattern vars binding '[?character :name ?name]))) 

  (for [binding (:bindings color-result)
        new-values (:bindings (match data (rewrite-pattern vars binding '[?character :name ?name])))]
    (vec (concat binding new-values))) 

  (join data color-result '[?character :name ?name]) 
  
)
