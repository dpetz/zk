(ns zk.core
  (:require
   ;[clojure.java.io :as io]
   ;[hickory.render :refer [hickory-to-html]]
   [hiccup2.core :as h2]
   [zk.joplin :as jop]
   [hickory.core]
   [clojure.string :as s]
   [hickory.zip :refer [hiccup-zip]]
   [clojure.zip :as zip]
   [hickory.select :as hys]
   [hickory.convert :refer [hiccup-to-hickory hiccup-fragment-to-hickory]]

   [clojure.string :as str])

  (:require ; my forks
   [cybermonday.core :refer [parse-body]] ; ignore :frontmatter
   ;[cybermonday.ir]
   [cybermonday.parser] ; force reload 
   :reload))

(defn hiccup-to-html [v] (str (h2/html v)))





;(println (search "notebook:Meta")) ; title:Book

; test 0b442146044c486b8eca6a6432e8ed66
; author 19a0bb1074c9477b8204fb0d8e4baef5
; Clj book 028e98cdc99f4f249ea8eee7b8fbfb18
(def note-id "028e98cdc99f4f249ea8eee7b8fbfb18")

(def note-body (:body (jop/note note-id)))

;(pprint (cybermonday.ir/md-to-ir note-body)) ; hiccup vector

(def note-hiccup (parse-body note-body))
(pprint note-hiccup)

; see https://pandoc.org/MANUAL.html#definition-lists
; pandoc's defintions are called descriptions in HTML5 
; terms are single line
; 1+ defs per term
; defs may contain blocks (lists, code, etc.)
; even dts may branch du to html formatting
;
; parsing logic
; 1 pair per dd
; for each dt/dd: returns first note id; otherwise html
; order maintained but not dl boundaries


;https://grishaev.me/en/clojure-zippers/
(defn iter-zip [zipper]
  "Walks through depth-first"
  (->> zipper
       (iterate zip/next)
       (take-while (complement zip/end?))))

(defn hrefs
  "Collects href value for each :a element"
  [hiccup]
  (->> hiccup 
       zip/vector-zip
       iter-zip
       (reduce #(if (= :a (zip/node %2))
                  (conj %1 (:href (zip/node (zip/right %2)))) %1) [])))

(defn note-ids
  "From collection such as
   [\"https://leanpub.com/clojureai\" \":/f37effc03aa042399db702484c6cda61\"]
  returns [\"f37effc03aa042399db702484c6cda61\"]"
  [col]
  (->> col
       (filter #(s/starts-with? % ":/"))
       (map #(subs % 2))))


(defn id-or-html
  "If hiccup contains any nested note ids return the first; otherwise html"
  [hiccup]
  (let [ ids (note-ids (hrefs hiccup))] 
       (if (empty? ids)
         (hiccup-to-html hiccup)
        (first ids))))


(defn dt-dd-pairs
  ([loc dt pairs]
     (cond
       (zip/end? loc)
       pairs
       (= :dt (zip/node loc))
       (do
         (println "INFO " pairs)
         (recur (zip/next loc) (id-or-html (zip/node (zip/rightmost loc))) pairs))
       (= :dd (zip/node loc))
       (recur (zip/next loc) dt
              (conj pairs (vector dt (id-or-html (vec (cons :div (rest (zip/node (zip/up loc)))))))))
       :else
       (recur (zip/next loc) dt pairs))) 

  ([hiccup] (dt-dd-pairs (zip/vector-zip hiccup) nil [])))


(pprint (note-ids (hrefs note-hiccup)))
(pprint (dt-dd-pairs note-hiccup))

