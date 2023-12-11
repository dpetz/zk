(ns zk.model
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))


(defn successor 
  "successor of first occurence of `item` in `coll` or `nil`"
  [item coll]
  (let [ix (.indexOf coll item)]
    ( if (= ix -1) nil (coll (inc ix)))))

(defn spec-keys
  "get  keys defined in :req :req-un :opt :opt-un in one list"
  [spec-symbol]
  (let [spec-form (vec (s/form (s/get-spec spec-symbol)))]
   (flatten (filter some? (map #(successor % spec-form) [:req :req-un :opt :opt-un])))))


(defn list-specs 
  "List all specs for given namespace. Replace namespace with prefix."
  ([ns prefix]
   (map #(str prefix %)
        (sort (map name (filter #(str/starts-with? % ns) (keys (s/registry)))))))
  ([] (list-specs ":zk.joplin" "::")))


(defmacro spec-group-keys
  "Defines a new spec based on existing with all keys moved in one group (:req :req-un :opt :opt-un)"
  [old-spec group new-spec]
  (list 's/def new-spec (list 's/keys group (vec (spec-keys old-spec)))))


;  (s/describe (spec-group-keys ::tag :req-un ::temp))