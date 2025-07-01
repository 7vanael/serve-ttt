(ns serve-ttt.web-interface
  (:require [tic-tac-toe.core :as core]
            [serve-ttt.html :as html])
  (:import [Router Router]
           [Main Server RouteHandler]
           [Connection Response Request]))

(defmethod core/start-game :web [state]
  (core/initial-state (:interface state) (:save state)))

(defmethod core/update-state [:web :welcome] [state]
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
        (assoc :status (if (= "human" o-type) :config-board :config-o-difficulty))
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
        (assoc :status :config-board)
        (dissoc :form-data))
    state))