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
  (def txt (apply conj txt (reverse (rest (reverse (apply concat (map #(conj [%1] [:br]) (clojure.string/split l #"\n"))))))))
  (append $log (entry txt))
  (def txt [:p])
)
(defn pnt [l]
  (def txt (conj txt l))
)

(defn set-log [l]
  (def $log l)
)

