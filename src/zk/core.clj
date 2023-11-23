(ns zk.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:gen-class))

; https://joplinapp.org/help/api/references/rest_api/
(def port "http://localhost:41184/")
(def token "dcd2cbd0584a6c9d3b494c2ae78b0fc56255c1134b2ab8da8ffabbe78a7dcb28bda4f29360789d2c98d295283147af3f1515538986009fbdb143041de2403a95")

(defn auth-get [verb params fields]
  {:pre [ (map? params) (coll? fields)]} 
  (let [response
        (http/get (str port verb) 
                  {:accept :json :query-params
                   (merge params { "token" token "fields" (str/join "," (map name fields)) })})] 
    (let [{:strs [items has_more]} (json/read-str (:body response))]
      (eval items)      
    )))

  (defn tags-all 
    ([fields] 
     {:pre [(coll? fields)]} 
     (auth-get "tags" {} fields)) ; no query parameters
    ([] (tags-all [:id :parent_id :title])))
  
(count(tags-all [:title]))
