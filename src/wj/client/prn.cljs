(ns wj.client.prn
  (:require [crate.core :as crate] )
  (:use [jayq.core :only [$ append val delegate inner]]
        )
  (:use-macros [crate.def-macros :only [defpartial]]))

(defpartial entry [e]
  [:p e]
)

(def txt [:p ])
(defn pntln [l]
  (def txt (conj txt l [:br]))
  (append $log (entry txt))
  (def txt [:p])
)
(defn pnt [l]
  (def txt (conj txt l))
)

(defn set-log [l]
  (def $log l)
)

