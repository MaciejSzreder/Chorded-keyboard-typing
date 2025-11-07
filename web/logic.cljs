(ns logic (:require
	[cljs.pprint :refer [char-code]]
	[ll.gui :as gui]
	[ll.log :refer [log peek spy]]
))
(defn saveStatistics! [env state]
	(env :download "statistics.edn" (pr-str (state :stats)))
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

(defn getRandomNotMeasuredCharacter [env stats characterSet]
	(env :rand-nth (filter #(not (contains? stats %)) characterSet))
)
(defn getRandomMeasuredCharacter [env stats characterSet]
	(let [
		measured (filter #(contains? stats %) characterSet)
		measurements (zipmap measured (map #(env :rand-nth (get stats %)) measured))
		r (* (env :rand) (reduce + (vals measurements)))
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
(defn getRandomCharacter [env stats characterSet]
	(if (< (env :rand) 0.5)
		(if (every? (fn[char] (contains? stats char)) characterSet) ;; every character has been measured
			(getRandomMeasuredCharacter env stats characterSet)
			(getRandomNotMeasuredCharacter env stats characterSet)
		)
		(if (empty? stats)
			(getRandomNotMeasuredCharacter env stats characterSet)
			(getRandomMeasuredCharacter env stats characterSet)
		)
	)
)

(defn updateCharacterSet! [env controller interface newCharacterSet]
	(let [toType (interface :toType)]
		(controller :characterSet newCharacterSet)
		(gui/setText! toType "")
		(while (< (count (gui/text toType)) 20)
			(gui/setText! toType (str (gui/text toType) (getRandomCharacter env (controller :stats) (controller :characterSet))))
		)
		(hint (interface :fingers) (subs (gui/text toType) 0 1))
	)
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

(defn keyUp! [env interface key controller ]
	(let [
		encodeKey (controller :encodeKey)
		inputMode (controller :inputMode)
		encodedCharacter (controller :encodedCharacter)
		newCharacter (bit-and encodedCharacter (bit-not(get encodeKey key 0)))
		start (controller :start)
		output (interface :output)
		toType (interface :toType)
	]
		(when (contains? encodeKey key)
			(if (= inputMode :keyDown)
				(do
					(env :log "first release")
					(gui/setText! output (str (gui/text output) (char encodedCharacter)))
					(when (= (char encodedCharacter) (subs (gui/text toType) 0 1))
						(gui/setText! toType (subs (gui/text toType) 1))
						(let [end (env :now)]
							(when start
								(controller :addStatistic (char encodedCharacter) (- end start))
								(env :log "added measurement" (char encodedCharacter) (- end start))
							)
							(controller :start end)
						)
					)
					(hint (interface :fingers) (subs (gui/text toType) 0 1))
					(controller :encodedCharacter newCharacter)
					(while (< (count (gui/text toType)) 20)
						(gui/setText! toType (str (gui/text toType) (getRandomCharacter env (controller :stats) (controller :characterSet))))
					)
				)
				(do
					(env :log "next release")
					(controller :encodedCharacter newCharacter)
				)
			)
			(controller :inputMode :keyUp)
			(gui/setText! (interface :preview) (char newCharacter))
		)
	)
)