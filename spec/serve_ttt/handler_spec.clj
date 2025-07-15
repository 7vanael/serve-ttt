(ns serve-ttt.handler-spec
  (:require [clojure.string :as str]
            [speclj.core :refer :all]
            [serve-ttt.handler :as sut]
            [serve-ttt.mock-request :refer [mock-request]]
            [serve-ttt.test-helper :as helper])
  (:import [Connection Response Request]
           (java.io ByteArrayInputStream File)))

(def board3 [[1 2 "X"]
             [4 "O" 6]
             [7 8 9]])
(def board3-str "[1 2 \"X\" 4 \"O\" 6 7 8 9]")
(def board4 [[1 "X" 3 4]
             [5 6 "O" 8]
             [9 10 11 12]
             [13 14 15 16]])
(def board4-str "[1 \"X\" 3 4 5 6 \"O\" 8 9 10 11 12 13 14 15 16]")
(def board27 [[[1 2 "X"]
               [4 "O" 6]
               [7 8 9]]
              [[10 11 12]
               [13 14 15]
               [16 17 18]]
              [[19 20 21]
               [22 23 24]
               [25 26 27]]])
(def board27-str "[1 2 \"X\" 4 \"O\" 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27]")

(def mock-initial-state {:interface           :web
                         :board               nil
                         :active-player-index 0
                         :status              :welcome
                         :players             [{:character "X" :play-type nil :difficulty nil}
                                               {:character "O" :play-type nil :difficulty nil}]
                         :save                :sql})

#_(defn create-request [method path & {:keys [headers body]}]
    (let [request-line   (str method " " path " HTTP/1.1\r\n")
          header-lines   (apply str (map (fn [[k v]] (str k ": " v "\r\n")) headers))
          request-string (str request-line header-lines "\r\n" (when body (String. body)))
          input-stream   (ByteArrayInputStream. (.getBytes request-string "ISO-8859-1"))]
      (Request/parseRequest input-stream)))

