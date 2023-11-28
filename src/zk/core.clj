(ns zk.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.walk :as walk] 
            [cybermonday.core :as cyber]
            [clojure.spec.alpha :as s])
  (:require [zk.model :as model] :reload)
  ) ; (:use [clojure.tools.logging])

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

(def INFO true)

(defn auth-get
  "If single field, returns collection of values. Empty fields vector for standard fields"
  ([verb fields params]
   {:pre [(coll? fields) (map? params)]}

   (let [params-all (default-params (assoc params :token token :fields (syms-to-str fields)))]

     (loop [cache []
            pars (update params-all :limit #(min % 100))
            gap (params-all :limit)]
       (let [response ; save-request? true :debug true 
             (http/get (str port verb) {:accept :json :query-params (walk/stringify-keys pars)})

             body
             (walk/keywordize-keys  (json/read-str (:body response))) ; 

             {:keys [items has_more]}
             (if (s/valid? :zk.model/response-items body) body {:items body :has_more 0})

             cached
             (concat cache (take gap (if-single-key-drop-it items fields)))

             gap-new
             (- gap (count items))]

           (when INFO (println (str/upper-case verb)  body)) 
           (if (and has_more (pos? gap-new))
             (recur cached (update pars :page #(inc %)) gap-new)
             (unwrap-if-alone  cached))))))

  ([verb fields]
   (auth-get verb fields {})))


(let [m {1 2 3 4}] 
  (if (map? m) m 0))

; ========== NOTES ==========

(def note-keys
  (model/spec-keys :zk.model/note))

(defn notes
  ([fields params]
   (auth-get "notes" fields params))
  ([fields] (notes fields {}))
  ([] (notes note-keys)))

(defn note [id]
  (auth-get (str "notes/" id) [] {})); note-keys

; ========== TAGS ==========


(defn tags
  ([fields params]
   (auth-get "tags" fields params))
  ([] (tags (model/spec-keys :zk.model/tag) {})))



; ========== SEARCH ==========


; https://joplinapp.org/help/#searching
; GET /search?query=recipes&type=folder
(defn search
  ([query type fields]
   {:pre [(contains? zk.model/item-types type)]}
   (auth-get "search" fields {:query query :type (name type)}))
  ([query type]
   (search query type []))
  ([query]
   (search query :note)))



; ========== STAGE ==========


(def book-id "f37effc03aa042399db702484c6cda61");

(println (search "notebook:Meta")) ; title:Book
;(println (note book-id))



  ;(cyber.core/parse-md my-markdown)