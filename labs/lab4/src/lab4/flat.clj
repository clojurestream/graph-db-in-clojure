(ns lab4.flat
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
                                     ;; map the variable to the value
                                     (assoc bndg p (nth triple n))
                                     (if (= p (nth triple n))
                                       ;; pattern element = triple element
                                       bndg
                                       ;; elements not equal: return nil
                                       (reduced nil)))))
                               {} (range 3)))
          data)))

(defn bind2
  [data pattern]
  (let [bindings (match data pattern)]
    (keep (fn [triple] (reduce (fn [bndg n]
                                 (let [p (nth pattern n)]
                                  (if (variable? p)
                                    (conj bndg (nth triple n))
                                    (if (= p (nth triple n)) bndg (reduced nil)))))
                               [] (range 3)))
          data)))

(comment
  (def data (load-data "../../graphs/starwars.edn")) 
  (match data '[?yoda :name "Yoda"]) 
  (match data '[?character :color "#000000"]) 
  (match data '[:yoda :interacts-with ?character]) 
  (bind data '[?yoda :name "Yoda"]) 
  (bind data '[?character :color "#000000"]) 
  (bind data '[:yoda :interacts-with ?character]) 

  (def color-pattern '[?character :color "#000000"]) 
  (def vars (vec (filter variable? color-pattern))) 
  (def black-characters (bind2 data color-pattern)) 
  (map #(zipmap vars %) black-characters) 
)