(describe "handler for web"
  (with-stubs)
  (before (reset! helper/mock-files {}))

  (context "form data"
    (it "returns a map of form input"
      (let [request (mock-request "POST" "/test" :body (.getBytes "key=value"))]
        (should= {"key" "value"} (sut/get-form-data request))))
    (it "can parse more than one form input"
      (let [request (mock-request "POST" "/test" :body (.getBytes "key1=value1&key2=value2"))]
        (should= {"key1" "value1" "key2" "value2"} (sut/get-form-data request))))
    )

  (context "board as a cookie/string"
    (it "converts a board to a string"
      (should= board3-str (sut/grid->string board3))
      (should= board4-str (sut/grid->string board4))
      (should= board27-str (sut/grid->string board27)))
    (it "converts a string board back into a complex vector"
      (should= board3 (sut/string->grid board3-str))
      (should= board4 (sut/string->grid board4-str))
      (should= board27 (sut/string->grid board27-str)))
    )

  (context "state to map (later to be cookies)"
    (it "creates a map that reflects the current state"
      (let [state   {:interface           :web
                     :board               board3
                     :active-player-index 0
                     :status              :in-progress
                     :players             [{:character "X" :play-type :computer :difficulty :easy}
                                           {:character "O" :play-type :computer :difficulty :hard}]
                     :save                :sql}
            cookies (sut/state->cookies state)]
        (should= "web" (get cookies "interface"))
        (should= board3-str (get cookies "board"))
        (should= "0" (get cookies "active-player-index"))
        (should= "in-progress" (get cookies "status"))
        (should= "sql" (get cookies "save"))
        (should= "computer" (get cookies "x-type"))
        (should= "computer" (get cookies "o-type"))
        (should= "easy" (get cookies "x-difficulty"))
        (should= "hard" (get cookies "o-difficulty"))))

    (it "allows for nil values in state when making cookies"
      (let [state   mock-initial-state
            cookies (sut/state->cookies state)]
        (should= "welcome" (get cookies "status"))
        (should-be-nil (get cookies "x-difficulty"))
        (should-be-nil (get cookies "x-type"))
        (should-be-nil (get cookies "o-difficulty"))
        (should-be-nil (get cookies "o-type"))))
    )

  (context "state from cookies"
    (it "updates the state according to the cookies"
      (let [cookies {"status"              "in-progress"
                     "interface"           "web"
                     "save"                "sql"
                     "active-player-index" "0"
                     "x-type"              "computer"
                     "o-type"              "computer"
                     "o-difficulty"        "hard"
                     "x-difficulty"        "easy"
                     "board"               board3-str}
            state   (sut/cookies->state cookies)]
        (should= :in-progress (:status state))
        (should= 0 (:active-player-index state))
        (should= :web (:interface state))
        (should= :computer (get-in state [:players 0 :play-type]))
        (should= :easy (get-in state [:players 0 :difficulty]))
        (should= :computer (get-in state [:players 1 :play-type]))
        (should= :hard (get-in state [:players 1 :difficulty]))
        (should= board3 (:board state))))

    (it "allows for nil values and still provides a complete state"
      (let [cookies {"status"              "in-progress"
                     "interface"           "web"
                     "save"                "sql"
                     "active-player-index" "0"
                     "x-type"              nil
                     "o-type"              nil
                     "o-difficulty"        nil
                     "x-difficulty"        nil
                     "board"               nil}
            state   (sut/cookies->state cookies)]
        (should= :in-progress (:status state))
        (should= 0 (:active-player-index state))
        (should= :web (:interface state))
        (should-be-nil (get-in state [:players 0 :play-type]))
        (should-be-nil (get-in state [:players 0 :difficulty]))
        (should-be-nil (get-in state [:players 1 :play-type]))
        (should-be-nil (get-in state [:players 1 :difficulty]))
        (should-be-nil (:board state))))
    )

  (context "adding form data to state"
    (it "adds form data to the state"
      (let [request (mock-request "POST" "/test" :body (.getBytes "new-game=start"))
            state   (sut/get-game-from-request request)]
        (should= {"new-game" "start"} (:form-data state))))
    (it "does not add form data to state if no form data is present"
      (let [request (mock-request "GET" "/test")
            state   (sut/get-game-from-request request)]
        (should-be-nil (:form-data state))))
    )

  (context "writes html files"
    (it "writes html to file named for status"
      (let [html "<html><h1>TestPage</h1></html>"
            filename (helper/mock-write-html-file html :welcome)]
        (should= "welcome.html" filename)
        (should (helper/mock-file-exists? "testroot/welcome.html"))
        (should (helper/mock-file-contains? "testroot/welcome.html" "TestPage")))))

  (it "overwrites existing mock files"
    (let [html1 "<html><h1>First</h1></html>"
          html2 "<html><h1>Second</h1></html>"
          _ (helper/mock-write-html-file html1 :config-x-type)
          filename (helper/mock-write-html-file html2 :config-x-type)]
      (should= "config-x-type.html" filename)
      (should (helper/mock-file-contains? "testroot/config-x-type.html" "Second"))
      (should-not (helper/mock-file-contains? "testroot/config-x-type.html" "First"))))

  (it "can track multiple files"
    (helper/mock-write-html-file "<html>Welcome</html>" :welcome)
    (helper/mock-write-html-file "<html>Config</html>" :config-x-type)
    (should= 2 (count (helper/list-mock-files)))
    (should (helper/mock-file-exists? "testroot/welcome.html"))
    (should (helper/mock-file-exists? "testroot/config-x-type.html")))

  (it "converts status keywords to filenames correctly"
    (let [test-cases [[:welcome "welcome.html"]
                      [:config-x-type "config-x-type.html"]
                      [:config-o-difficulty "config-o-difficulty.html"]
                      [:in-progress "in-progress.html"]]]
      (doseq [[status expected-filename] test-cases]
        (let [filename (helper/mock-write-html-file "<html>test</html>" status)]
          (should= expected-filename filename)
          (should (helper/mock-file-exists? (str "testroot/" expected-filename)))))))

  (context "generates redirect response object"
    (it "creates a 302 redirect response using mock file system"
      (let [state mock-initial-state
            response (sut/handle-request state helper/mock-write-html-file)
            headers (.getHeaders response)]
        (should= 302 (.getStatusCode response))
        (should= "Redirecting" (String. (.getBody response)))
        (should-contain "Location" (keys headers))
        (should= 1 (count (helper/list-mock-files)))))

    (it "creates a response object including cookies that capture the state"
      (let [state    {:interface           :web
                      :board               board3
                      :active-player-index 0
                      :status              :in-progress
                      :players             [{:character "X" :play-type :computer :difficulty :easy}
                                            {:character "O" :play-type :computer :difficulty :hard}]
                      :save                :sql}
            html     "<html>Test Body</html>"
            response (sut/generate-response html state)
            headers  (.getHeaders response)
            cookies  (.getCookies response)]
        (should= "text/html" (get headers "Content-Type"))
        (should= html (String. (.getBody response)))
        (should (some #(str/includes? % "status=in-progress") cookies))
        (should= 9 (count cookies))))
    )

  (context "handles the Post with a redirect to get"
    (it "processes POST request and returns redirect with correct location"
      (let [state (assoc mock-initial-state :form-data {"new-game" "start"})
            response (sut/handle-request state helper/mock-write-html-file)
            headers (.getHeaders response)
            location (get headers "Location")]
        (should= 302 (.getStatusCode response))
        (should (str/includes? location "/ttt/"))
        (should (str/ends-with? location ".html"))
        (should-contain "Redirecting" (String. (.getBody response)))))

    (it "creates mock HTML file that matches redirect location"
      (let [state (assoc mock-initial-state :form-data {"new-game" "start"})
            response (sut/handle-request state helper/mock-write-html-file)
            headers (.getHeaders response)
            location (get headers "Location")
            filename (str/replace location "/ttt/" "testroot/")]
        (should (helper/mock-file-exists? filename))
        (should (> (count (helper/mock-file-content filename)) 0))))

    (it "different states create different mock filenames"
      (let [welcome-state (assoc mock-initial-state :status :welcome)
            config-state (assoc mock-initial-state :status :config-x-type)
            welcome-response (sut/handle-request welcome-state helper/mock-write-html-file)
            config-response (sut/handle-request config-state helper/mock-write-html-file)
            welcome-location (get (.getHeaders welcome-response) "Location")
            config-location (get (.getHeaders config-response) "Location")]
        (should-not= welcome-location config-location)
        (should (str/includes? welcome-location "welcome.html"))
        (should (str/includes? config-location "config-x-type.html"))
        (should= 2 (count (helper/list-mock-files)))))

    (it "full integration with mocked file system"
      (with-redefs[]
        (let [request (mock-request "POST" "/ttt" :body (.getBytes "new-game=start"))
            state (sut/get-game-from-request request)
            response (sut/handle-request state helper/mock-write-html-file)
            headers (.getHeaders response)]
        (should= 302 (.getStatusCode response))
        (should-contain "Location" (keys headers))
        (should-contain "Redirecting" (String. (.getBody response)))
        (should (> (count (helper/list-mock-files)) 0))))))
  )