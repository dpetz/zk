(ns zk.markup
  (:require
   [clojure.string :as s]
   [clojure.pprint :refer [pprint]]
   [zk.joplin :as jop]
   [hiccup2.core :as h2]
   [hickory.core]
   [clojure.zip :as zip])

  (:require ; my forks
   [cybermonday.core :as cyber] ; ignore :frontmatter
   [cybermonday.parser] ; force reload 
   :reload))

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

(defn hiccup-to-html [v] (str (h2/html v)))

;https://grishaev.me/en/clojure-zippers/
(defn iter-zip
  "Walks through depth-first"
  [zipper]
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
  "Scans hiccup. If contains single note id returns it;
   otherwise returns as html"
  [hiccup]
  (let [ids (note-ids (hrefs hiccup))]
    (if (= 1 (count ids))
      (first ids)
      (hiccup-to-html hiccup))))


(defn dt-dd-hiccup
  "Finds all description definitions in hiccup and pais them
   with description terms. Returns pairs of hiccups.
   Call with hiccup as single argument as entry point."
  ([loc dt pairs]
   (cond
     (zip/end? loc)
     pairs

     (= :dt (zip/node loc))
     (recur (zip/next loc) (zip/node (zip/rightmost loc)) pairs)

     (= :dd (zip/node loc))
     (recur (zip/next loc) dt
            (conj pairs (vector dt (vec (cons :div (rest (zip/node (zip/up loc))))))))

     :else
     (recur (zip/next loc) dt pairs)))

  ([hiccup] (dt-dd-hiccup (zip/vector-zip hiccup) nil [])))

(defn parse-body
  "Parse note body to hiccup (ignoring any cybermonday :frontmatter)"
  [markdown]
  (cyber/parse-body markdown)
  )

;(def note-id "028e98cdc99f4f249ea8eee7b8fbfb18")
   ; Watson's book
;(def note-body (:body (jop/note note-id)))
;(def note-hiccup (parse-body note-body))
;(pprint (map #(map id-or-html %) (dt-dd-pairs note-hiccup)))
  