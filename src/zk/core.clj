(ns zk.core
  (:require
   ;[clojure.java.io :as io]
   ;[hickory.render :refer [hickory-to-html]]
   [hiccup2.core :as h]
   [zk.joplin :as jop])
  
  (:require
   [cybermonday.core]
   [cybermonday.ir]
   [cybermonday.parser] ; my forks
   :reload)
  )




;(println (search "notebook:Meta")) ; title:Book


(def author-id "19a0bb1074c9477b8204fb0d8e4baef5")
(def author-body (:body (jop/note author-id)))


(pprint author-body)


(pprint (cybermonday.ir/md-to-ir author-body)) 
(pprint (str (h/html (:body (cybermonday.core/parse-md author-body)))))