(ns serve-ttt.web-interface-spec
  (:require [speclj.core :refer :all]
            [serve-ttt.web-interface :as sut]
            [tic-tac-toe.core :as core]
            [serve-ttt.test-helper :as helper]))


(describe "web state management"
  (with-stubs)
  (before (reset! helper/mock-db nil))

  (context "update-state for welcome status"
    (it "moves to config-x-type when form-data exists"
      (let [state  {:interface :web :status :welcome :form-data {"new-game" "start"} :save :mock}
            result (sut/process-input state)]
        (should= :config-x-type (:status result))))
    )

  (context "update-state for found-save status"
    (it "moves to config-x-type when form-data {load-game load}"
      (let [state  (helper/state-create {:interface :web :status :found-save :response :load :save :mock
                                         :x-type    :human :o-type :computer :o-difficulty :medium :board [[1 2 "X"] [4 "O" 6] [7 8 9]]})
            result (sut/process-input state)]
        (should= result (assoc state :status :in-progress))))

    (it "resumes play of the loaded game (status = in-progress) when response = start"
      (let [state  (helper/state-create {:interface :web :status :found-save :response :start :save :mock
                                         :x-type    :human :o-type :computer :o-difficulty :medium :board [[1 2 "X"] [4 "O" 6] [7 8 9]]})
            result (sut/process-input state)]
        (should= result (core/fresh-start {:interface :web :save :mock}))))
    )


  (context "update-state for config-x-type status"
    (it "sets x player to human and moves to config-o-type when response is human"
      (let [state  (helper/state-create {:status :config-x-type :response :human})
            result (sut/process-input state)]
        (should= :human (get-in result [:players 0 :play-type]))
        (should= :config-o-type (:status result))
        (should-not-contain :response result)))

    (it "sets x player-type to computer and moves to config-x-difficulty when response is computer"
      (let [state  (helper/state-create {:status :config-x-type :response :computer})
            result (sut/process-input state)]
        (should= :computer (get-in result [:players 0 :play-type]))
        (should= :config-x-difficulty (:status result))
        (should-not-contain :response result)))
    )

  (context "update-state for config-o-type status"
    (it "sets o player to human and moves to config-board when response is human"
      (let [state  (helper/state-create {:status :config-o-type :response :human})
            result (sut/process-input state)]
        (should= :human (get-in result [:players 1 :play-type]))
        (should= :select-board (:status result))
        (should-not-contain :response result)))

    (it "sets o player to computer and moves to config-o-difficulty when response is computer"
      (let [state  (helper/state-create {:status :config-o-type :response :computer})
            result (sut/process-input state)]
        (should= :computer (get-in result [:players 1 :play-type]))
        (should= :config-o-difficulty (:status result))
        (should-not-contain :response result)))
    )

  (context "update-state for config-x-difficulty status"
    (it "sets x player difficulty and moves to config-o-type"
      (let [state  (helper/state-create {:status :config-x-difficulty :response :easy :x-type :computer})
            result (sut/process-input state)]
        (should= :easy (get-in result [:players 0 :difficulty]))
        (should= :config-o-type (:status result))
        (should-not-contain :form-data result)))

    (it "handles hard difficulty"
      (let [state  (helper/state-create {:status :config-x-difficulty :response :hard :x-type :computer})
            result (sut/process-input state)]
        (should= :hard (get-in result [:players 0 :difficulty]))))

    )

  (context "update-state for config-o-difficulty status"
    (it "sets o player difficulty and moves to config-board"
      (let [state  (helper/state-create {:status :config-o-difficulty :response :medium :o-type :computer :x-type :human})
            result (sut/process-input state)]
        (should= :medium (get-in result [:players 1 :difficulty]))
        (should= :select-board (:status result))
        (should-not-contain :form-data result)))

    (it "handles easy difficulty"
      (let [state  (helper/state-create {:status :config-o-difficulty :response :easy :o-type :computer :x-type :human})
            result (sut/process-input state)]
        (should= :easy (get-in result [:players 1 :difficulty]))))

    )

  (context "board configuration"
    (it "sets board configuration and moves to in-progress for board size 3"
      (let [state  (helper/state-create {:status :select-board :response :3x3 :o-type :computer :x-type :human :o-difficulty :medium})
            result (sut/process-input state)]
        (should= :in-progress (:status result))
        (should-not-contain :form-data result)
        (should= [[1 2 3] [4 5 6] [7 8 9]] (:board result))))

    (it "sets board configuration and moves to in-progress for board size 4"
      (let [state  (helper/state-create {:status :select-board :response :4x4 :o-type :computer :x-type :human :o-difficulty :medium})
            result (sut/process-input state)]
        (should= :in-progress (:status result))
        (should-not-contain :form-data result)
        (should= [[1 2 3 4] [5 6 7 8] [9 10 11 12] [13 14 15 16]] (:board result))))

    (it "sets board configuration and moves to in-progress for board size 3x3x3"
      (let [state  (helper/state-create {:status :select-board :response :3x3x3 :o-type :computer :x-type :human :o-difficulty :medium})
            result (sut/process-input state)]
        (should= :in-progress (:status result))
        (should-not-contain :form-data result)
        (should= [[[1 2 3] [4 5 6] [7 8 9]] [[10 11 12] [13 14 15] [16 17 18]] [[19 20 21] [22 23 24] [25 26 27]]]
                 (:board result))))
    )

  (context "tie"
    (it "returns a fresh-state in status config-x-type for play-again"
      (let [state  (helper/state-create {:status   :tie :x-type :human :o-type :human :board [["X" "X" "O"]
                                                                                              ["O" "O" "X"]
                                                                                              ["X" "O" "X"]]
                                         :response :play-again})
            result (sut/process-input state)]
        (should= result (core/fresh-start state))))

    (it "returns a state in status game-over for exit"
      (let [state  (helper/state-create {:status   :tie :x-type :human :o-type :human :board [["X" "X" "O"]
                                                                                              ["O" "O" "X"]
                                                                                              ["X" "O" "X"]]
                                         :response :exit})
            result (sut/process-input state)]
        (should= result (dissoc (assoc state :status :game-over) :response))))

    )

  (context "winner"
    (it "returns a fresh-state in status config-x-type for play-again"
      (let [state  (helper/state-create {:status   :winner :x-type :human :o-type :human :board [["X" "X" "X"]
                                                                                                 ["O" "O" "X"]
                                                                                                 ["X" "O" "O"]]
                                         :response :play-again})
            result (sut/process-input state)]
        (should= result (core/fresh-start state))))

    (it "returns a state in status game-over for exit"
      (let [state  (helper/state-create {:status   :winner :x-type :human :o-type :human :board [["X" "X" "X"]
                                                                                                 ["O" "O" "X"]
                                                                                                 ["X" "O" "O"]]
                                         :response :exit})
            result (sut/process-input state)]
        (should= result (dissoc (assoc state :status :game-over) :response))))

    )

  (context "in-progress"
    (it "in a human v human game, it plays the current turn and returns the next state"
      (let [state    (helper/state-create {:status              :in-progress :save :mock :x-type :human :o-type :human
                                           :active-player-index 0 :board [[1 2 3] [4 5 6] [7 8 9]]
                                           :response            :5 :interface :web})
            expected (helper/state-create {:status              :in-progress :save :mock :x-type :human :o-type :human
                                           :active-player-index 1 :board [[1 2 3] [4 "X" 6] [7 8 9]]
                                           :interface           :web})
            final    (sut/process-input state)]
        (should= expected final)))

    (it "returns an updated state without changed player if game is over"
      (let [state    (helper/state-create {:status              :in-progress :save :mock :x-type :human :o-type :human
                                           :active-player-index 0 :board [["X" "X" 3] ["O" "O" "X"] ["X" "O" "O"]]
                                           :response            :3 :interface :web})
            expected (helper/state-create {:status              :winner :save :mock :x-type :human :o-type :human
                                           :active-player-index 0 :board [["X" "X" "X"] ["O" "O" "X"] ["X" "O" "O"]]
                                           :interface           :web})]
        (should= expected (sut/process-input state))))

    (it "allows the computer to be prompted to take a turn"
      (let [state    (helper/state-create {:status              :in-progress :save :mock :x-type :computer :o-type :human
                                           :active-player-index 0 :board [["X" "X" 3]
                                                                          ["O" 5 "X"]
                                                                          [7 "O" "O"]]
                                           :response            :1 :interface :web :x-difficulty :hard})
            expected (helper/state-create {:status              :winner :save :mock :x-type :computer :o-type :human
                                           :active-player-index 0 :board [["X" "X" "X"]
                                                                          ["O" 5 "X"]
                                                                          [7 "O" "O"]]
                                           :interface           :web :x-difficulty :hard})]
        (should= expected (sut/process-input state))))
    )
  )