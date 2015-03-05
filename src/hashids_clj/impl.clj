(ns hashids-clj.impl
  (:require [hashids-clj.util :refer :all]))

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
   (if (zero? input)
     (subs alphabet 0 1)
     (if (zero? n)
       res
       (recur input
              alphabet
              (int (/ n (count alphabet)))
              (str (nth (cycle alphabet) n) res))))))

(defn dehash
  ;; Known as 'unhash' in other implementations
  [input alphabet]
  (reduce + (map-indexed
             (fn [idx c]
               (let [pos (first (positions #{c} alphabet))]
                 (* pos (int (expt (count alphabet)
                                   (- (count input) idx 1))))))
             (vec input))))

(defn encode-numbers
  [seps alphabet salt hash_int numbers]
  (let [lottery  (str (nth alphabet (mod hash_int (count alphabet))))
        sepsc    (cycle seps)]
    (second
     (reduce (fn [[alph ret] [idx n]]
               (let [buf     (concat lottery salt alph)
                     alph    (consistent-shuffle (vec alph) (take (count alph) buf))
                     encchar (enhash n alph)
                     addsep  (nth sepsc (mod n (+ (int (first encchar)) idx)))]
                 (if (< (+ idx 1) (count numbers))
                   [alph (str ret encchar addsep)]
                   [alph (str ret encchar)])))
             [alphabet lottery] ;; reduce by passing along alphabet, which is transformed in each iteration
             (map-indexed vector numbers)))))

(defn add-guards
  [min-length guards hash_int hashstr]
  (let [prepend-guard #(if (< (count %) min-length)
                         (str (nth (cycle guards)
                                   (+ hash_int (int (nth % 0))))
                              %)
                         %)
        append-guard #(if (< (count %) min-length)
                        (str %
                             (nth (cycle guards)
                                  (+ hash_int (int (nth % 2)))))
                        %)]
    (->> hashstr
         prepend-guard
         append-guard)))

(defn ensure-min-length
  [min-length alphabet hashstr]
  (let [half_length (int (/ (count alphabet) 2))
        upsize (fn [[alph ret]]
                 (let [alph (consistent-shuffle alph alph)
                       rplusalph (str (subs alph half_length) ret (subs alph 0 half_length))
                       excess (- (count rplusalph) min-length)]
                   (if (> excess 0)
                     [alph (subs rplusalph (int (/ excess 2)) min-length)]
                     [alph rplusalph])))]
    (second (first (drop-while #(< (count (second %)) min-length)
                               (iterate upsize [alphabet hashstr]))))))


(defn balance-seps
  "Balance alphabet and seps, the ratio of sizes of which should SEP_DIV"
  [seps alph]
  (let [seps-length (max 2 (ceil (/ (count alph) SEP_DIV)))
        seps-diff (- seps-length (count seps))]
    (if (or (zero? (count seps))
            (> (/ (count alph)
                  (count seps))
               SEP_DIV))
      (if (> seps-length (count seps))
        (vec (map clojure.string/join (split-at 2 (str seps alph))))
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
  (let [alph-unbal (->> (chars-difference alphabet seps)
                        distinct
                        strip-whitespace)
        seps-unbal (->> (chars-intersection alphabet seps)
                        distinct
                        strip-whitespace)
        [seps alph] (balance-seps (consistent-shuffle seps-unbal salt) alph-unbal)]
    (assoc (extract-guards (consistent-shuffle alph salt) seps)
      :min-length min-length
      :salt salt))))

(defn encode
  [opts numbers]
  (let [{:keys [seps alphabet salt min-length guards]} (setup opts)
        hash_int (reduce + (map-indexed (fn [idx num] (mod num (+ idx 100))) numbers))]

  (->> numbers
       (encode-numbers seps alphabet salt hash_int)
       (add-guards min-length guards hash_int)
       (ensure-min-length min-length alphabet))))

(defn decode
  [opts encstr]
  (let [{:keys [seps alphabet salt min-length guards]} (setup opts)
        breakdown (split-on-chars encstr guards)
        breakdown-idx (if (some #{(count breakdown)} '(2 3)) 1 0)
        bdn (nth breakdown breakdown-idx)
        lottery (first bdn)
        arr (split-on-chars (drop 1 bdn) seps)]
    (seq (second (reduce (fn [[prev-alph ret] sub-hash]
              (let [buf (str lottery salt prev-alph)
                    alph (consistent-shuffle prev-alph (subs buf 0 (count prev-alph)))]
                [alph (conj ret (dehash sub-hash alph))]))
            [alphabet []]
            arr)))))

