(ns wj.views.common
  (:require [noir.cljs.core :as cljs])
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css html5]]
        [noir.request]
        [hiccup.page]
        [hiccup.element]))


(def tab-links [{:url "/" :text "Nebula Core" :pattern #"^/$"}
                {:url "/qog/start" :text "QOG" :pattern #"^/qog"}
                {:url "/about" :text "About" :pattern #"^/about"}
                ]
  )

(defpartial link-item [{:keys [url cls text]}]
  [:li
   (link-to {:class cls} url text)
   ]
  )

(defn make-active-links [links]
  (let [path (:uri (ring-request))] 
    (map #(if (re-find (:pattern %1) path) (assoc %1 :cls "active") %1) links)))

(defpartial layout [& content]
  (html5
   [:head
    [:title "The Nebula"]
    (include-css "/css/reset.css")]
   [:body
    [:div#tabs [:ul [:li.nultab "&nbsp;"] (map link-item (make-active-links tab-links)) [:li.nultablast "&nbsp;"]]
     [:div#navclose "&nbsp"]
     ]
    
    [:div#wrapper
     content]
    ]))


