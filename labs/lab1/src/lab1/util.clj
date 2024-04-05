(ns lab1.util
  (:require [clojure.string :as str]))

(defn keyname [name]
  (-> name
      (str/replace #"[ _/]" "-")
      str/lower-case
      keyword))

(defn node-id
  [node]
  (-> node :name keyname))

(defn replace-char
  [s idx c]
  (str (subs s 0 idx) c (subs s (inc idx))))

(defn capitalize-name
  [name]
  (if (re-find #"[0-9]" name)
    name  ;; names with numbers are droids 
    (let [subnames (-> name
                       (str/replace #"[ _/]" "-")
                       (str/split #"-"))
          newname (->> subnames 
                      (map #(str/capitalize %))
                      (str/join " "))
          len (count newname)]
      (loop [result newname idx 0]
        (if (>= idx len)
          result
          (let [c (get name idx)]
            (if (#{\- \/ \_} c)
              (recur (replace-char result idx c) (inc idx))
              (recur result (inc idx)))))))))

