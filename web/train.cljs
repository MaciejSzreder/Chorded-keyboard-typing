(ns train (:require
	[ll.gui :as gui]
	[ll.log :refer [log peek spy]]
	[logic :refer [saveStatistics! updateEncoding! hint updateCharacterSet! keyDown! keyUp!]]
))

(do
	(def encodeKey (atom {
		"q" 1
		"w" 2
		"e" 4
		"f" 8
		"c" 16
		"m" 32
		"j" 64
		"i" 128
		"o" 256
		"p" 512
	}))
	(def characterSet (atom "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM-=[]\\;',./`~!@#$%^&*()_+{}|:\"<>?"))
	(def stats (atom {}))
	(def encodedCharacter (atom 0))
	(def inputMode (atom :keyDown))
	(def start (atom nil))

	(def downloadingButton
		(gui/button
			"Save statistics"
			#(saveStatistics! @stats)
		)
	)
	(gui/render downloadingButton)

	(defn createFingerConfigurationInput [finger]
		(let [
				encoded (Math/pow 2 finger)
				input (gui/textField (some (fn[[key code]] (when (= code encoded) key)) @encodeKey) {
					:width :2em,
					:height :2em,
					:text-align :center,
					:font-family :monospace
				} #(this-as this
					(updateEncoding! encodeKey (gui/text this) encoded)
				))
			]
			input
		)
	)

	(def fingers (map createFingerConfigurationInput (range 10)))
	(def keyMapping (gui/container fingers {}))
	(gui/render keyMapping)

	(def toType (gui/inline [@characterSet] {}))
	(def characterSetConfiguration (gui/textField @characterSet {
		:width :100ch,
		:font-family :monospace,
	} #(updateCharacterSet! characterSet (gui/text characterSetConfiguration) toType @stats fingers)
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
			#(keyDown! % encodeKey inputMode encodedCharacter preview),
		:keyup
			#(keyUp! % encodeKey inputMode encodedCharacter preview output toType start stats fingers characterSet)
	})
)