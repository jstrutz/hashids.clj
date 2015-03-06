(ns hashids.impl-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [hashids.impl :refer :all]))

(def gen-alphabet (gen/return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"))
(def gen-salt gen/string-alphanumeric)

(defspec consistent-shuffle-empty-salt
  ;;"consistent-shuffle returns the alphabet if given an empty salt"
  1000
  (prop/for-all [alphabet gen-alphabet]
                (= alphabet (consistent-shuffle alphabet ""))))

(defspec consistent-shuffle-non-empty
  ;;"consistent-shuffle returns something other than the alphabet for a non-empty salt"
  1000
  (prop/for-all [alphabet gen-alphabet
                 salt (gen/not-empty gen-salt)]
                (not= alphabet (consistent-shuffle alphabet salt))))

(deftest setup-creates-proper-guards
  "Setup returns default values"
  (let [opts (setup {:salt "this is my salt"})]
    (is (= "5N6y2rljDQak4xgzn8ZR1oKYLmJpEbVq3OBv9WwXPMe7" (opts :alphabet)))
    (is (= "UHuhtcITCsFifS" (opts :seps)))
    (is (= "AdG0" (opts :guards)))
    (is (= 0 (opts :min-length)))))

(deftest setup-returns-defaults
  (let [opts (setup)]
    (is (= "gjklmnopqrvwxyzABDEGJKLMNOPQRVWXYZ1234567890" (opts :alphabet)))
    (is (= DEFAULT_SEPS (opts :seps)))
    (is (= "abde" (opts :guards)))
    (is (= "" (opts :salt)))
    (is (= 0 (opts :min-length)))))

(deftest consistent-shuffle-known-shuffle
  (is (= "dceba"
         (consistent-shuffle "abcde" "this is my salt")))
  (is (= "fcaodykrgqvblxjwmtupzeisnh"
         (consistent-shuffle "abcdefghijklmnopqrstuvwxyz" "this is my salt")))
  (is (= "f17a8zvCwo0iuqYDXlJ4RmAS2end5ghTcpjbOWLK9GFyE6xUI3ZBMQtPsNHrkV"
         (consistent-shuffle DEFAULT_ALPHABET "salt"))))

(deftest enhash-known-cases
  (is (= "a" (enhash 0 "abcdefg")))
  (is (= "bf" (enhash 12 "abcdefg")))
  (is (= "ga" (enhash 42 "abcdefg")))
  (is (= "cde" (enhash 123 "abcdefg")))
  (is (= "cggc" (enhash 1024 "abcdefg")))
  (is (= "bbadeefc" (enhash 950000 "abcdefg")))
  (is (= "ääå-ÅÅÄö" (enhash 950000 "åäö-ÅÄÖ")))
  (is (= "ebfbfaea" (enhash 3500000 "abcdefg")))
  (is (= "1y-y-X1X" (enhash 3500000 "Xyz01-å"))))

(deftest dehash-known-cases
  (is (= 59 (dehash "abbd" "abcdefg")))
  (is (= 66 (dehash "abcd" "abcdefg")))
  (is (= 100 (dehash "acac" "abcdefg")))
  (is (= 139 (dehash "acfg" "abcdefg")))
  (is (= 218 (dehash "x21y" "xyz1234")))
  (is (= 440 (dehash "yy44" "xyz1234")))
  (is (= 1045 (dehash "1xzz" "xyz1234"))))

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
                                    (is (= encoding (encode {:salt salt} (flatten (list nums)))))))]
    (testing "encoding"
      (doall (map encode-test known-encodings)))))

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
                                    (is (= encoding (encode {:salt salt} (flatten (list nums)))))))
        decode-test (fn [arglist] (let [[salt, nums, encoding] arglist]
                                    (is (= (flatten (list nums)) (decode {:salt salt} encoding)))))]
    (testing "encode"
      (doall (map encode-test known-encodings)))
    (testing "decode"
      (doall (map decode-test known-encodings)))))
