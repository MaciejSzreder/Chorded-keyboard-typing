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
	(def fingers (map createFingerConfigurationInput (range 10)))
	(doseq [finger fingers]
		(.appendChild keyMapping finger)
	)
	(.appendChild js/document.body keyMapping)
	
	(defn highlightFinger [n]
		(.setProperty (.-style (nth fingers n)) "background-color" "yellow")
	)
	(defn unhighlightFinger [n]
		(.removeProperty (.-style (nth fingers n)) "background-color")
	)
	(defn hint [char]
		(dotimes [finger 10]
			(if (not= 0 (bit-and (Math.pow 2 finger) (.charCodeAt char 0)))
				(highlightFinger finger)
				(unhighlightFinger finger)
			)
		)
	)

	(def characterSetConfiguration (.createElement js/document "input"))
	(set! (.-value characterSetConfiguration) @characterSet)
	(.appendChild js/document.body characterSetConfiguration)

	(def workspace (.createElement js/document "div"))

	(def output (.createElement js/document "span"))
	(.setAttribute output "style" "font-size: 2em; font-family: monospace;")
	(.appendChild workspace output)

	(def preview (.createElement js/document "span"))
	(.setAttribute preview "style" "font-size: 2em; font-family: monospace; color: red;")
	(.setAttribute preview "id" "preview")
	(.appendChild workspace preview)

	(def toType (.createElement js/document "span"))
	(.setAttribute toType "style" "font-size: 2em; font-family: monospace;")
	(.appendChild workspace toType)
	(set! (.-textContent toType) @characterSet)
	(hint (subs (.-textContent toType) 0 1))
	
	(.addEventListener characterSetConfiguration "input" #(do
		(reset! characterSet (.-value characterSetConfiguration))
		(set! (.-textContent toType) "")
		(while (< (.-length (.-textContent toType)) 20)
			(set! (.-textContent toType) (str (.-textContent toType) (nth @characterSet (rand (.-length @characterSet)))))
		)
		(hint (subs (.-textContent toType) 0 1))
	))

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
					(hint (subs (.-textContent toType) 0 1))
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
					(while (< (.-length (.-textContent toType)) 20)
						(set! (.-textContent toType) (str (.-textContent toType) (nth @characterSet (rand (.-length @characterSet)))))
					)
				)
				(do
					(js/console.log "next release")
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
				)
			)
			(reset! inputMode :keyUp)
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
	))

	(.appendChild js/document.body workspace)
)