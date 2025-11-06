(ns state)

(def state {
	:encodeKey (atom {
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
	}),
	:characterSet (atom "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM-=[]\\;',./`~!@#$%^&*()_+{}|:\"<>?"),
	:stats (atom {}),
	:encodedCharacter (atom 0),
	:inputMode (atom :keyDown),
	:start (atom nil)
})

(defn property [name] (fn[state] (get state name)))

(def actions {
	:encodeKey (property :encodeKey)
	:characterSet (property :characterSet)
	:stats (property :stats )
	:addStatistic (fn[state char time]
		(reset! (:stats state) (assoc @(:stats state) char (conj (get @(:stats state) char) time)))
	)
	:encodedCharacter (property :encodedCharacter)
	:inputMode (property :inputMode)
	:start (property :start)
})

(defn controller
	([] (controller state))
	([state] (fn[action & args]
		(apply (get actions action) state args)
	))
)