(ns ll.log)

(defn log [& args]
	(apply (.-log js/console) args)
	(apply pr args)
	(first args)
)

(defn peek [f & args] 
	(log (apply f args))
)

(defn spy [f & args]
	(apply log (conj args "<-" (apply f args)))
)