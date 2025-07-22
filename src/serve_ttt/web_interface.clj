(ns serve-ttt.web-interface
  (:require [tic-tac-toe.core :as core]))

(defmulti process-input :status)

(defmethod core/start-game :web [state]
  (core/initial-state state))

(defmethod process-input :welcome [state]
  (core/maybe-load-save state))

(defmethod process-input :found-save [state]
  (let [response (= :load (:response state))]
    (core/maybe-resume-save state response)))

(defmethod process-input :config-x-type [state]
  (core/config-x-type state))

(defmethod process-input :config-o-type [state]
  (core/config-o-type state))

(defmethod process-input :config-x-difficulty [state]
  (core/config-x-difficulty state))

(defmethod process-input :config-o-difficulty [state]
  (core/config-o-difficulty state))

(def board-size-map
  {"3x3"   3
   "4x4"   4
   "3x3x3" [3 3 3]})

(defmethod process-input :select-board [state]
  (core/select-board state))