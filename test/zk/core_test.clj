(ns zk.core-test
  (:require [clojure.test :refer :all]
            [zk.core :refer :all])) ; [clojure.spec.alpha :as s]

(deftest tag-titles
  (testing "Fetching 3 tag titles"
    (let [tags (tags-all [:title] {:limit 3})]
      (is(= 3 (count tags)))
      (is (every? string? tags)))))
