(do
	(def encodeKey {
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
	})
	(def encodedCharacter (atom 0))
	(.addEventListener js/document "keydown" #(
		js/console.log
			"Key down event detected"
			(get encodeKey (.-key %))
			(reset! encodedCharacter (bit-or @encodedCharacter (get encodeKey (.-key %) 0)))
	))
	(.addEventListener js/document "keyup" #(
		js/console.log
			"Key down event detected"
			(get encodeKey (.-key %))
			(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get encodeKey (.-key %) 0))))
	))
)