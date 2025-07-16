(ns serve-ttt.web-interface
  (:require [tic-tac-toe.core :as core]
            [tic-tac-toe.board :refer [new-board]]
            ))

(defmethod core/start-game :web [state]
  (core/initial-state (:interface state) (:save state)))

(defmethod core/update-state [:web :welcome] [state]
  ;(let [game (core/load-game state)]
  ;  (prn "game:" game))
  (if (:form-data state)
    (assoc state :status :config-x-type)
    state))

(defmethod core/update-state [:web :config-x-type] [state]
  (if-let [x-type (get-in state [:form-data "x-type"])]
    (-> state
        (assoc-in [:players 0 :play-type] (keyword x-type))
        (assoc :status (if (= "human" x-type) :config-o-type :config-x-difficulty))
        (dissoc :form-data))
    state))

(defmethod core/update-state [:web :config-o-type] [state]
  (if-let [o-type (get-in state [:form-data "o-type"])]
    (-> state
        (assoc-in [:players 1 :play-type] (keyword o-type))
        (assoc :status (if (= "human" o-type) :select-board :config-o-difficulty))
        (dissoc :form-data))
    state))

(defmethod core/update-state [:web :config-x-difficulty] [state]
  (if-let [x-difficulty (get-in state [:form-data "x-difficulty"])]
    (-> state
        (assoc-in [:players 0 :difficulty] (keyword x-difficulty))
        (assoc :status :config-o-type)
        (dissoc :form-data))
    state))

(defmethod core/update-state [:web :config-o-difficulty] [state]
  (if-let [o-difficulty (get-in state [:form-data "o-difficulty"])]
    (-> state
        (assoc-in [:players 1 :difficulty] (keyword o-difficulty))
        (assoc :status :select-board)
        (dissoc :form-data))
    state))

(def board-size-map
  {"3x3"   3
   "4x4"   4
   "3x3x3" [3 3 3]})

(defmethod core/update-state [:web :select-board] [state]
  (if-let [board-size (get-in state [:form-data "board-size"])]
    (if-let [board-key (get board-size-map board-size)]
      (-> state
          (assoc :status :display) ;this will be changed to in-progress once that story is in!
          (assoc :board (new-board board-key))
          (dissoc :form-data))
      state)
    state))