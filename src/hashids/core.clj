(ns hashids.core
  (:require [hashids.impl :as impl]))


(defn encode
  ([opts nums]
   (impl/encode opts (flatten (list nums))))
  ;; TODO Add precondition to ensure all nums are integers
)

(defn decode
  ([opts encstr]
  (impl/decode opts encstr)))
