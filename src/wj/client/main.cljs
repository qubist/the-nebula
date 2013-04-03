(ns wj.client.main
  (:require [noir.cljs.client.watcher :as watcher]
            [clojure.browser.repl :as repl]
            [crate.core :as crate]
            [wj.client.commands :as commands]
			[clojure.string :as str]

)
  (:use [jayq.core :only [$ append val delegate inner bind hide]]
        [wj.client.world :only [initialize-inventory initialize-world location world print-items-in-room]]
        [wj.client.prn :only [pnt pntln set-log]]
        )
  (:use-macros [crate.def-macros :only [defpartial]])
)

;;************************************************
;; Dev stuff
;;************************************************

;(watcher/init)
;(repl/connect "http://localhost:9000/repl")

;;************************************************
;; Code
;;************************************************

(def $content ($ :#wrapper))
(defn des [] (:des (location world)))

(defpartial x []
  [:div [:div#log] [:input#command {:type "text"}]])
(append $content (x))
(def $body ($ :body))
(def $cmd ($ :#command))
(def $log ($ :#log))

(set-log $log)

(defn handle-command []
  (-> $log (inner ""))
  (pnt "> ")
  (let [cmd (-> $cmd (val))]
    (pntln cmd)
    (commands/do-commands cmd))
	  (pntln (des))
  (print-items-in-room)
  (-> $cmd (val ""))
)

(bind $cmd "keyup"
          (fn [e] (if (= 13 (aget e "which"))
                    (handle-command))))

;initializeation
(defn initialize []
	(initialize-inventory)
	(initialize-world)
)

(initialize)
(hide ($ :#loading-img))
(pntln (des))
