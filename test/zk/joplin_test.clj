(ns zk.joplin-test
  (:use [clojure.test])
  (:require [clojure.spec.alpha :as s])
  (:require
   [zk.joplin :as jop]
   [zk.model :as model]:reload))


(deftest tags

  (testing "n tags titles"
    (let [n 101 tags (:jop/tags [:title] {:limit n})]
      (is (= n (count tags)))
      (is (s/coll-of string?))))

  (testing "All tags with all fields (method default)"
    (let [tags :jop/tags] ; :id :title :parent_id
      (do
        (model/spec-group-keys :zk.joplin/tag :req-un :zk.joplin-test/tag-completetag-complete)
        (is (s/valid? (s/coll-of ::tag-complete) tags))))))
      

(deftest search
  (testing "Search folder id"
    (is (= (:title (jop/search "Zettel" :folder)) "Zettel")))
(testing "Meta contains `✒️ author`"
  (is (some #{"✒️ author"} (jop/search "notebook:Meta" :note [:title])))))


(run-tests *ns*) ; 'zk.joplin-test

;(s/conform (s/coll-of :jop/tag) (core/tag-list [] {}))
