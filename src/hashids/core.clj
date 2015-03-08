(ns hashids.core
  (:require [hashids.impl :as impl])
  (:require [hashids.util :as util]))


(defn encode
  [opts & nums]
  {:pre [(not-empty nums)
         (every? integer? (flatten (list nums)))
         (not-any? neg? (flatten (list nums)))]}
   (impl/encode opts (flatten (list nums))))

(defn decode
  [opts encstr]
  {:pre [(seq encstr)]}
  (impl/decode opts encstr))

(defn encode-hex
  [opts & hexstrs]
  {:pre [(not-empty hexstrs)
         (every? string? (flatten (list hexstrs)))]}
  (encode opts (map util/hexstr->long (flatten (list hexstrs)))))

(defn decode-hex
  [opts encstr]
  {:pre [(seq encstr)]}
  (map util/long->hexstr (decode opts encstr)))
