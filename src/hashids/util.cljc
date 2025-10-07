(ns hashids.util
  #?(:cljs (:require-macros [hashids.util :refer [xor]]))
  (:require [clojure.edn :as edn]
            [clojure.math :as math]
            [clojure.string :as str]))

(defmacro xor
  ([] nil)
  ([a] a)
  ([a b]
   `(let [a# ~a
          b# ~b]
      (if a#
        (if b# false a#)
        (if b# b# false)))))

(defn long->hexstr [n]
  #?(:cljs    (js-invoke n "toString" 16)
     :cljd    (.toRadixString ^int n 16)
     :default (format "%x" n)))

(defn ceil [n]
  (long (#?(:cljr math/ceiling :default math/ceil) n)))

(defn hexstr->long
  [s]
  (try
    (edn/read-string (str "0x" s))
    (catch #?(:clj  java.lang.NumberFormatException
              :cljr System.FormatException
              :cljd FormatException
              :cljs :default) _)))

(defn char-code [c]
  #?(:cljs    (js-invoke c "charCodeAt" 0)
     :default (long c)))

(defn positions
  "Returns the indexes of the items in the collection whose items satisfy the predicate"
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn swap [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(defn strip-whitespace [s]
  (apply str (remove str/blank? (map str s))))

(defn chars-intersection [str1 str2]
  (keep (fn [c]
          (some #{c} str2))
        (distinct str1)))

(defn chars-difference [str1 str2]
  (filter (fn [c] (xor (some #{c} str1)
                       (some #{c} str2)))
          (distinct (str str1 str2))))

(defn chars-subtraction [str1 str2]
  (remove #(some #{%} str2) str1))


(defn split-on-chars
  [instr splitstr]
  (map #(map second %)
       (partition-by first
                     (second (reduce
                               (fn [[prev-chg letters] letter]
                                 (let [is-sep   (boolean (some #{letter} splitstr))
                                       this-chg (xor prev-chg is-sep)]
                                   [this-chg (if is-sep
                                               letters
                                               (conj letters [this-chg letter]))]))
                               [false []]
                               instr)))))
