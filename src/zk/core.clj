(ns zk.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.walk :as walk])
  (:gen-class))

; https://joplinapp.org/help/api/references/rest_api/
(def port "http://localhost:41184/")
(def token "dcd2cbd0584a6c9d3b494c2ae78b0fc56255c1134b2ab8da8ffabbe78a7dcb28bda4f29360789d2c98d295283147af3f1515538986009fbdb143041de2403a95")

(def data-model
  {:tag [:id
         :title
         :created_time
         :updated_time
         :user_created
         :user_updated_time
         :encryption_cipher
         :encryption_applied
         :is_shared
         :parent_id
         :user_data]})


(defn default-params
  [overwrites]
  (walk/stringify-keys 
   (merge {:page 0 ; 0 for `all`
           :limit 100 ; 100 is max   
           :order_by "updated_time" 
           :order_dir " DESC"}
          overwrites)))

(defn auth-get
  "If single field, returns collection of values"
  [verb fields params] 
  {:pre [ (coll? fields) (map? params)]} 
  (let [params-enriched
        (default-params (merge params {:token token :fields (str/join "," (map name fields))}))

        response
        (http/get (str port verb) {:accept :json :query-params params-enriched})

        {:strs [items has_more]}
        (json/read-str (:body response))] 
    (cond
      (= 1 (count fields))
          (let [field (name (first fields))] (map #(% field) #break items)) ; [{"title" "youtube"} ..] 
          :else (eval items))))

  (defn tags-all 
    ([fields params] 
     (auth-get "tags" fields params)) ; no query parameters
    ([] (tags-all [:id :parent_id :title] {})))
  
;(println (tags-all [:title] {:limit 10}))


