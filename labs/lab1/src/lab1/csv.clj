(ns lab1.csv
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [lab1.util :refer [keyname node-id capitalize-name]]))

(defn csv->objects
  "Convert a CSV file to maps"
  [csv-file]
  (with-open [reader (io/reader csv-file)]
    (let [[hdr & csv-data] (csv/read-csv reader)
          header (map #(keyword (str/replace % #"_" "-")) hdr)
          objects (mapv #(zipmap header %) csv-data)]
      objects)))

(defn obj->triples
  "Convert a map to triples"
  ([object] (obj->triples object :app-id))
  ([object key-field]
   (let [id (keyword (str "g" (get object key-field)))]
     ;; TODO
     )))

(defn write-triples
 [filename triples]
 (with-open [writer (io/writer filename)]
   (binding [*out* writer]
     (prn triples))))

(defn convert-csv
  [filename]
  (->> filename
       csv->objects
       (mapcat obj->triples)
       (into [])
       (write-triples (str/replace filename #"\.csv$" ".ttl"))))

