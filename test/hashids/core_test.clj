(ns hashids.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [hashids.core :refer :all]))

(def gen-salt gen/string-alphanumeric)
(def gen-nums (gen/not-empty (gen/vector gen/pos-int)))

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
                                    (is (= encoding (encode {:salt salt} nums)))))
        decode-test (fn [arglist] (let [[salt, nums, encoding] arglist]
                                    (is (= (flatten (list nums)) (decode {:salt salt} encoding)))))]
    (testing "encoding"
      (doall (map encode-test known-encodings))
      (doall (map decode-test known-encodings)))))

(deftest test-encode-long-args
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (is (= "pwcnfVMX3" (encode {:salt ""} 0 1 1000000))))

(defspec test-respects-min-length
  ;;"encode a bunch of numbers, and make sure that they return an empty collection when you attempt to decrypt with a different salt"
  200
  (prop/for-all [salt gen-salt
                 nums gen-nums
                 min-length gen/pos-int]
                (is (<= min-length (count (encode {:salt salt :min-length min-length} nums))))))

(defspec test-encodes-and-decodes
  ;;"encode a bunch of numbers, and make sure that they return an empty collection when you attempt to decrypt with a different salt"
  200
  (prop/for-all [salt gen-salt
                 nums gen-nums
                 min-length gen/pos-int]
                (is (= nums (decode {:salt salt :min-length min-length} (encode {:salt salt :min-length min-length} nums))))))


(deftest test-min-length-known-values
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (is (= "B0NkK9A5" (encode {:salt "this is my salt" :min-length 8} 12345))))

(deftest test-encode-hex
  (is (= "kRNrpKlJ" (encode-hex {:salt "this is my salt"} "deadbeef"))))

(deftest test-decode-hex
  (is (= '("DEADBEEF") (decode-hex {:salt "this is my salt"} "kRNrpKlJ"))))
