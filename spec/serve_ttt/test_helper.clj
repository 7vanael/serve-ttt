(ns serve-ttt.test-helper)

(def mock-files (atom {}))

(defn mock-spit [filename content]
  (swap! mock-files assoc filename content)
  filename)

(defn mock-file-exists? [filename]
  (contains? @mock-files filename))

(defn mock-file-content [filename]
  (get @mock-files filename))

(defn mock-file-contains? [filename content]
  (when-let [file-content (mock-file-content filename)]
    (clojure.string/includes? file-content content)))

(defn list-mock-files []
  (keys @mock-files))

(defn mock-write-html-file [html status]
  (let [filename (str (name status) ".html")
        filepath (str "testroot/" filename)]
    (try
      (mock-spit filepath html)
      filename
      (catch Exception e
        (println "Mock error writing file:" (.getMessage e))
        nil))))