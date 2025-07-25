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

(defn extract-cookie-value [cookies cookie-name]
  (when-let [cookie (first (filter #(str/includes? % (str cookie-name "=")) cookies))]
    (let [value-part (second (str/split cookie #"="))]
      (first (str/split value-part #";")))))

(describe "handler for web"
  (with-stubs)
  (before (reset! helper/mock-db {}))
  (around [it] (binding [sut/*save-method* :mock] (it)))

  (context "form data"
    (it "returns a map of form input"
      (let [request (mock-request "POST" "/test" :body (.getBytes "key=value"))]
        (should= {"key" "value"} (sut/get-form-data request))))
    (it "can parse more than one form input"
      (let [request (mock-request "POST" "/test" :body (.getBytes "key1=value1&key2=value2"))]
        (should= {"key1" "value1" "key2" "value2"} (sut/get-form-data request))))
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
      (let [request  (mock-request "POST" "/ttt" :body (.getBytes "new-game=start"))
            handler  (TttPostHandler.)
            response (.handle handler request)]
        (should= 302 (.getStatusCode response))
        (should= "/ttt/view" (get (.getHeaders response) "Location"))
        (should-contain "Redirecting" (String. (.getBody response)))
        (should (some #(str/includes? % "game-id=") (.getCookies response)))))

    (it "creates new game when no game-id cookie present"
      (let [request        (mock-request "POST" "/ttt" :body (.getBytes "new-game=start"))
            handler        (TttPostHandler.)
            response       (.handle handler request)
            cookies        (.getCookies response)
            game-id-cookie (first (filter #(str/includes? % "game-id=") cookies))]
        (should-not-be-nil game-id-cookie)
        (let [cookie-value (second (str/split game-id-cookie #"="))
              game-id      (Integer/parseInt (first (str/split cookie-value #";")))
              loaded-state (ttt-core/load-game {:save :mock :interface :web :game-id game-id})]
          (should= :welcome (:status loaded-state)))))

    (it "Response includes cookie with game-id"
      (let [state    {:status :config-x-type :save :mock :interface :web :response :human :game-id 6}
            response (sut/handle-request state)
            cookies  (.getCookies response)]
        (should (some #(str/includes? % "game-id=") cookies))))

    (it "handles form data correctly and successfully updates the saved state"
      (let [initial-state {:status  :config-x-type :save :mock :interface :web
                           :players [{:character "X" :play-type nil :difficulty nil}
                                     {:character "O" :play-type nil :difficulty nil}]}
            saved-state   (ttt-core/save-game initial-state)
            game-id       (:game-id saved-state)
            request       (mock-request "POST" "/ttt"
                                        :cookies {"game-id" (str game-id)}
                                        :body (.getBytes "x-type=human"))
            handler       (TttPostHandler.)
            response      (.handle handler request)
            cookies       (.getCookies response)
            game-id-str   (extract-cookie-value cookies "game-id")
            _             (prn "game-id-str:" game-id-str)
            updated-state (ttt-core/load-game {:save :mock :interface :web :game-id game-id})]

        (prn "updated-state:" updated-state)
        (should= (str game-id) game-id-str)
        (should= :human (get-in updated-state [:players 0 :play-type]))
        (should= :config-o-type (:status updated-state))))

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
    (it "loads existing game when game-id cookie present"
      (let [existing-state {:status :in-progress :board [["X" 2 3] [4 5 6] [7 8 9]] :save :mock :interface :web}
            saved-state    (ttt-core/save-game existing-state)
            game-id        (:game-id saved-state)
            request        (mock-request "GET" "/ttt/view" :cookies {"game-id" (str game-id)})
            handler        (TttViewHandler.)
            response       (.handle handler request)]
        (should= 200 (.getStatusCode response))
        (should-contain "X" (String. (.getBody response)))))

    (it "loads existing game when game-id cookie present"
      (let [existing-state {:status :in-progress :board [["X" 2 3] [4 5 6] [7 8 9]] :save :mock :interface :web}
            saved-state    (ttt-core/save-game existing-state)
            game-id        (:game-id saved-state)
            request        (mock-request "GET" "/ttt/view" :cookies {"game-id" (str game-id)})
            handler        (TttViewHandler.)
            response       (.handle handler request)]
        (should= 200 (.getStatusCode response))
        (should-contain "X" (String. (.getBody response)))))

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

    (it "creates a response object including game-id cookie"
      (let [state    {:interface           :web
                      :board               board3
                      :active-player-index 0
                      :status              :in-progress
                      :players             [{:character "X" :play-type :computer :difficulty :easy}
                                            {:character "O" :play-type :computer :difficulty :hard}]
                      :save                :mock}
            saved-state (core/save-game state)
            html     "<html>Test Body</html>"
            response (sut/generate-get-response html saved-state)
            id (:game-id saved-state)
            headers  (.getHeaders response)
            cookies  (.getCookies response)]
        (should= "text/html" (get headers "Content-Type"))
        (should= html (String. (.getBody response)))
        (should (some #(str/includes? % (str "game-id="id)) cookies))
        (should= 1 (count cookies))))
    )
  )