IF NOT EXIST cljs.jar^

	echo download cljs.jar from https://github.com/clojure/clojurescript/releases

java^
	-cp "cljs.jar;web"^
	cljs.main^
	-O advanced^
	-o train.js^
	-c train