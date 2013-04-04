(ns wj.client.commands
  (:use [clojure.string :only [lower-case split join]]
        [wj.client.world :only [world location set-location inv invrm search-rinv search-inv get-item-description get-inventory-descriptions invadd take-item-from-world zap-item-from-world move-item give-item-to-world rm-obj-from-world robj-contains? set-door-open door-closed? riddle-unanswered? set-riddle-answered change-room-des]]
        [wj.client.prn :only [pntln]])
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;illegal moves
(defn illegal-move? [con]
	(cond (nil? con) "You can't go that way."
		(and (= con :yard) (not (contains? inv :lit_lantern))) "It's too dark to go there."
		(and (= location :cave_door) (= con :cave) (door-closed? :door_to_cave)) "The door is locked."
		(and (= location :d_room_1) (= con :crossroads) (door-closed? :door_to_crossroads)) "The door is locked."
		(and (= location :clock_room) (= con :silver_key_room) (door-closed? :door_to_silver_key_room)) "The door is locked."
		(and (= con :outside) (robj-contains? :yard :dog)) "The dog growls and blocks your path."
		(and (= con :mird_hillb) (robj-contains? :mird :mird)) "The monkey-bird monster makes a strange monkey-squawk noise and blocks the way."
		(and (= location :sphinx) (= con :l_en) (riddle-unanswered? :sphinx)) "The Sphinx says \"Answer the riddle, and then you may pass!\""
		(and (= location :pword_room) (= con :white_pebble_room) (riddle-unanswered? :pword_room)) "The door is locked."
		(and (= location :mine_room_1) (= con :lyre_room)) (do (set-location :mineshaft_bottom) (if (contains? inv :zegg) (do (invrm :zegg) "As you walk into the room, you get hit by something very heavy. When you wake up, you have a grape sized lump on the back of your head and you feel like you are missing something...") "As you walk into the room, you get hit by something very heavy. When you wake up, you have a grape sized lump on the back of your head."))
		(= con :forest) (do (set-location :forest) "You manage to bushwack your way through the dense forest with only a small amount of hardship.")
		(and (= location :crystal_room) (= con :overlook_ladder) (door-closed? :door_to_overlook_ladder)) "You can't go that way."
		(and (= con :mineshaft_overlook_2) (= location :overlook_ladder)) (do (set-location :mineshaft_overlook_2) "As you start to climb the ladder, a swift wind shoots you up the tube and out into a cavern filled with mining instruments. You land on a metal platform.")
		(and (= location :cath_stransc) (= con :cath_crypt_web) (door-closed? :door_to_cath_crypt_web)) "The trapdoor is locked."
		(and (= con :flooded_room_1) (= location :mine_room_1)) (do (set-location :flooded_room_1) "You fall through the hole and into shallow water.")
		(and (= con :mird_hillb) (= location :mird)) (do (set-location :mird_hillb) "You step out the massive doorway and immediately trip over a rock and tumble down a steep grassy slope.")
		(and (= con :bee_hall) (= location :crossroads)) (do (set-location :bee_hall) "As soon as you step into the Northern passageway, a huge stone slab smashes down behind you sealing the way back.")
		(and (= con :mineshaft_top) (= location :crossroads)) (do (set-location :mineshaft_top) "As soon as you step into the South-leading passageway, a huge stone slab crashes down behind you sealing the way back.")
		true false))

			
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;declare functions in order to fix circular dependencys.
(declare find-command)
(declare move)
(declare do-get-item)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;commands
(def commands
	(array-map
		
	:n {:name "n" :helptext "Description: used to travel to the North\nUsage: n" :fn (fn [_ _] (move "n"))}
	:s {:name "s" :helptext "Description: used to travel to the South\nUsage: s" :fn (fn [_ _] (move "s"))}
	:e {:name "e" :helptext "Description: used to travel to the East\nUsage: e" :fn (fn [_ _] (move "e"))}
	:w {:name "w" :helptext "Description: used to travel to the West\nUsage: w" :fn (fn [_ _] (move "w"))}
	:u {:name "u" :helptext "Description: used to go up\nUsage: u" :fn (fn [_ _] (move "u"))}
	:d {:name "d" :helptext "Description: used to go down\nUsage: d" :fn (fn [_ _] (move "d"))}
	
	:get {
		:name "get"
		:helptext "Description: used to pick up items\nUsage: get <item>"
	   	:fn (fn [item-str input] 
			(let [room (location world)
				  item (search-rinv item-str room)]
				(cond (nil? item) (pntln "You can't do that.")
					  (= item :_unclear_) (pntln (str "Which " item-str "? Please be more specific."))
					  (and (= location :zegg_room) (= item :zegg)) (do 
																   (do-get-item item)
																   (pntln "The floor opens up from under you and you fall into a pit!")
																   (set-location :zegg_pit))
					  true (do-get-item item)
						   )))
		}
		
	:put {
		:name "put"
		:helptext "Description: used to put down items\nUsage: put <item>"
		:fn (fn [item-str input]
				(let [item (search-inv item-str)]
					(cond (nil? item) (pntln (str "You don't have a " item-str"."))
					      (= item :_unclear_) (pntln (str "Which " item-str "? Please be more specific."))
						  true (do
									(pntln (str "You put down " (get-item-description item inv) "."))
									(give-item-to-world location item)
									(if (and (= location :yard ) (= item :meat) (robj-contains? :yard :dog))
										(do
											(pntln "The dog gobbles up the meat and runs off into the bushes")
											(zap-item-from-world :yard :meat)
											(rm-obj-from-world :yard :dog)))
									(if (and (= location :mird ) (robj-contains? :mird :mird) (or (= item :gold_bar) (= item :zegg) (= item :gold_key) (= item :crystal_key) (= item :silver_key) (= item :coin_bag)))
										(do
											(pntln "The monster grabs your offering in its beak and gingerly sets it on its pile of treasure. It then lies down in its nest and watches you, non-threateningly.")
											(zap-item-from-world :mird item)
											(change-room-des :mird "You are in a expansive cave with a floor covered with sticks and even a few bones. In the center of the room is a giant nest occupied by a resting bird-monkey monster. It is looking at you non-threateningly. In the nest is a huge hoard of riches, gold, and jewels. There is a hallway to the North, and to the South there is a massive doorway through which you can see green grass and sunlight.")
											(rm-obj-from-world :mird :mird)))
									(if (= location :mineshaft_mid)
										(do
											(pntln "The object slips from your hand and falls down into the mineshaft. You here a echo come up the mineshaft as the item hits the bottom.")
											(move-item :mineshaft_mid :mineshaft_bottom item)))
									(if (= location :mineshaft_overlook)
										(do
											(pntln "The object slips from your hand and falls down, out of sight.")
											(zap-item-from-world :mineshaft_overlook item)))
									(if (= location :mineshaft_overlook_2)
										(do
											(pntln "The object slips from your hand and falls down, out of sight.")
											(zap-item-from-world :mineshaft_overlook_2 item)))

									(if (= location :pit_room )
										(do
											(pntln "The object slips from your hand and tumbles into the black abyss below you.")
											(zap-item-from-world :pit_room item)))
									(if (= location :tree )
										(do
											(pntln "The object slips from your hand and tumbles down into the grass below the tree.")
											(move-item :tree :outside item)))
									(if (and (= location :crystal_room) (= item :crystal))
										(do
											(pntln "As you place the crystal into the contraption, It starts to glow, and red light starts to be drawn from the crystal, through the wires and tubes, and into the walls. You hear the sound of machinery starting up.")
											(zap-item-from-world :crystal_room :crystal)
											(set-door-open :door_to_overlook_ladder "A doorway opens in the stone of the North wall of the room.")
											(change-room-des :crystal_room "You find yourself in a large square room. A strange contraption stands in the center of the room. It has wires and tubes all running into the walls away from a glowing, crimson crystal about the size of your fist. Red light is being drawn from the crystal, through the wires and tubes, and into the walls. A hallway leads East, and there is a doorway to the North.")
											(change-room-des :mineshaft_overlook "You are on a long viewing area looking over a massive cavern filled with a complex of chutes, minecart tracks, and metal catwalks. A few minecarts, piled with gold ore, zip along a track, powered by a red glow that seems to pull them along. Machines are chugging, engines whirring and the far off sound of pickaxes can be heard. The viewing are continues to the East, and there is a tunnel to the South.")
											(change-room-des :mineshaft_elevator "You are inside a unsteady, rusted elevator cage. Above you there is a system of pulleys and cables that suspend the elevator from the ceiling. There is no obvious way to control the elevator, except a tiny, red keyhole with the words \"In case of emergency\" enscribed below it. The keyhole is glowing with red light. There is an exit to the West.")))

					))))}
					
	:inv {
		:name "inv"
		:helptext "Description: used to display the items you are carrying\nUsage: inv"
		:fn (fn [_ _]
			(if (empty? inv) (pntln "You are empty handed.")
							 (pntln (str "You have:\n" (get-inventory-descriptions inv) "."))))}
							
	:unlock {
		:name "unlock"
		:helptext "Description: used to unlock doors with keys or other items\nUsage: unlock <target>"
		:fn (fn [p _]
			(if (not (or (= p "elevator") (= p "door") (= p "lock") (= p "trapdoor"))) (pntln "You can't unlock that.")
							 (cond
								(and (= location :cave_door) (contains? inv :copper_key)) (set-door-open :door_to_cave "The door unlocks with a click.")
								(and (= location :clock_room) (contains? inv (and :black_pebble :gray_pebble :white_pebble))) (do (set-door-open :door_to_silver_key_room "The pebbles fly out of your hand into the holes, and roll smoothly down into the depths of the door. The door swings open.") (invrm :black_pebble) (invrm :gray_pebble) (invrm :white_pebble))
								(and (= location :d_room_1) (contains? inv :silver_key)) (set-door-open :door_to_crossroads "The door unlocks smoothly.")
								(and (= location :cath_stransc) (contains? inv :iron_key)) (set-door-open :door_to_cath_crypt_web "The heavy trapdoor clicks unlocked.")
								(and (= location :mineshaft_elevator) (contains? inv :crystal_key)) (do (pntln "As you turn the key in the lock, the cables supporting the elevator cage snap and you start to plummet down to the bottom of the elevator shaft. Just when you think that you are about to hit the bottom and be turned into a adventurer pancake breakfast for the nearest monster, there is a blinding flash of red light, and you feel yourself being teleported.") (set-location :outside_elevator))
								(or (= location :cave_door) (= location :mineshaft_elevator) (= location :d_room_1) (= location :cath_stransc)) (pntln "You do not have the correct key.")
								(= location :clock_room) (pntln "You do not have the correct items.")
								(not (or (= location :cave_door) (= location :mineshaft_elevator) (= location :clock_room) (= location :d_room_1) (= location :cath_stransc))) (pntln "There is no locked door here.")
							 )
							))}
							
	:say {
		:name "say"
		:helptext "Description: used to talk to in game\nUsage: say <your text here>"
		:fn (fn [_ input]
			(cond (= location :sphinx) (if (riddle-unanswered? :sphinx) (if (re-find #"(night|day).+(night|day)" (lower-case input))
					(do (pntln "The Sphinx says \"Correct, you may pass!\" Strangely, it then yawns and goes to sleep.")
						(set-riddle-answered :sphinx)
						(change-room-des :sphinx "You are in a dim hallway. In front of you lies a sleeping Sphinx. It is snoring heavily. Behind the Sphinx, the rest of the hallway is hard too see because of a blinding light."))
					(pntln "The Sphinx says \"That is not the answer. Try again.\""))
					(pntln "The Sphinx stirs, and mumbles in its sleep."))
				(= location :pword_room) (if (re-find #"sir" (lower-case input))
					(do (pntln "The room shakes violently and the door slides open.")
						(set-riddle-answered :pword_room))
					(pntln "The room shakes slightly, but the door does not open."))
				(and (= location :cath_crypt_main) (re-find #"romeo|juliet" (lower-case input)))
					(pntln "Nice try.")
				true (pntln "talking to one's self is a sign of impending mental collapse.")
			))
		}
		
	:read {
		:name "read"
		:helptext "Description: used to read items\nUsage: read <item>"
		:fn (fn [p _]
			(let [have-journal (contains? inv :journal)]
			  (cond (and (= p "journal") have-journal (not(= location :study))) (pntln "You open the journal to find that age has worn the already faint marks from the page. You can only make out some of the words and letters; the rest are smudged or faded beyond recognition. You read from the last entry:\n\"M y 12, 174 A. .E. \nI f ar that t ey h  e disc     d our    in  plac . T   Ojer n Gem  ald i  ot saf  here. My fa  e  asu es m  that t   ke  is h  den, an   e wil   e s  e. I am n t so   rtan. Tom r w  e  will relo  te the    eral  t  a s     po  ti  . It will b  v ry dan    us.\nI l  e  n fe r.\nTh y a e comi g.\"")
					(and (= p "journal") have-journal (= location :study)) (pntln "The journal emits a green glow from the pages and the letters are reformed by green glowing lines. The passage reads:\n\"May 12, 174 A.C.E. \nI fear that they have discovered our hiding place. The Ojeran Gemerald is not safe here. My father asures me that the key is hidden, and we will be safe. I am not so certan. Tomorow we will relocate the Gemerald to a safer position. It will be very dangerous.\nI live in fear.\nThey are coming.\"")
					(and (contains? inv :hint_note) (re-find (get (get inv :hint_note) :regex) p)) (pntln "The paper says:\n\"To open the door, three stones are required.\nNot things of value, just ordinary rocks.\nThe door will open, revealing a key,\nTo help you go on in your adventures.\"")
					(= p "") (pntln "What would you like to read?")
					true (pntln (str "You can't do that."))
				))
			)}
	:light {
	:name "light"
	:helptext "Description: used to light items (like lanterns) with a match\nUsage: light <item>"
	:fn (fn [p _]
		(let [item-name (keyword p)]
			(cond (= p "") (pntln "What would you like to light?")
				  (not (contains? inv item-name)) (pntln (str "You don't have a " p "."))
				  (not (contains? inv :match)) (pntln "You need a match to light things.")
				  (not (= item-name :lantern)) (pntln (str "You can't light a " p "."))
				  true (do (invrm :match)
						   (invrm item-name)
						   (invadd :lit_lantern {:des "a lit lantern" :regex #"lit lantern|lit|lantern"})
						   (pntln (str "You have lit a " p))))))}

	:dev {
		:name "dev"
		:helptext "Description: This is definitely NOT an all powerful developer command\nUsage: ERROR clojure.lang.RuntimeException: compiling:(NO_SOURCE_PATH:1)"
		:fn (fn [p _]
			(let [[command param] (split p #" ")]
			(cond
				(= command "goto") (set-location (keyword param))
				true (pntln (str "\"" p "\"" " is not a dev command"))
				))
			
			)}
						
	:help {
		:name "help"
		:helptext "Description: used to display the help menu\nUsage: help OR help <command>"
		:fn (fn [p _]
			(let [command (find-command p)]
			(cond
				(= p "") (pntln (str "commands:\n" (join "\n" (map (fn [[key val]] (get val :name)) (dissoc commands :dev)))))
				(not (= command nil)) (pntln (get command :helptext))
				true (pntln (str "\"" p "\"" " is not a command"))
				)
			))
			}
		
		
			
;	:quit {
;		:name "quit"
;		:helptext "Description: used to exit out of the game\nUsage: quit"
;		:fn (fn [_ _]
;			(set-not-done false)
;			)}
		
	)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;location and movement
(defn move [direction]
	(let [room (location world)
		  con ((keyword direction) (:con room))]
		(let [error (illegal-move? con)]
			(if error 
				(pntln error)
				(set-location con)
			))))

;random answer
(defn random-answer [possible-answers]
	(let [random-number (int (rand (count possible-answers)))]
		(get possible-answers random-number)
	))

;code for getting items
(defn do-get-item [item]
	(take-item-from-world location item)
  	   (pntln (str "You now have " (get-item-description item inv) ".")))

;finding command keys
(defn find-command-key [command-str]
	(some (fn [[key val]] (if (= command-str (get val :name)) key nil)) commands))

;finding command 
(defn find-command [command-str]
	(let [command-key (find-command-key command-str)]
		(if (nil? command-key) nil (get commands command-key))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn do-commands [input]
	(let [
	  input-lower (lower-case input)
	  x (split input-lower #" ")
	  c (first x)
	  p (join " " (rest x))
	  room (location world)
	  command (find-command c)
	]
	(if (nil? command)
		(pntln (random-answer 
					(let [x (str "What is this \"" input "\" of which you speak?")]
						[x x x (str "What do you mean, \"" input "\"?") (str "I don't know what \"" input "\" means.")]
					)))
		
		((get command :fn) p input)
	)
))
