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
	(def encodedCharacter (atom 0))
	(def inputMode (atom :keyDown))

	(defn createFingerConfigurationInput [finger]
		(let [
				input (.createElement js/document "input")
				encoded (Math/pow 2 finger)
			]
			(.setAttribute input "style" "font-size: 1em; font-family: monospace; width:2em; text-align: center")
			(set! (.-value input) (some (fn [[key code]] (when (= code encoded) key)) @encodeKey))
			(.addEventListener input "input" #(do
				(reset! encodeKey (merge
					(zipmap (.-value input) (map (fn [] encoded) (.-value input)))
					(filter (fn[[key code]] (not= code encoded)) @encodeKey)
				))
			))
			input
		)
	)

	(def keyMapping (.createElement js/document "div"))
	(doseq [finger (map createFingerConfigurationInput (range 10))]
		(.appendChild keyMapping finger)
	)
	(.appendChild js/document.body keyMapping)

	(def output (.createElement js/document "span"))
	(.setAttribute output "style" "font-size: 2em; font-family: monospace;")
	(.appendChild js/document.body output)

	(def preview (.createElement js/document "span"))
	(.setAttribute preview "style" "font-size: 2em; font-family: monospace; color: red;")
	(.setAttribute preview "id" "preview")
	(.appendChild js/document.body preview)

	(def toType (.createElement js/document "span"))
	(.setAttribute toType "style" "font-size: 2em; font-family: monospace;")
	(.appendChild js/document.body toType)
	(set! (.-textContent toType) "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:',.<>?/`~")

	(.addEventListener js/document "keydown" #(
		when (contains? @encodeKey (.-key %))
			(reset! inputMode :keyDown)
			(reset! encodedCharacter (bit-or @encodedCharacter (get @encodeKey (.-key %) 0)))
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
		
	))
	(.addEventListener js/document "keyup" #(
		when (contains? @encodeKey (.-key %))
			(if (= @inputMode :keyDown)
				(do
					(js/console.log "first release")
					(set! (.-textContent output) (str (.-textContent output) (.fromCharCode js/String @encodedCharacter)))
					(when (= (.fromCharCode js/String @encodedCharacter) (subs (.-textContent toType) 0 1))
						(set! (.-textContent toType) (subs (.-textContent toType) 1))
					)
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
				)
				(do
					(js/console.log "next release")
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
				)
			)
			(reset! inputMode :keyUp)
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
	))

)