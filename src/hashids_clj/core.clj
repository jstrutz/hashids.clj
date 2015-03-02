(ns hashids-clj.core
  (:require [hashids-clj.impl :as impl]))


(defn encode
  ([salt nums]
   (impl/encode {:salt salt} (flatten (list nums))))
  ;; TODO Add precondition to ensure all nums are integers
)

(defn decode
  [str])

