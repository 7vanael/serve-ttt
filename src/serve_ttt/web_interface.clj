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
  {:3x3   3
   :4x4   4
   :3x3x3 [3 3 3]})

(defmethod process-input :select-board [state]
  (let [converted-size  (get board-size-map (:response state))
        corrected-state (assoc state :response converted-size)]
    (core/select-board corrected-state)))

(defmethod process-input :in-progress [state]
  (core/play-turn! state))

(defmethod process-input :tie [state]
  (core/maybe-play-again state))

(defmethod process-input :winner [state]
  (core/maybe-play-again state))

(defmethod core/take-human-turn :web [state]
  (let [correct-response (-> (:response state)
                             name
                             Integer/parseInt)
        correct-state (assoc state :response correct-response)]
    (core/do-take-human-turn correct-state)))