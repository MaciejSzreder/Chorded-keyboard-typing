(ns train)

(do
	(defn spy [x] 
		(console.log x)
		x
	)

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

	(defn addStatistic [char time]
		(reset! stats (assoc @stats char (conj (get @stats char) time)))
	)

	(defn createDownloadingButton []
		(let [button (.createElement js/document "button")]
			(set! (.-textContent button) "Save statistics")
			(.addEventListener button "click" 
				#(let [
					downloader (.createElement js/document "a")
					blob (.createObjectURL js/URL (js/Blob. #js [(.stringify js/JSON (clj->js @stats))] #js {:type "application/json"}))
					]
					(.setAttribute downloader "href" blob)
					(.setAttribute downloader "download" "statistics.json")
					(.click downloader)
					(.revokeObjectURL js/URL blob)
				)
			)
			button
		)
	)
	(.appendChild js/document.body (createDownloadingButton))

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
			(.addEventListener input "keydown" #(.stopPropagation %))
			(.addEventListener input "keyup" #(.stopPropagation %))
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
	(.addEventListener characterSetConfiguration "keydown" #(.stopPropagation %))
	(.addEventListener characterSetConfiguration "keyup" #(.stopPropagation %))
	(set! (.-value characterSetConfiguration) @characterSet)
	(.appendChild js/document.body characterSetConfiguration)

	(defn createWorkspace []
		(let [workspace (.createElement js/document "div")]
			(.setAttribute workspace "style" "word-break: break-word")
			workspace
		)
	)
	(def workspace (createWorkspace))

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

	(defn getRandomNotMeasuredCharacter []
		(rand-nth (filter #(not (contains? @stats %)) @characterSet))
	)
	(defn getRandomMeasuredCharacter []
		(let [
			measured (filter #(contains? @stats %) @characterSet)
			measurements (zipmap measured (map #(rand-nth (get @stats %)) measured))
			r (* (rand) (reduce + (vals measurements)))
			]
			(reduce
				(fn[acc [char time]]
					(let [c (+ acc time)]
						(if (< c r)
							c
							(reduced char)
						)
					)
				)
				0
				measurements
			)
		)
	)
	(defn getRandomCharacter []
		(if (< (rand) 0.5)
			(if (every? (fn[char] (contains? @stats char)) @characterSet) ;; every character has been measured
				(getRandomMeasuredCharacter)
				(getRandomNotMeasuredCharacter)
			)
			(if (empty? @stats)
				(getRandomNotMeasuredCharacter)
				(getRandomMeasuredCharacter)
			)
		)
	)
	
	(.addEventListener characterSetConfiguration "input" #(do
		(reset! characterSet (.-value characterSetConfiguration))
		(set! (.-textContent toType) "")
		(while (< (.-length (.-textContent toType)) 20)
			(set! (.-textContent toType) (str (.-textContent toType) (getRandomCharacter)))
		)
		(hint (subs (.-textContent toType) 0 1))
	))

	(.addEventListener js/document "keydown" #(
		when (contains? @encodeKey (.-key %))
			(reset! inputMode :keyDown)
			(reset! encodedCharacter (bit-or @encodedCharacter (get @encodeKey (.-key %) 0)))
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
		
	) false)
	(.addEventListener js/document "keyup" #(
		when (contains? @encodeKey (.-key %))
			(if (= @inputMode :keyDown)
				(do
					(js/console.log "first release")
					(set! (.-textContent output) (str (.-textContent output) (.fromCharCode js/String @encodedCharacter)))
					(when (= (.fromCharCode js/String @encodedCharacter) (subs (.-textContent toType) 0 1))
						(set! (.-textContent toType) (subs (.-textContent toType) 1))
						(let [end (.now js/Date)]
							(when @start
								(addStatistic (.fromCharCode js/String @encodedCharacter) (- end @start))
								(js/console.log "added measurement" (.fromCharCode js/String @encodedCharacter) (- end @start))
							)
							(reset! start end)
						)
					)
					(hint (subs (.-textContent toType) 0 1))
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
					(while (< (.-length (.-textContent toType)) 20)
						(set! (.-textContent toType) (str (.-textContent toType) (getRandomCharacter)))
					)
				)
				(do
					(js/console.log "next release")
					(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey (.-key %) 0))))
				)
			)
			(reset! inputMode :keyUp)
			(set! (.-textContent preview) (.fromCharCode js/String @encodedCharacter))
	) false)

	(.appendChild js/document.body workspace)
)