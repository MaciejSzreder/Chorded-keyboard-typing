(ns ll.file)

(defn download [name content]
	(let [
		downloader (.createElement js/document "a")
		blob (.createObjectURL js/URL (js/Blob. #js [content] #js {}))
		]
		(.setAttribute downloader "href" blob)
		(.setAttribute downloader "download" name)
		(.click downloader)
		(.revokeObjectURL js/URL blob)
	)
)
