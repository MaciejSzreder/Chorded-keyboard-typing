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
	(def inputMode (atom :keyDown))
	(.addEventListener js/document "keydown" #(do
		(reset! inputMode :keyDown)
		(.log js/console
			"preview"
			(reset! encodedCharacter (bit-or @encodedCharacter (get encodeKey (.-key %) 0)))
			(.fromCharCode js/String @encodedCharacter)
		)
	))
	(.addEventListener js/document "keyup" #(do
		(reset! inputMode :keyDown)
		if (= @inputMode :keyUp)
			(js/console.log
				"output"
				(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get encodeKey (.-key %) 0))))
				(.fromCharCode js/String @encodedCharacter)
			)
			(js/console.log
				"preview"
				(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get encodeKey (.-key %) 0))))
				(.fromCharCode js/String @encodedCharacter)
			)

	))
)