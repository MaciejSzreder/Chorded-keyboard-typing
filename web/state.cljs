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

(defn addStatistic! [stats char time]
	(reset! stats (assoc @stats char (conj (get @stats char) time)))
)