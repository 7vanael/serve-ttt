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

(defn state-create [{:keys [interface board active-player-index status x-type o-type x-difficulty o-difficulty cells save response]
                     :or   {board               nil
                            active-player-index 0
                            status              :welcome
                            x-type              nil
                            o-type              nil
                            x-difficulty        nil
                            o-difficulty        nil
                            save                :mock}}]
  (cond-> {:board               board
           :active-player-index active-player-index
           :status              status
           :players             [{:character "X" :play-type x-type :difficulty x-difficulty}
                                 {:character "O" :play-type o-type :difficulty o-difficulty}]}
          (some? cells) (assoc :cells cells)
          (some? interface) (assoc :interface interface)
          (some? response) (assoc :response response)
          (some? save) (assoc :save save)))