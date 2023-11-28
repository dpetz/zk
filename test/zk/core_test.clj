(ns zk.core-test
  (:use [clojure.test] 
        [zk.model] :reload)
  (:require [clojure.spec.alpha :as s]
            [zk.core :as core] :reload))


(deftest tags

  (testing "n tags titles"
    (let [n 101 tags (core/tags [:title] {:limit n})]
      (is (= n (count tags)))
      (is (s/coll-of string?))))

  (testing "All tags with all fields (method default)"
    (let [tags (core/tags)] ; :id :title :parent_id
      (do
        (zk.model/spec-group-keys :zk.model/tag :req-un ::tag-complete)
        (is (s/valid? (s/coll-of ::tag-complete) tags)))
        ;(is (s/valid? (s/coll-of :zk.model/tag) tags)))
        )))

(deftest search
  (testing "Search folder id"
    (is (= (:title (core/search "Zettel" :folder)) "Zettel"))))
(testing "Meta contains `✒️ author`"
    (is (some #{"✒️ author"} (core/search (str "notebook:Meta") :note [:title]))))


(run-tests 'zk.core-test)

;(s/conform (s/coll-of :zk.model/tag) (core/tag-list [] {}))
