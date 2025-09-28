(ns train
	(:require [ll.gui :as gui])
)

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

	(def downloadingButton
		(gui/button
			"Save statistics"
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
					(reset! encodeKey (merge
						(zipmap (gui/text this) (map (fn [] encoded) (gui/text this)))
						(filter (fn[[key code]] (not= code encoded)) @encodeKey)
					))
				))
			]
			input
		)
	)

	(def fingers (map createFingerConfigurationInput (range 10)))
	(def keyMapping (gui/container fingers {}))
	(gui/render keyMapping)
	
	(defn highlightFinger [n]
		(gui/set! (nth fingers n) {:background-color :yellow})
	)
	(defn unhighlightFinger [n]
		(gui/unset! (nth fingers n) [:background-color])
	)
	(defn hint [char]
		(dotimes [finger 10]
			(if (not= 0 (bit-and (Math.pow 2 finger) (.charCodeAt char 0)))
				(highlightFinger finger)
				(unhighlightFinger finger)
			)
		)
	)

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

	(def toType (gui/inline [@characterSet] {}))
	(def characterSetConfiguration (gui/textField @characterSet {
		:width :100ch,
		:font-family :monospace,
	} #(do
		(reset! characterSet (gui/text characterSetConfiguration))
		(gui/setText! toType "")
		(while (< (.-length (gui/text toType)) 20)
			(gui/setText! toType (str (gui/text toType) (getRandomCharacter)))
		)
		(hint (subs (gui/text toType) 0 1))
	)))
	(gui/render characterSetConfiguration)

	(def output (gui/inline [] {}))
	(def preview (gui/inline [] {:color :red}))
	(def workspace (gui/container [output, preview, toType] {
		:font-size :2em,
		:font-family :monospace,
		:word-break :break-word
	}))
	(gui/render workspace)

	(hint (subs (gui/text toType) 0 1))

	(gui/registerListeners {
		:keydown
			#(when (contains? @encodeKey %)
				(reset! inputMode :keyDown)
				(reset! encodedCharacter (bit-or @encodedCharacter (get @encodeKey % 0)))
				(gui/setText! preview (.fromCharCode js/String @encodedCharacter))	
			),
		:keyup
			#(when (contains? @encodeKey %)
				(if (= @inputMode :keyDown)
					(do
						(js/console.log "first release")
						(gui/setText! output (str (gui/text output) (.fromCharCode js/String @encodedCharacter)))
						(when (= (.fromCharCode js/String @encodedCharacter) (subs (gui/text toType) 0 1))
							(gui/setText! toType (subs (gui/text toType) 1))
							(let [end (.now js/Date)]
								(when @start
									(addStatistic (.fromCharCode js/String @encodedCharacter) (- end @start))
									(js/console.log "added measurement" (.fromCharCode js/String @encodedCharacter) (- end @start))
								)
								(reset! start end)
							)
						)
						(hint (subs (gui/text toType) 0 1))
						(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey % 0))))
						(while (< (.-length (gui/text toType)) 20)
							(gui/setText! toType (str (gui/text toType) (getRandomCharacter)))
						)
					)
					(do
						(js/console.log "next release")
						(reset! encodedCharacter (bit-and @encodedCharacter (bit-not(get @encodeKey % 0))))
					)
				)
				(reset! inputMode :keyUp)
				(gui/setText! preview (.fromCharCode js/String @encodedCharacter))
			)
	})
)