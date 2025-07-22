(ns serve-ttt.web-interface-spec
  (:require [speclj.core :refer :all]
            [serve-ttt.web-interface :as sut]
            [tic-tac-toe.core :as core]
            [serve-ttt.test-helper :as helper]))


(describe "web state management"
  (with-stubs)

  (context "update-state for welcome status"                ;This may need to change, why not try to load a save here?
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
      (let [state  (helper/state-create {:status :select-board :response 3 :o-type :computer :x-type :human :o-difficulty :medium})
            result (sut/process-input state)]
        (should= :in-progress (:status result))
        (should-not-contain :form-data result)
        (should= [[1 2 3] [4 5 6] [7 8 9]] (:board result))))

    (it "sets board configuration and moves to in-progress for board size 3"
      (let [state  (helper/state-create {:status :select-board :response 4 :o-type :computer :x-type :human :o-difficulty :medium})
            result (sut/process-input state)]
        (should= :in-progress (:status result))
        (should-not-contain :form-data result)
        (should= [[1 2 3 4] [5 6 7 8] [9 10 11 12] [13 14 15 16]] (:board result))))


    )
  )