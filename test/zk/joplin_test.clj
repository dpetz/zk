(ns zk.joplin-test
  (:use [clojure.test]
        [zk.model] :reload)
  (:require [clojure.spec.alpha :as s]
            [zk.joplin :as jop] :reload))


(deftest tags

  (testing "n tags titles"
    (let [n 101 tags (jop/tags [:title] {:limit n})]
      (is (= n (count tags)))
      (is (s/coll-of string?))))

  (testing "All tags with all fields (method default)"
    (let [tags (jop/tags)] ; :id :title :parent_id
      (do
        (zk.model/spec-group-keys :zk.model/tag :req-un ::tag-complete)
        (is (s/valid? (s/coll-of ::tag-complete) tags)))
        ;(is (s/valid? (s/coll-of :zk.model/tag) tags)))
      )))

(deftest search
  (testing "Search folder id"
    (is (= (:title (jop/search "Zettel" :folder)) "Zettel"))))
(testing "Meta contains `✒️ author`"
  (is (some #{"✒️ author"} (jop/search (str "notebook:Meta") :note [:title]))))


(run-tests 'zk.joplin-test)

;(s/conform (s/coll-of :zk.model/tag) (core/tag-list [] {}))
