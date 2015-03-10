(ns hashids.util-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [hashids.util :refer :all]
            [clojure.set]))

(deftest test-long->hexstr
  (is (= "2a" (long->hexstr 42)))
  (is (= "deadbeef" (long->hexstr 3735928559))))

(deftest test-hexstr->long
  (is (= 42 (hexstr->long "2a")))
  (is (= 3735928559 (hexstr->long "deadbeef"))))

(deftest test-hexstr->long-bad-input
  (is (= nil (hexstr->long "XYZ"))))


(deftest split-on-chars-test
  (is (= '( (\p \w) (\n) (\V \M \X \3)) (split-on-chars "pwcnfVMX3" "cfhistuCFHISTU"))))

(deftest positions-test
  (is (= '(1 2 4) (positions even? '(1 20 22 17 12 19)))))

(defspec split-on-chars-never-returns-splitchars
  1000
  (prop/for-all [instr gen/string
                 splitstr gen/string]
                (is (every? (fn [s]
                              (empty? (clojure.set/intersection (set s) (set splitstr))))
                            (split-on-chars instr splitstr)))))

(defspec strip-whitespace-never-returns-whitespace
  ;;"consistent-shuffle returns something other than the alphabet for a non-empty string"
  1000
  (prop/for-all [s gen/string]
                (is (not-any? clojure.string/blank? (map str (strip-whitespace s))))))

