(ns hashids.util-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [clojure.set :as set]
            [hashids.util :as util]
            #?@(:cljd    ()
                :default ([clojure.test.check.clojure-test :refer [defspec]]
                          [clojure.test.check.generators :as gen]
                          [clojure.test.check.properties :as prop]))))

(deftest test-long->hexstr
  (is (= "2a" (util/long->hexstr 42)))
  (is (= 3735928559 (util/hexstr->long "deadbeef")))
  (is (= "deadbeef" (util/long->hexstr 3735928559))))

(deftest test-hexstr->long
  (is (= 42 (util/hexstr->long "2a")))
  (is (= 3735928559 (util/hexstr->long "deadbeef"))))

(deftest test-hexstr->long-bad-input
  (is (nil? (util/hexstr->long "XYZ"))))

(deftest split-on-chars-test
  (is (= '((\p \w) (\n) (\V \M \X \3)) (util/split-on-chars "pwcnfVMX3" "cfhistuCFHISTU"))))

(deftest positions-test
  (is (= '(1 2 4) (util/positions even? '(1 20 22 17 12 19)))))

(defn chars-not-contained-in? [splitstr s]
  (empty? (set/intersection (set s) (set splitstr))))

#?(:cljd
   (deftest split-on-chars-never-returns-splitchars
     (is (= [["d" "e" "f" "g"]] (util/split-on-chars "abcdefg" "abc")))
     (is (= [["d" "d"]] (util/split-on-chars "abadbcd" "abc")))
     (is (= [["a" "b" "c" "1" "2" "3"]] (util/split-on-chars "abc123" ""))))
   :default
   (defspec split-on-chars-never-returns-splitchars
     1000
     (prop/for-all [instr    gen/string
                    splitstr gen/string]
       (is (every? (partial chars-not-contained-in? splitstr) (util/split-on-chars instr splitstr))))))

#?(:cljd
   (deftest strip-whitespace-never-returns-whitespace
     (is (not-any? str/blank? (map str (util/strip-whitespace " \r\n\t ")))))
   :default
   (defspec strip-whitespace-never-returns-whitespace
     ;;"consistent-shuffle returns something other than the alphabet for a non-empty string"
     1000
     (prop/for-all [s gen/string]
       (is (not-any? str/blank? (map str (util/strip-whitespace s)))))))

