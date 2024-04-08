(ns lab4.indexed
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


(comment
  ;; Load the starwars graph
  (def data (load-data "../../graphs/starwars.edn"))

  ;; raw access to the indexes
  (get-in (:spo data) [:yoda :name])
  (get (:osp data) "#191970")
  (let [idx (get (:osp data) "#191970")]
    (for [[s pm] idx p (keys pm)] [s p]))

  ;; set up variables and patterns
  (def name-pattern '[?character :name ?name])
  (def vars (vec (filter variable? name-pattern)))
  (def template (normalize nil name-pattern))

  ;; select statements from the graph
  (match data '[?yoda :name "Yoda"])
  (match data '[?character :color "#000000"])
  ;; demonstrate that we can still create the bindings maps
  (def rabe (match data '[:rabe ?attribute ?value]))
  (map #(zipmap (:vars rabe) %) (:bindings rabe))

  ;; demonstrate filtering results
  (def names (match data '[?character :name ?name]))
  (filter (fn [[?character ?name]] (str/starts-with? ?name "Darth")) (:bindings names))

  ;; manually build a filter function
  (def fltr '(str/starts-with? ?name "Darth"))
  (def vars (:vars names))
  (def filter-fn (eval `(fn [~vars] ~fltr)))
  (filter filter-fn (:bindings names))

  ;; Execute a match and filter in one go
  (-> data
      (match '[?character :name ?name])
      (do-filter '[(str/starts-with? ?name "Darth")]))
)
