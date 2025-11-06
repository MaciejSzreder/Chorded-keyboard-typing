(ns train (:require
	[ll.gui :as gui]
	[ll.log :refer [log peek spy]]
	[state :refer [controller]]
	[logic :refer [saveStatistics! updateEncoding! hint updateCharacterSet! keyDown! keyUp!]]
))

(do
	(def downloadingButton
		(gui/button
			"Save statistics"
			#(saveStatistics! @((controller) :stats))
		)
	)
	(gui/render downloadingButton)

	(defn createFingerConfigurationInput [finger]
		(let [
				encoded (Math/pow 2 finger)
				input (gui/textField (some (fn[[key code]] (when (= code encoded) key)) @((controller) :encodeKey)) {
					:width :2em,
					:height :2em,
					:text-align :center,
					:font-family :monospace
				} #(this-as this
					(updateEncoding! (controller) (gui/text this) encoded)
				))
			]
			input
		)
	)

	(def fingers (map createFingerConfigurationInput (range 10)))
	(def keyMapping (gui/container fingers {}))
	(gui/render keyMapping)

	(def toType (gui/inline [@((controller) :characterSet)] {}))
	(def characterSetConfiguration (gui/textField @((controller) :characterSet) {
		:width :100ch,
		:font-family :monospace,
	} #(updateCharacterSet! (controller) (gui/text characterSetConfiguration) toType fingers)
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
			#(keyDown! % (controller) preview),
		:keyup
			#(keyUp! % (controller) preview output toType fingers)
	})
)