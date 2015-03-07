(ns hashids.util-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [hashids.util :refer :all]
            [clojure.set]))

(deftest test-int->hexstr
  (is (= "2a" (int->hexstr 42))))

(deftest test-hexstr->int
  (is (= 42 (hexstr->int "2a"))))

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

