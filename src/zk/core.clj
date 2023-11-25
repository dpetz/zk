(ns zk.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.walk :as walk])
  (:require [zk.model :as model] :reload))

; https://joplinapp.org/help/api/references/rest_api/
(def port "http://localhost:41184/")
(def token "dcd2cbd0584a6c9d3b494c2ae78b0fc56255c1134b2ab8da8ffabbe78a7dcb28bda4f29360789d2c98d295283147af3f1515538986009fbdb143041de2403a95")

(defn default-params
  [overwrites]
  (merge {:page 1
          :limit 100
          :order_by "updated_time"
          :order_dir " DESC"}
         overwrites))

(defn only
  "if collection has single item returns it, otherwise nil"
  [col] (case (count col) 1 (first col) nil))

(defn if-single-key-drop-it
  "E.g. [{:title \"youtube\"} ..] to [\"youtube\" ..]. Otherwise unchanged."
  [col keys]
  (if-let [key (only keys)]
    (map #(% key) col)
    col))

(defn syms-to-str [col] (str/join "," (map name col)))

(def ALL 99999)

(defn unwrap-if-alone
  [coll]
  (if (empty? (rest coll)) (first coll) coll))

(defn auth-get
  "If single field, returns collection of values. Empty fields vector for standard fields"
  [verb fields params]
  {:pre [(coll? fields) (map? params)]}

  (let [params-all (default-params (assoc params :token token :fields (syms-to-str fields)))]

    (loop [cache []
           pars (update params-all :limit #(min % 100))
           gap (params-all :limit)]
      (let [response
            (http/get (str port verb) {:accept :json :query-params (walk/stringify-keys pars)})

            {:keys [items has_more]}
            (walk/keywordize-keys (json/read-str (:body response)))

            cached
            (concat cache (take gap (if-single-key-drop-it items fields)))

            gap-new
            (- gap (count items))]


        (if (and has_more (pos? gap-new))
          (recur cached (update pars :page #(inc %)) gap-new)
          (unwrap-if-alone cached))))))

(empty (rest [1 2]))

(defn note-list
  ([fields params]
   (auth-get "notes" fields params))
  ([fields] (note-list fields {}))
  ([] (note-list (model/spec-keys :zk.model/note))))


(defn tag-list
  ([fields params]
   (auth-get "tags" fields params))
  ([] (tag-list (model/spec-keys :zk.model/tag) {})))

   ; https://joplinapp.org/help/#searching
; GET /search?query=recipes&type=folder
(defn search
  ( [query type]
   {:pre [(contains? zk.model/item-types type)]}
    (auth-get "search" [] {:query query :type (name type)}))
  ( [query]
   (search query :note)))
  


  (def meta-id
    (get (search "Meta" :folder) :id))

(def meta-notes
  (search (str "notebook:Meta")))


(println meta-notes)
  