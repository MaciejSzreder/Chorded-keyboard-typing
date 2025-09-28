(ns ll.gui)


(defn spy [x] 
	(console.log x)
	x
)

(defn render [element]
	(.appendChild js/document.body element)
)

(defn button [label action]
	(let [button (.createElement js/document "button")]
		(set! (.-textContent button) label)
		(.addEventListener button "click" action)
		button
	)
)

(defn textField [content options action]
	(let [input (.createElement js/document "input")]
		(.setAttribute input "style" (apply str (map (
			fn[[property value]] (str (name property) ": " (name value) ";"))
			options
		)))
		(set! (.-value input) content)
		(.addEventListener input "keydown" #(.stopPropagation %))
		(.addEventListener input "keyup" #(.stopPropagation %))
		(.addEventListener input "input" action)
		input
	)
)

(defn container [elements options]
	(let [div (.createElement js/document "div")]
		(.setAttribute div "style" (apply str (map (
			fn[[property value]] (str (name property) ": " (name value) ";"))
			options
		)))
		(doseq [element elements]
			(.appendChild div element)
		)
		div
	)
)

(defn inline [elements options]
	(let [div (.createElement js/document "span")]
		(.setAttribute div "style" (apply str (map (
			fn[[property value]] (str (name property) ": " (name value) ";"))
			options
		)))
		(doseq [element elements]
			(.append div element)
		)
		div
	)
)

(defn set! [element options]
	(doseq [[property value] options]
		(.setProperty (.-style element) (name property) (name value))
	)
)

(defn unset! [element options]
	(doseq [property options]
		(.removeProperty (.-style element) (name property))
	)
)

(defn text [element]
	(let [
		value (.-value element)
		text (.-textContent element)
		]
		(cond
			(nil? value) text
			(nil? text) value
			(> (count value) (count text)) value
			:else text
		)
	)
)

(defn setText! [element newContent]
	(let [
		value (.-value element)
		text (.-textContent element)
		]
		(cond
			(nil? value) (set! (.-textContent element) newContent)
			(nil? text) (set! (.-value element) newContent)
			(> (count value) (count text)) (set! (.-value element) newContent)
			:else (set! (.-textContent element) newContent)
		)
	)
)

(defn registerListeners [listeners]
	(doseq [[event action] listeners]
		(.addEventListener js/document (name event) #(action (.-key %)))
	)
)