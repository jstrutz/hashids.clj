(ns hashids.impl
  (:require [hashids.util :refer :all]))

(def DEFAULT_ALPHABET "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
(def DEFAULT_SEPS "cfhistuCFHISTU")
(def DEFAULT_SALT "")
(def DEFAULT_MIN_LENGTH 0)

(def MIN_ALPHABET_LENGTH 16)

(def SEP_DIV (/ 7 2))
(def GUARD_DIV 12)

(defn consistent-shuffle
  [alphabet salt]
  (if (empty? salt)
    alphabet
    (apply str (reduce (fn [[alph p] [idx a]]
                         (let [i (- (count alph) idx 1)
                               v (mod idx (count salt))
                               n (int (nth salt v))
                               p (+ p n)]
                           (if (zero? i)
                             alph
                             [(swap alph i (mod (+ n v p) i)) p])))
                       [(vec alphabet) 0]
                       (map-indexed (fn [idx a] [idx a]) alphabet)))))

(defn enhash
  ;; Known as 'hash' in other implementations
  ([input alphabet] (enhash input alphabet (num input) ""))
  ([input alphabet n res]
   (cond
    (zero? input) (subs alphabet 0 1)
    (zero? n) res
    :else (recur input
                 alphabet
                 (int (/ n (count alphabet)))
                 (str (nth (cycle alphabet) n) res)))))

(defn dehash
  ;; Known as 'unhash' in other implementations
  [input alphabet]
  (reduce +
          (remove nil?
                  (map-indexed
                   (fn [idx c]
                     (if-let [pos (first (positions #{c} alphabet))]
                       (* pos (int (expt (count alphabet)
                                         (- (count input) idx 1))))))
                   (vec input)))))

(defn encode-numbers
  [{:keys [seps alphabet salt hash-int numbers], :as args}]
  (let [lottery  (str (nth alphabet (mod hash-int (count alphabet))))
        sepsc    (cycle seps)
        enc-result (reduce (fn [[alph ret] [idx n]]
                             (let [buf     (concat lottery salt alph)
                                   alph    (consistent-shuffle (vec alph) (take (count alph) buf))
                                   encchar (enhash n alph)
                                   addsep  (nth sepsc (mod n (+ (int (first encchar)) idx)))]
                               (if (< (+ idx 1) (count numbers))
                                 [alph (str ret encchar addsep)]
                                 [alph (str ret encchar)])))
                           [alphabet lottery] ;; reduce by passing along alphabet, which is transformed in each iteration
                           (map-indexed vector numbers))]
    (assoc args
      :alphabet (first enc-result)
      :hash-str (second enc-result))))


(defn add-guards
  [{:keys [min-length guards hash-int hash-str] :as all-args}]

  (let [prepend-guard #(if (< (count %) min-length)
                         (str (nth (cycle guards)
                                   (+ hash-int (int (nth % 0))))
                              %)
                         %)
        append-guard #(if (< (count %) min-length)
                        (str %
                             (nth (cycle guards)
                                  (+ hash-int (int (nth % 2)))))
                        %)]
    (assoc all-args :hash-str (->> hash-str
                                   prepend-guard
                                   append-guard))))

(defn ensure-min-length
  [{:keys [min-length alphabet hash-str] :as all-args}]

  (let [half-length (int (/ (count alphabet) 2))
        upsize (fn [[alph ret]]
                 (let [alph (consistent-shuffle alph alph)
                       rplusalph (str (subs alph half-length) ret (subs alph 0 half-length))
                       excess (- (count rplusalph) min-length)
                       ret-start (int (/ excess 2))
                       ret-end (+ ret-start min-length)]
                   (if (pos? excess)
                     [alph (subs rplusalph ret-start ret-end)]
                     [alph rplusalph])))
        result (first (drop-while #(< (count (second %)) min-length)
                                  (iterate upsize [alphabet hash-str])))]
    (assoc all-args
      :alphabet (first result)
      :hash-str (second result))))



(defn balance-seps
  "Balance alphabet and seps, the ratio of sizes of which should SEP_DIV"
  [seps alph]
  (let [seps-length-ceil (ceil (/ (count alph) SEP_DIV))
        seps-length (if (= 1 seps-length-ceil)
                      2
                      seps-length-ceil)
        seps-diff (- seps-length (count seps))
        split-alph (map clojure.string/join (split-at seps-diff alph))]

    (if (or (zero? (count seps))
            (> (/ (count alph)
                  (count seps))
               SEP_DIV))
      (if (pos? seps-diff)
        [(str seps (first split-alph)) (last split-alph)]
        [(subs seps 0 seps-length) alph])
      [seps alph])))

(defn extract-guards
  "Take portions of seps or alphabet to make guards"
  [alph seps]
  (let [guard-length (ceil (/ (count alph) GUARD_DIV))]
    (if (< (count alph) 3)
      {:guards (subs seps 0 guard-length)
       :seps (subs seps guard-length)
       :alphabet alph}
      {:guards (subs alph 0 guard-length)
       :seps seps
       :alphabet (subs alph guard-length)})))


(defn setup
  ([] (setup {}))
  ([{:keys [seps alphabet salt min-length]
    :or {seps       DEFAULT_SEPS
         alphabet   DEFAULT_ALPHABET
         salt       DEFAULT_SALT
         min-length DEFAULT_MIN_LENGTH}}]
    {:pre  [(>= (count alphabet) MIN_ALPHABET_LENGTH)]}
  (let [alph-unbal (->> (chars-subtraction alphabet seps) ;; Alphabet should not contains seps
                        distinct
                        strip-whitespace)
        seps-unbal (->> (chars-intersection alphabet seps) ;; Seps should only contain characters present in alphabet
                        distinct
                        strip-whitespace)
        [seps-bal alph-bal] (balance-seps (consistent-shuffle seps-unbal salt) alph-unbal)]
    (assoc (extract-guards (consistent-shuffle alph-bal salt) seps-bal)
      :min-length min-length
      :salt salt))))

(defn encode-intern
  [opts numbers]
  {:pre [(coll? numbers)]}
  (let [settings (setup opts)
        hash-int (reduce + (map-indexed (fn [idx num] (mod num (+ idx 100))) numbers))]
     (->> (assoc settings :hash-int hash-int :numbers numbers)
          encode-numbers
          add-guards
          ensure-min-length)))

(defn encode
  [opts numbers]
  {:pre [(coll? numbers)]}
  (:hash-str (encode-intern opts numbers)))


(defn decode
  [opts encstr]
  {:pre [(string? encstr)]}
  (let [{:keys [seps alphabet salt min-length guards]} (setup opts)
        breakdown (split-on-chars encstr guards)
        breakdown-idx (if (some #{(count breakdown)} '(2 3)) 1 0)
        bdn (nth breakdown breakdown-idx)
        lottery (first bdn)
        arr (split-on-chars (drop 1 bdn) seps)
        decoded-result (seq (second (reduce (fn [[prev-alph ret] sub-hash]
                        (let [buf (str lottery salt prev-alph)
                              alph (consistent-shuffle prev-alph (subs buf 0 (count prev-alph)))]
                          [alph (conj ret (dehash sub-hash alph))]))
                      [alphabet []]
                      arr)))]
    (if (= encstr
           (encode opts decoded-result))
      decoded-result
      '())))
