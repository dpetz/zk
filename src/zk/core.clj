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
   )
  
  (:require ; my forks
   [cybermonday.core :refer [parse-body]] ; don't need :frontmatter
   ;[cybermonday.ir]
   [cybermonday.parser] ; list here to force reload 
   :reload)
  )



;(println (search "notebook:Meta")) ; title:Book

; test 0b442146044c486b8eca6a6432e8ed66
; author 19a0bb1074c9477b8204fb0d8e4baef5
; Clj book 028e98cdc99f4f249ea8eee7b8fbfb18
(def note-id "028e98cdc99f4f249ea8eee7b8fbfb18")

(def note-body (:body (jop/note note-id)))

;(pprint (cybermonday.ir/md-to-ir note-body)) ; hiccup vector
;(pprint (str (hp/html (:body (cybermonday.core/parse-md note-body)))))

(def note-html (str (h2/html (parse-body note-body)))) ; drop :frontmatter
(pprint note-html)



(defn tag-contents
  [html tag]
  ; (?<= ) text after, (?=< ) text before 
  ;(println tag html)
  (map #(first %)
   (re-seq (re-pattern (str "(?<=<" tag ">)(.|\n)*?(?=</" tag ">)")) html)))

(defn description-pairs
  "Scans hiccup for html description lists returnd sequence of 
   all :dt and :dd content pairs"
  [html-content]

(let

 term-chunks (fn [html]
              (tag-contents (map #(str "<dt>" %) (s/split html #"<dt>"))))

   [dt-dd-pairs
        (fn
          [html]
          (let [dt (tag-contents html "dt")] 
            (assert (= 1 (count dt)) (str "Expected single <dt>: " html))
            (map
             #(vector (first dt) %)
             (tag-contents html "dd"))))]
    
    (reduce
     #(concat %1 (dt-dd-pairs %2)) []
     ; swap from regex to clojure.zip for nested dls (allowed?) 
     (tag-contents html-content "dl"))))


(pprint (description-pairs note-html))


