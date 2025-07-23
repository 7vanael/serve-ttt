(ns serve-ttt.handler-spec
  (:require [clojure.string :as str]
            [speclj.core :refer :all]
            [serve-ttt.handler :as sut]
            [serve-ttt.mock-request :refer [mock-request]]
            [serve-ttt.test-helper :as helper]
            [tic-tac-toe.core :as core]
            [tic-tac-toe.core :as ttt-core])
  (:import [Connection Response Request]
           (java.io ByteArrayInputStream File)
           (serve_ttt.handler TttPostHandler TttViewHandler)))




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
                         :save                :mock})

(describe "handler for web"
  (with-stubs)
  (before (reset! helper/mock-db nil))

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
                     :save                :mock}
            cookies (sut/state->cookies state)]
        (should= "web" (get cookies "interface"))
        (should= board3-str (get cookies "board"))
        (should= "0" (get cookies "active-player-index"))
        (should= "in-progress" (get cookies "status"))
        (should= "mock" (get cookies "save"))
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
        (should= :start (:response state))))
    (it "does not add form data to state if no form data is present"
      (let [request (mock-request "GET" "/test")
            state   (sut/get-game-from-request request)]
        (should-be-nil (:response state))))
    )
  (context "POST handler"
    (it "processes POST request and returns redirect "
      (with-redefs [ttt-core/load-game (fn [state & _] state)]
        (let [request  (mock-request "POST" "/ttt" :body (.getBytes "new-game=start"))
              handler  (TttPostHandler.)
              response (.handle handler request)]
          (should= 302 (.getStatusCode response))
          (should= "/ttt/view" (get (.getHeaders response) "Location"))
          (should-contain "Redirecting" (String. (.getBody response)))
          (should (some #(str/includes? % "status=config-x-type") (.getCookies response))))))

    (it "sets cookies from updated state"
      (let [state    {:status :config-x-type :save :mock :interface :web :response :human}
            response (sut/handle-request state)
            cookies  (.getCookies response)]
        (should (some #(str/includes? % "status=config-o-type") cookies))))

    (it "handles form data correctly"
      (let [request  (mock-request "POST" "/ttt"
                                   :cookies {"status" "config-x-type"}
                                   :body (.getBytes "x-type=human"))
            handler  (TttPostHandler.)
            response (.handle handler request)]
        (should= 302 (.getStatusCode response))
        (should (some #(str/includes? % "x-type") (.getCookies response)))))

    (it "full process test POST; implementable handler that creates a redirect response from a Post request"
      (with-redefs [ttt-core/initial-state (fn [state] (merge {:interface           :web
                                                               :board               nil
                                                               :active-player-index 0
                                                               :status              :welcome
                                                               :players             [{:character "X" :play-type nil :difficulty nil}
                                                                                     {:character "O" :play-type nil :difficulty nil}]
                                                               :save                :mock}
                                                              state))]
        (let [request  (mock-request "POST" "/ttt" :body (.getBytes "new-game=start"))
              handler  (sut/TttPostHandler.)
              response (.handle handler request)
              headers  (.getHeaders response)]
          (should= 302 (.getStatusCode response))
          (should-contain "Location" (keys headers))
          (should-contain "Redirecting" (String. (.getBody response))))))
    )


  (context "GET (view) handler "
    (it "generates HTML response from cookie state"
      (let [request  (mock-request "GET" "/ttt/view"
                                   :cookies {"status" "config-x-type", "interface" "web", "save" "mock"})
            handler  (TttViewHandler.)
            response (.handle handler request)]
        (should= 200 (.getStatusCode response))
        (should= "text/html" (get (.getHeaders response) "Content-Type"))
        (should-contain "html" (str/lower-case (String. (.getBody response))))))

    (it "handles initial game state from cookies"
      (let [request  (mock-request "GET" "/ttt/view"
                                   :cookies {"status" "welcome"})
            handler  (TttViewHandler.)
            response (.handle handler request)]
        (should= 200 (.getStatusCode response))))

    (it "full process test GET; implementable handler that renders and serves html from the state provided in a GET request"
      (let [request  (mock-request "GET" "/ttt/view"
                                   :cookies {"status"    "welcome"
                                             "interface" "web"
                                             "save"      "mock"})
            handler  (sut/TttViewHandler.)
            response (.handle handler request)]
        (should= 200 (.getStatusCode response))
        (should= "text/html" (get (.getHeaders response) "Content-Type"))
        (should-contain "<html" (String. (.getBody response)))))
    )

  (context "generates redirect response object"
    (it "creates a 302 redirect response"
      (let [state    mock-initial-state
            response (sut/handle-request state)
            headers  (.getHeaders response)]
        (should= 302 (.getStatusCode response))
        (should= "Redirecting" (String. (.getBody response)))
        (should-contain "Location" (keys headers))))

    (it "creates a response object including cookies that capture the state"
      (let [state    {:interface           :web
                      :board               board3
                      :active-player-index 0
                      :status              :in-progress
                      :players             [{:character "X" :play-type :computer :difficulty :easy}
                                            {:character "O" :play-type :computer :difficulty :hard}]
                      :save                :mock}
            html     "<html>Test Body</html>"
            response (sut/generate-response html state)
            headers  (.getHeaders response)
            cookies  (.getCookies response)]
        (should= "text/html" (get headers "Content-Type"))
        (should= html (String. (.getBody response)))
        (should (some #(str/includes? % "status=in-progress") cookies))
        (should= 9 (count cookies))))
    )
  )