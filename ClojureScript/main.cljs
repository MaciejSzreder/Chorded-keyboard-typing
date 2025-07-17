(.addEventListener js/document "keydown" #(
	js/console.log "Key down event detected" (.-key %)
))