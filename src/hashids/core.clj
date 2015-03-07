(ns hashids.core
  (:require [hashids.impl :as impl]))


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
