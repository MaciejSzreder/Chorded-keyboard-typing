(ns environment(:require
	[ll.file :refer [download]]
	[ll.log :refer [log]]
))

(def actions {
	:download download
	:rand-nth rand-nth
	:rand rand
	:log log
})

(defn controller [action & args]
	(apply (get actions action) args)
)