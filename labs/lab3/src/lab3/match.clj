(ns lab3.match
  (:require [clojure.edn :as edn]))

(defn load-data
  [filename]
  (edn/read-string (slurp filename)))

(defn variable?
  [x]
  (and (symbol? x) (= \? (first (name x)))))

(defn element-match
  [p e]
  (or (variable? p) (= p e)))

(defn match
  [data pattern]
  (filter (fn [triple] (every? #(element-match (nth pattern %) (nth triple %)) (range 3))) data))

(defn bind
  [data pattern]
  (let [bindings (match data pattern)]
    (keep (fn [triple] (reduce (fn [bndg n]
                                 (let [p (nth pattern n)]
                                  (if (variable? p)
                                    ;; map the variable to the value from the triple
                                    (assoc bndg p (nth triple n))
                                    (if (= p (nth triple n))
                                      ;; the pattern element equals the triple element
                                      bndg
                                      ;; the pattern element does not equal the triple element: return nil
                                      (reduced nil)))))
                               {} (range 3)))
          data)))

(comment
  (def data (load-data "../../graphs/starwars.edn"))
  (match data '[?yoda :name "Yoda"])
  (match data '[?character :color "#000000"])
  (match data '[:yoda :interacts-with ?character])
  (bind data '[?yoda :name "Yoda"])
  (bind data '[?character :color "#000000"])
  (bind data '[:yoda :interacts-with ?character])
)
