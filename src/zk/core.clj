(ns zk.core
  (:require
   [asami.core :as d]
   [zk.joplin :as jop] 
[zk.markup :as markup]
   )
  )
; (:use [zk.model])

(defn note-id?
  [str]
  (some? (re-matches #"^[A-Fa-f0-9]{32}" str)))

(defn triples-from-md
  [note-id markdown]
  (map #(list note-id
                (markup/id-or-html (first %))   ; dt
                (markup/id-or-html (second %))) ; dd
       (markup/dt-dd-hiccup (markup/parse-body markdown))))


(def note-id "f37effc03aa042399db702484c6cda61") ; book

(def notes (jop/search
      note-id :note [:id :title :body] {:limit 10}))

(def note-relations
  (filter #(every? note-id? %)
          (mapcat #(triples-from-md (:id %) (:body %))
               notes))) ; jop/ALL

(defn attribute-to-triple
  [notes attr]
  (map #(vector (:id %) (symbol "jop" (name attr)) (attr %)) notes))

(attribute-to-triple notes)


;; Create an in-memory database, named dbname
(def db-uri "asami:mem://tmp2")
(d/create-database db-uri)

;; Create a connection to the database
(def conn (d/connect db-uri))

(defn id-namespace [entry]
  (if note-id? (symbol "a" entry) entry))


(defn add-graph
  [triples]
  (->>
   triples
   (map #(map id-namespace %))
   (mapv #(cons :db/add %))
   (d/transact conn)))



(def db (add-graph note-relations))
(pprint @db)

; https://github.com/quoll/asami/wiki/4.-Transactions#transaction-data-structure
(def tx (d/transact conn {:id "0ded4789cc9d49539f7b15ba986f1d45" :model/title "Hello World" }))
; https://github.com/quoll/asami/wiki/3.-Loading-Data
(def tx2 (d/transact conn [[ :db/add :a/f37effc03aa042399db702484c6cda61 :title "Hi again"]]))


(pprint @tx2)