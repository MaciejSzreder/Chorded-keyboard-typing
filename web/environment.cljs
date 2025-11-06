(ns environment(:require
	[ll.file :refer [download]]
))

(def actions {
	:download download
})

(defn controller [action & args]
	(apply (get actions action) args)
)