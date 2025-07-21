(ns serve-ttt.web-interface-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.core :as core]))


#_(describe "web state management"
  (with-stubs)

  (context "update-state for welcome status"
    (it "moves to config-x-type when form-data exists"
      (let [state  {:interface :web :status :welcome :form-data {"new-game" "start"}}
            result (core/update-state state)]
        (should= :config-x-type (:status result))))

    (it "stays at welcome when no form-data"
      (let [state  {:interface :web :status :welcome}
            result (core/update-state state)]
        (should= :welcome (:status result)))))

  (context "update-state for config-x-type status"
    (it "sets x player to human and moves to config-o-type"
      (let [state  {:interface :web
                    :status    :config-x-type
                    :form-data {"x-type" "human"}
                    :players   [{:play-type nil :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :human (get-in result [:players 0 :play-type]))
        (should= :config-o-type (:status result))
        (should-not-contain :form-data result)))

    (it "sets x player-type to computer and moves to config-x-difficulty"
      (let [state  {:interface :web
                    :status    :config-x-type
                    :form-data {"x-type" "computer"}
                    :players   [{:play-type nil :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :computer (get-in result [:players 0 :play-type]))
        (should= :config-x-difficulty (:status result))
        (should-not-contain :form-data result)))

    (it "stays at config-x-type when no x-type in form-data"
      (let [state  {:interface :web
                    :status    :config-x-type
                    :form-data {"other" "data"}
                    :players   [{:play-type nil :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :config-x-type (:status result))
        (should-contain :form-data result))))

  (context "update-state for config-o-type status"
    (it "sets o player to human and moves to config-board"
      (let [state  {:interface :web
                    :status    :config-o-type
                    :form-data {"o-type" "human"}
                    :players   [{:play-type :human :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :human (get-in result [:players 1 :play-type]))
        (should= :select-board (:status result))
        (should-not-contain :form-data result)))

    (it "sets o player to computer and moves to config-o-difficulty"
      (let [state  {:interface :web
                    :status    :config-o-type
                    :form-data {"o-type" "computer"}
                    :players   [{:play-type :human :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :computer (get-in result [:players 1 :play-type]))
        (should= :config-o-difficulty (:status result))
        (should-not-contain :form-data result)))

    (it "stays at config-o-type when no o-type in form-data"
      (let [state  {:interface :web
                    :status    :config-o-type
                    :form-data {"other" "data"}
                    :players   [{:play-type :human :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :config-o-type (:status result))
        (should-contain :form-data result))))

  (context "update-state for config-x-difficulty status"
    (it "sets x player difficulty and moves to config-o-type"
      (let [state  {:interface :web
                    :status    :config-x-difficulty
                    :form-data {"x-difficulty" "easy"}
                    :players   [{:play-type :computer :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :easy (get-in result [:players 0 :difficulty]))
        (should= :config-o-type (:status result))
        (should-not-contain :form-data result)))

    (it "handles hard difficulty"
      (let [state  {:interface :web
                    :status    :config-x-difficulty
                    :form-data {"x-difficulty" "hard"}
                    :players   [{:play-type :computer :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :hard (get-in result [:players 0 :difficulty]))))

    (it "stays at config-x-difficulty when no x-difficulty in form-data"
      (let [state  {:interface :web
                    :status    :config-x-difficulty
                    :form-data {"other" "data"}
                    :players   [{:play-type :computer :difficulty nil} {:play-type nil :difficulty nil}]}
            result (core/update-state state)]
        (should= :config-x-difficulty (:status result))
        (should-contain :form-data result))))

  (context "update-state for config-o-difficulty status"
    (it "sets o player difficulty and moves to config-board"
      (let [state  {:interface :web
                    :status    :config-o-difficulty
                    :form-data {"o-difficulty" "medium"}
                    :players   [{:play-type :human :difficulty nil} {:play-type :computer :difficulty nil}]}
            result (core/update-state state)]
        (should= :medium (get-in result [:players 1 :difficulty]))
        (should= :select-board (:status result))
        (should-not-contain :form-data result)))

    (it "handles easy difficulty"
      (let [state  {:interface :web
                    :status    :config-o-difficulty
                    :form-data {"o-difficulty" "easy"}
                    :players   [{:play-type :human :difficulty nil} {:play-type :computer :difficulty nil}]}
            result (core/update-state state)]
        (should= :easy (get-in result [:players 1 :difficulty]))))

    (it "stays at config-o-difficulty when no o-difficulty in form-data"
      (let [state  {:interface :web
                    :status    :config-o-difficulty
                    :form-data {"other" "data"}
                    :players   [{:play-type :human :difficulty nil} {:play-type :computer :difficulty nil}]}
            result (core/update-state state)]
        (should= :config-o-difficulty (:status result))
        (should-contain :form-data result))))

  (context "board configuration"
    (it "sets board configuration and moves to display"
      (let [state  {:interface :web
                    :status    :select-board
                    :form-data {"board-size" "3x3"}
                    :players   [{:play-type :human :difficulty nil} {:play-type :computer :difficulty nil}]}
            result (core/update-state state)]
        (should= :display (:status result))
        (should-not-contain :form-data result)
        (should= [[1 2 3] [4 5 6] [7 8 9]] (:board result)))))
  )