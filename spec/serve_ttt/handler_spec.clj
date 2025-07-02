(ns serve-ttt.handler-spec
  (:require [clojure.string :as str]
            [speclj.core :refer :all]
            [serve-ttt.handler :as sut]
            [serve-ttt.mock-request :refer [mock-request]]))

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

(describe "handler for web"
  (with-stubs)

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
        (should= board3 (:board state))
        ))
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

  (context "generates response object"
    (it "creates a response object if given html and a state"
      (let [state    mock-initial-state
            html     "<html><h1>Test html body</h1></html>"
            response (sut/generate-response html state)]
        (should-not-be-nil response)
        (should-contain "Test html body" (String. (.getBody response)))))
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

  (context "handles the request"
    (it "calls update-state on the state and creates a response"
      (let [state    (assoc mock-initial-state :form-data "new-game=start")
            response (sut/handle-request state)
            cookies  (.getCookies response)]
        (should-not-be-nil response)
        (should-contain "Choose X Player" (String. (.getBody response)))
        (should (some #(str/includes? % "status=config-x-type") cookies))))

    (it "handles the request from request object to response object"
      (let [request (mock-request "POST" "/ttt" )]))
    )
  )