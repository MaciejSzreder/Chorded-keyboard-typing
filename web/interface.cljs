(ns interface (:require
	[ll.gui :as gui]
	[cljs.pprint :refer [char-code]]
	[logic :refer [hint]]
))

(defn downloadingButton [logic]
	(gui/button
		"Save statistics"
		logic
	)
)

(defn createFingerConfigurationInput [finger loadKeyMapping setKeyMapping]
	(let [
			input (gui/textField (loadKeyMapping finger) {
				:width :2em,
				:height :2em,
				:text-align :center,
				:font-family :monospace
			} #(this-as this
				(setKeyMapping (gui/text this) finger)
			))
		]
		input
	)
)

(defn fingers [loadKeyMapping setKeyMapping]
	(map
		#(createFingerConfigurationInput % loadKeyMapping setKeyMapping)
		(range 10)
	)
)
(defn keyMapping [fingers] (gui/container fingers {}))

(defn characterSetConfiguration [loadCharacterSet setCharacterSet]
	(gui/textField (loadCharacterSet) {
		:width :100ch,
		:font-family :monospace,
	} #(this-as this
		(setCharacterSet (gui/text this))
	))
)

(defn toType [loadText] (gui/inline [(loadText)] {}))
(defn output [] (gui/inline [] {}))
(defn preview [] (gui/inline [] {:color :red}))
(defn workspace [output preview toType]
	(gui/container [output preview toType] {
		:font-size :2em,
		:font-family :monospace,
		:word-break :break-word
	})
)

(defn textFieldController [textField]
	(fn
		([] (gui/text textField))
		([newText] (gui/setText! textField newText))
	)
)

(defn fingersController [fingers]
	(fn[finger action] 
		(case action
			:highlight (gui/set! (nth fingers finger) {:background-color :yellow})
			:unhighlight (gui/unset! (nth fingers finger) [:background-color])
		)
	)
)

(defn controller
	([behavior]
		(gui/registerListeners {
			:keydown (:keydown behavior)
			:keyup (:keyup behavior)
		})
		(gui/render (downloadingButton (:downloadStatistics behavior)))
		(let [
			fingers (fingers (:loadKeyMapping behavior) (:setKeyMapping behavior))
			output (output)
			preview (preview)
			toType (toType (:loadText behavior))
		]
			(gui/render (keyMapping fingers))
			(gui/render (characterSetConfiguration (:loadCharacterSet behavior) (:setCharacterSet behavior)))
			(gui/render (workspace output preview toType))
			(let [
				interface {
					:fingers (fingersController fingers)
					:preview (textFieldController preview)
					:output (textFieldController output)
					:toType (textFieldController toType)
				}
				controller (fn[action & args] (apply (action interface) args))
			]
				(hint controller (subs (gui/text toType) 0 1))
				controller
			)
		)
	)
)