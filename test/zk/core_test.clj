(ns zk.core-test
  (:require [clojure.test :refer :all] 
            [zk.core :refer :all]
            [zk.model :refer :all]
            [clojure.spec.alpha :as s])) ; [clojure.spec.alpha :as s]

(deftest tags
  (testing "3 tags with titles only"
    (let [tags (tags-all [:title] {:limit 3})]
      (is(= 3 (count tags)))
      (is (every? string? tags))))
  
  (testing "All tags with all fields (method default)"
    (let [tags (tags-all [] {})]
      (s/valid? :zk.model/tag (first tags)))))
