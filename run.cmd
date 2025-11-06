@echo off

echo ^

^<!doctype html^>^

^<title^>debug utf8-keyboard^</title^>^

^<body^>^

	^<script src=^"out/main.js^"^>^</script^>^

^</body^>^
 > index.html


IF NOT EXIST cljs.jar^

	echo download cljs.jar from https://github.com/clojure/clojurescript/releases


java^
	-cp "cljs.jar;web"^
	cljs.main^
	-c train^
	-r