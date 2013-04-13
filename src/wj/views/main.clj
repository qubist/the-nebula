(ns wj.views.main
  (:require [wj.views.common :as common]
            [noir.cljs.core :as cljs]
			[noir.validation :as vali]
			)
  (:use [noir.core :only [defpage defpartial render]]
        [hiccup.core :only [html]]
		[hiccup.form]))

(defpage "/" []
         (common/layout
           [:p.last "Welcome to " [:b "The Nebula"]", Will Harris-Braun's website."]
		   [:p.last "(It's not quite done yet. Don't tell anyone!)"]
))

(defpage "/qog/start" []
         (common/layout
		   [:p.last [:b [:i "Quest for the Ojeran Gemerald"]] " is a text-based, role-playing, adventure game. You can play it if you like! It will have background music soon."]
		   [:p [:b "Instructions:"]]
		   [:p "In the game, you use text commands to move around and interact with the world. For example, use the command \"n\" to move North, \"u\" to move up, etc."]
		   [:p.last "Using the command \"help\" will give you a list of commands and what they do. If you type \"help\" and then one of the commands on the list, it will give you detailed information on that command"]
		   [:p [:a {:href "/qog/play"} "Play Quest for the Ojeran Gemerald!"]]
))

(defpage "/qog/play" []
         (common/layout
			[:p.last [:b "Quest for the Ojeran Gemerald"]]
			[:p.last ""]
			[:img#loading-img {:src "/art/loading.gif"}]
			(cljs/include-scripts :with-jquery)
			[:div#report-issue [:a {:href "/qog/issues" :target "_blank"} "Report problems! Pretty please?"]]
))

(defpage "/contact" []
         (common/layout
			[:p "ISM"]
))

(defpage "/about" []
         (common/layout
		   [:p [:b "About"]]
		   [:p.last [:b "Will Harris-Braun"] " lives in New York State and sadly, he spends a lot of his time in school. In his free time, he enjoys coding, learning to code, playing computer games, and reading books. Some of his favorite things are macaroni and cheese, weekends, outer space, and people who understand very bad nerd jokes."]
		   [:p [:a {:href "/contact"} "Contact"]]
))

(defpage "/fargegg" []
         (common/layout
		   [:p [:i "!esseJ ,yrt eciN"]]
))

(defn build-option [value Text selection]
	[:option (let [x {:value value}] (if (= selection value) (assoc x :selected true) x)) Text])

(defpartial error-item [[first-error]]
	[:p.error first-error])

(defpartial issue-fields [{:keys [name email issue_type issue]}]
   [:p [:b "Name:"] (vali/on-error :name error-item)]
   [:p.last [:input#name {:name "name" :value name}]]

   [:p [:b "Email:"] (vali/on-error :email error-item)]
   [:p.last [:input#email {:name "email" :value email}]]

   [:p [:b "Issue Type:"] (vali/on-error :issue_type error-item)]		
   [:p.last [:select#issue_type {:name "issue_type"}
					 [:option (let [x {:disabled true}] (if (or (nil? issue_type) (= issue_type "")) (assoc x :selected true) x)) "–Select One–"] 
					 (build-option "bug" "Bug" issue_type)
					 (build-option "feature" "Feature Request" issue_type)
					 (build-option "typo" "Typo" issue_type)
					]]
					
   [:p [:b "Issue:"] (vali/on-error :issue error-item)]
   [:h3 "(Be specific––if the issue is a bug, state where you were, what you did, what happened, and what should have happened. If it is a typo, say where you were, what word was misspelled, and how it should have been spelled. If you are requesting that I add a certain feature, be detailed and clear.)"]
   [:p.last [:textarea#issue {:name "issue"} issue]]

	)

(def req-error-txt "This is a required field")
(defn valid? [{:keys [name email issue_type issue]}]
	(vali/rule (vali/has-value? name)
		[:name req-error-txt])
	(vali/rule (vali/has-value? email)
		[:email req-error-txt])
	(vali/rule (vali/has-value? issue_type)
		[:issue_type req-error-txt])
	(vali/rule (vali/has-value? issue)
		[:issue req-error-txt])
	)

(defpage "/qog/issues" {:as issue}
         (common/layout
		   [:p [:b "Hi,"]]
		   [:p.last "Please report issues here! It really helps me make the game better and more enjoyable for everyone. It can be a pain, but really? Think of all the things I've done for " [:i "you! "] "Plus, every time you report an issue, one thousand kittens are born!"]
		   (form-to [:post "/qog/issues"]
		
		   (issue-fields issue)

		   [:p (submit-button {} "Submit Issue")]
		)
))

(defn send-email []
	true ) ;FIXME
	
(defpage [:post "/qog/issues"] {:as issue}
 		(cond 
			(valid? issue)
			(do
				(spit "issues.txt" (str issue "\n") :append true)
				(send-email)
        		(common/layout
		   			[:p [:b "Thanks!"]]
		   			[:p "Because you submitted this issue, the world is a better place!"]))
			true
			(render "/qog/issues" issue)
		))

(defpage "/qog/admin" []
	(common/layout
		(let [text (clojure.string/split (slurp "issues.txt") #"\n")]
			(into [:div [:p [:b "Name:"] "some name"] [:p [:b "Email:"] "some email"]] (map #(vector :p %1)  text))
				)))