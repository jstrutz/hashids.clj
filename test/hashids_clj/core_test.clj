(ns hashids-clj.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [hashids-clj.core :refer :all]))

(deftest test-known-encodings
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (let [known-encodings ['("this is my salt" [12345] "NkK9")
                         '("this is my salt" [12346] "69PV")
                         '("this was my salt" [12345] "dRn3")
                         '("this was my salt" 12345 "dRn3")
                         '("" [0] "gY")
                         '("" [0 1 1000000] "pwcnfVMX3")
                         '("this is my salt" [547 31 241271 311 31397 1129 71129] "3RoSDhelEyhxRsyWpCx5t1ZK")]
        encode-test (fn [arglist] (let [[salt, nums, encoding] arglist]
                                    (is (= encoding (encode salt nums)))))
        decode-test (fn [arglist] (let [[salt, nums, encoding] arglist]
                                    (is (= (flatten (list nums)) (decode salt encoding)))))]
    (testing "encoding"
      (doall (map encode-test known-encodings))
      (doall (map decode-test known-encodings)))))
