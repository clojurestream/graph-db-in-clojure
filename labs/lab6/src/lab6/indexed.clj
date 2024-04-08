(ns lab6.indexed
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

(defn project
  [selected-vars result]
  (->> (:bindings result)
       (map #(zipmap (:vars result) %))
       (map #(select-keys % selected-vars))))

(defn step
  [graph part-result pattern]
  (when-not (vector? pattern)
    (throw (ex-info "Pattern must be a vector" {:pattern pattern})))
  (if (list? (first pattern))
    (do-filter part-result pattern)
    (join graph part-result pattern)))

(def identity-binding {:vars [] :bindings [[]]})

(defn query->map
  "Takes a string, seq, or map, and returns a map of :find and :where"
  [query]
  (let [query (if (string? query) (edn/read-string query) query)]
    (if (map? query)
      query
      (let [find-clause (take-while #(not= :where %) query)
            where-clause (drop-while #(not= :where %) query)]
        (when (or (not= :find (first find-clause))
                  (not= :where (first where-clause)))
          (throw (ex-info "Query must start with :find and :where" {:query query})))
        {:find (rest find-clause)
         :where (rest where-clause)}))))

(defn q
  [query graph]
  (let [{:keys [find where]} (query->map query)]
    (->> (reduce (partial step graph) identity-binding where)
         (project find))))

(comment
  ;; Load the starwars graph
  (def data (load-data "../../graphs/starwars.edn")) 

  ;; [:find ?name ?color :where [?character :name ?name] [?character :color ?color] [(str/starts-with ?name "Darth")]]
  (def name-pattern '[?character :name ?name]) 
  (def color-pattern '[?character :color ?color]) 
  (def filter-pattern '[(str/starts-with? ?name "Darth")]) 
  (def first-step (join data identity-binding name-pattern)) 
  (def second-step (join data first-step color-pattern))
  (def last-step (do-filter second-step filter-pattern))
  (project '[?name ?color] last-step)
  
  (def name-color-query '[:find ?name ?color
                          :where [?character :name ?name]
                                 [?character :color ?color]
                                 [(str/starts-with? ?name "Darth")]])

  (q name-color-query data)
)
