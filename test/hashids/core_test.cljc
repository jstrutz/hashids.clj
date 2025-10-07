(ns hashids.core-test
  (:require [clojure.test :refer [deftest is]]
            [hashids.core :as core]
            #?@(:cljd    ()
                :default ([clojure.test.check.clojure-test :refer [defspec]]
                          [clojure.test.check.generators :as gen]
                          [clojure.test.check.properties :as prop]))))

(def gen-salt #?(:cljd nil :default gen/string-alphanumeric))
(def gen-nums #?(:cljd nil :default (gen/not-empty (gen/vector gen/nat))))

(deftest test-known-encodings
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (doseq [[salt nums encoding] ['("this is my salt" [12345] "NkK9")
                                '("this is my salt" [12346] "69PV")
                                '("this was my salt" [12345] "dRn3")
                                '("this was my salt" 12345 "dRn3")
                                '("" [0] "gY")
                                '("" [0 1 1000000] "pwcnfVMX3")
                                '("this is my salt" [547 31 241271 311 31397 1129 71129] "3RoSDhelEyhxRsyWpCx5t1ZK")]]
    (is (= encoding (core/encode {:salt salt} nums)))
    (is (= (flatten (list nums)) (core/decode {:salt salt} encoding)))))

(deftest test-encode-long-args
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (is (= "pwcnfVMX3" (core/encode {:salt ""} 0 1 1000000))))

#?(:cljd
   (deftest test-respects-min-length
     (doseq [salt       ["salt" "blah"]
             nums       [[1] [1 20] [0 1 1000000]]
             min-length [1 2 3 10]]
       (is (<= min-length (count (core/encode {:salt salt :min-length min-length} nums))))))
   :default
   (defspec test-respects-min-length
     ;;"encode a bunch of numbers, and make sure that they return an empty collection when you attempt to decrypt with a different salt"
     200
     (prop/for-all [salt       gen-salt
                    nums       gen-nums
                    min-length gen/nat]
       (is (<= min-length (count (core/encode {:salt salt :min-length min-length} nums)))))))

#?(:cljd
   (deftest test-encodes-and-decodes
     (doseq [salt       ["salt" "blah"]
             nums       [[1] [1 20] [0 1 1000000]]
             min-length [1 2 3 10]]
       (is (= nums (core/decode {:salt salt :min-length min-length} (core/encode {:salt salt :min-length min-length} nums))))))
   :default
   (defspec test-encodes-and-decodes
     ;;"encode a bunch of numbers, and make sure that they return an empty collection when you attempt to decrypt with a different salt"
     200
     (prop/for-all [salt       gen-salt
                    nums       gen-nums
                    min-length gen/nat]
       (is (= nums (core/decode {:salt salt :min-length min-length} (core/encode {:salt salt :min-length min-length} nums)))))))


(deftest test-min-length-known-values
  "Test known encodings of integers from other hashids libraries, for a given salt"
  (is (= "B0NkK9A5" (core/encode {:salt "this is my salt" :min-length 8} 12345))))

(deftest test-encode-hex
  (is (= "kRNrpKlJ" (core/encode-hex {:salt "this is my salt"} "deadbeef"))))

(deftest test-decode-hex
  (is (= '("deadbeef") (core/decode-hex {:salt "this is my salt"} "kRNrpKlJ"))))
