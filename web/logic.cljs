(ns logic (:require
	[cljs.pprint :refer [char-code]]
	[ll.gui :as gui]
	[ll.file :as file]
	[ll.log :refer [log peek spy]]
))
(defn saveStatistics! [stats]
	(file/download "statistics.edn" (pr-str stats))
)

(defn updateEncoding! [controller keys code]
	(controller :encodeKey (merge
		(zipmap keys (map (fn[] code) keys))
		(filter (fn[[key encoding]] (not= code encoding)) (controller :encodeKey))
	))
)

(defn highlightFinger [fingers n]
	(gui/set! (nth fingers n) {:background-color :yellow})
)
(defn unhighlightFinger [fingers n]
	(gui/unset! (nth fingers n) [:background-color])
)
(defn hint [fingers char]
	(dotimes [finger 10]
		(if (not= 0 (bit-and (Math/pow 2 finger) (char-code char)))
			(highlightFinger fingers finger)
			(unhighlightFinger fingers finger)
		)
	)
)

(defn getRandomNotMeasuredCharacter [stats characterSet]
	(rand-nth (filter #(not (contains? stats %)) characterSet))
)
(defn getRandomMeasuredCharacter [stats characterSet]
	(let [
		measured (filter #(contains? stats %) characterSet)
		measurements (zipmap measured (map #(rand-nth (get stats %)) measured))
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
(defn getRandomCharacter [stats characterSet]
	(if (< (rand) 0.5)
		(if (every? (fn[char] (contains? stats char)) characterSet) ;; every character has been measured
			(getRandomMeasuredCharacter stats characterSet)
			(getRandomNotMeasuredCharacter stats characterSet)
		)
		(if (empty? stats)
			(getRandomNotMeasuredCharacter stats characterSet)
			(getRandomMeasuredCharacter stats characterSet)
		)
	)
)

(defn updateCharacterSet! [controller newCharacterSet toType fingers]
	(controller :characterSet newCharacterSet)
	(gui/setText! toType "")
	(while (< (count (gui/text toType)) 20)
		(gui/setText! toType (str (gui/text toType) (getRandomCharacter (controller :stats) (controller :characterSet))))
	)
	(hint fingers (subs (gui/text toType) 0 1))
)

(defn keyDown! [key controller preview]
	(let [
		encodeKey (controller :encodeKey)
		encodedCharacter (controller :encodedCharacter)
	]
		(when (contains? encodeKey key)
			(controller :inputMode :keyDown)
			(let [newCharacter (bit-or encodedCharacter (get encodeKey key 0))]
				(controller :encodedCharacter newCharacter)
				(gui/setText! preview (char newCharacter))
			)	
		)
	)
)

(defn keyUp! [key controller preview output toType fingers]
	(let [
		encodeKey (controller :encodeKey)
		inputMode (controller :inputMode)
		encodedCharacter (controller :encodedCharacter)
		newCharacter (bit-and encodedCharacter (bit-not(get encodeKey key 0)))
		start (controller :start)
	]
		(when (contains? encodeKey key)
			(if (= inputMode :keyDown)
				(do
					(log "first release")
					(gui/setText! output (str (gui/text output) (char encodedCharacter)))
					(when (= (char encodedCharacter) (subs (gui/text toType) 0 1))
						(gui/setText! toType (subs (gui/text toType) 1))
						(let [end (system-time)]
							(when start
								(controller :addStatistic (char encodedCharacter) (- end start))
								(log "added measurement" (char encodedCharacter) (- end start))
							)
							(controller :start end)
						)
					)
					(hint fingers (subs (gui/text toType) 0 1))
					(controller :encodedCharacter newCharacter)
					(while (< (count (gui/text toType)) 20)
						(gui/setText! toType (str (gui/text toType) (getRandomCharacter (controller :stats) (controller :characterSet))))
					)
				)
				(do
					(log "next release")
					(controller :encodedCharacter newCharacter)
				)
			)
			(controller :inputMode :keyUp)
			(gui/setText! preview (char newCharacter))
		)
	)
)