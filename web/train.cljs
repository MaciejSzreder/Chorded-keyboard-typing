(ns train (:require
	[ll.gui :as gui]
	[ll.log :refer [log peek spy]]
	[environment :refer [controller] :rename {controller env}]
	[state :refer [controller]]
	[logic :refer [saveStatistics! updateEncoding! hint updateCharacterSet! keyDown! keyUp!]]
	[interface]
))

(do
	(def interface (interface/controller {
		:downloadStatistics #(saveStatistics! env (controller))
		:loadKeyMapping #(some (fn[[key code]] (when (= code (Math/pow 2 %)) key)) ((controller) :encodeKey))
		:setKeyMapping #(updateEncoding! (controller) %1 (Math/pow 2 %2))
		:loadCharacterSet #((controller) :characterSet)
		:setCharacterSet #(updateCharacterSet! env (controller) interface %)
		:loadText #((controller) :characterSet)
		:keydown #(keyDown! % (controller) interface)
		:keyup #(keyUp! env interface % (controller))
	}))


)