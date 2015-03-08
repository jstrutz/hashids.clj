(ns hashids.util)

(defmacro xor
  ([] nil)
  ([a] a)
  ([a b]
    `(let [a# ~a
           b# ~b]
      (if a#
        (if b# false a#)
        (if b# b# false)))))

(defn expt
  [b e]
  (java.lang.Math/pow b e))

(defn long->hexstr [n] (format "%x" n))
(defn hexstr->long
  [s]
  (try
    (reduce + (map-indexed (fn [idx c]
               (* (int (expt 16 idx)) (Integer/parseInt (str c) 16))) (reverse s)))
  (catch java.lang.NumberFormatException e nil)))

(defn ceil
  [v]
  (long (java.lang.Math/ceil v)))


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
  (apply str (remove clojure.string/blank? (map str s))))

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
                                (let [is-sep (boolean (some #{letter} splitstr))
                                      this-chg (xor prev-chg is-sep)]
                                  [this-chg (if is-sep
                                              letters
                                              (conj letters [this-chg letter]))]))
                              [false []]
                              instr)))))
