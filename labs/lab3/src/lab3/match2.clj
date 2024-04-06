(ns lab3.match2
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

(defn normalize
  "Convert a pattern into a normalized form"
  [pattern]
  (mapv #(if (variable? %) :sym :fix) pattern))

(defmulti tester normalize)

(defmethod tester [:sym :sym :sym] [pattern] (constantly true))
(defmethod tester [:fix :sym :sym] [[sp pp op]] (fn [[s p o]] (= sp s)))
(defmethod tester [:sym :fix :sym] [[sp pp op]] (fn [[s p o]] (= pp p)))
(defmethod tester [:sym :sym :fix] [[sp pp op]] (fn [[s p o]] (= op o)))
(defmethod tester [:fix :fix :sym] [[sp pp op]] (fn [[s p o]] (and (= sp s) (= pp p))))
(defmethod tester [:fix :sym :fix] [[sp pp op]] (fn [[s p o]] (and (= sp s) (= op o))))
(defmethod tester [:sym :fix :fix] [[sp pp op]] (fn [[s p o]] (and (= pp p) (= op o))))
(defmethod tester [:fix :fix :fix] [[sp pp op]] (fn [[s p o]] (and (= sp s) (= pp p) (= op o))))

(defn lookup-match
  [data pattern]
  (filter (tester pattern) data))

