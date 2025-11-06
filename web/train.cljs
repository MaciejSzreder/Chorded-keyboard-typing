(ns train (:require
	[ll.gui :as gui]
	[ll.log :refer [log peek spy]]
	[state :refer [state]]
	[logic :refer [saveStatistics! updateEncoding! hint updateCharacterSet! keyDown! keyUp!]]
))

(do
	(def downloadingButton
		(gui/button
			"Save statistics"
			#(saveStatistics! @(:stats state))
		)
	)
	(gui/render downloadingButton)

	(defn createFingerConfigurationInput [finger]
		(let [
				encoded (Math/pow 2 finger)
				input (gui/textField (some (fn[[key code]] (when (= code encoded) key)) @(:encodeKey state)) {
					:width :2em,
					:height :2em,
					:text-align :center,
					:font-family :monospace
				} #(this-as this
					(updateEncoding! state (gui/text this) encoded)
				))
			]
			input
		)
	)

	(def fingers (map createFingerConfigurationInput (range 10)))
	(def keyMapping (gui/container fingers {}))
	(gui/render keyMapping)

	(def toType (gui/inline [@(:characterSet state)] {}))
	(def characterSetConfiguration (gui/textField @(:characterSet state) {
		:width :100ch,
		:font-family :monospace,
	} #(updateCharacterSet! (:characterSet state) (gui/text characterSetConfiguration) toType @(:stats state) fingers)
	))
	(gui/render characterSetConfiguration)

	(def output (gui/inline [] {}))
	(def preview (gui/inline [] {:color :red}))
	(def workspace (gui/container [output, preview, toType] {
		:font-size :2em,
		:font-family :monospace,
		:word-break :break-word
	}))
	(gui/render workspace)

	(hint fingers (subs (gui/text toType) 0 1))

	(gui/registerListeners {
		:keydown
			#(keyDown! % (:encodeKey state) (:inputMode state) (:encodedCharacter state) preview),
		:keyup
			#(keyUp! % (:encodeKey state) (:inputMode state) (:encodedCharacter state) preview output toType (:start state) (:stats state) fingers (:characterSet state))
	})
)