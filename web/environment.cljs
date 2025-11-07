(ns environment(:require
	[ll.file :refer [download]]
))

(def actions {
	:download download
	:rand-nth rand-nth
	:rand rand
})

(defn controller [action & args]
	(apply (get actions action) args)
)