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

	(def output (.createElement js/document "span"))
	(.setAttribute output "style" "font-size: 2em; font-family: monospace;")
	(.appendChild js/document.body output)

	(def preview (.createElement js/document "span"))
	(.setAttribute preview "style" "font-size: 2em; font-family: monospace;")
	(.appendChild js/document.body preview)

	(.addEventListener js/document "keydown" #(
		when (contains? encodeKey (.-key %))
			(reset! inputMode :keyDown)
			(reset! encodedCharacter (bit-or @encodedCharacter (get encodeKey (.-key %) 0)))
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
		
	))
	(.addEventListener js/document "keyup" #(
		when (contains? encodeKey (.-key %))
			(if (= @inputMode :keyDown)
				(do
					(set! (.-textContent output) (str (.-textContent output) (.fromCharCode js/String @encodedCharacter)))
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get encodeKey (.-key %) 0))))
				)
				(do
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get encodeKey (.-key %) 0))))
					(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
				)
			)
			(reset! inputMode :keyUp)
	))

)