(ns serve-ttt.web-interface
  (:require [tic-tac-toe.core :as core]
            [tic-tac-toe.computer.easy]
            [tic-tac-toe.computer.medium]
            [tic-tac-toe.computer.hard]))


(defmethod core/start-game :web [state]
  (core/initial-state state))

(def board-size-map
  {:3x3   3
   :4x4   4
   :3x3x3 [3 3 3]})

#_(defmulti process-input :status)

;
;(defmethod process-input :welcome [state]
;  (core/maybe-load-save state))
;
;(defmethod process-input :found-save [state]
;  (let [response (= :load (:response state))]
;    (core/maybe-resume-save state response)))
;
;(defmethod process-input :config-x-type [state]
;  (core/config-x-type state))
;
;(defmethod process-input :config-o-type [state]
;  (core/config-o-type state))
;
;(defmethod process-input :config-x-difficulty [state]
;  (core/config-x-difficulty state))
;
;(defmethod process-input :config-o-difficulty [state]
;  (core/config-o-difficulty state))
;
;(defmethod process-input :select-board [state]
;  (let [converted-size  (get board-size-map (:response state))
;        corrected-state (assoc state :response converted-size)]
;    (core/select-board corrected-state)))
;
;(defmethod process-input :in-progress [state]
;  (core/play-turn! state))
;
;(defmethod process-input :tie [state]
;  (let [corrected-state (assoc state :response (= :play-again (:response state)))]
;    (core/maybe-play-again corrected-state)))
;
;(defmethod process-input :winner [state]
;  (let [corrected-state (assoc state :response (= :play-again (:response state)))]
;    (core/maybe-play-again corrected-state)))

(defmethod core/take-human-turn :web [state]
    (core/do-take-human-turn state))

(defn found-save [state]
  (let [response (= :load (:response state))]
    (core/maybe-resume-save state response)))

(defn select-board [state]
  (let [converted-size  (get board-size-map (:response state))
        corrected-state (assoc state :response converted-size)]
    (core/select-board corrected-state)))

(defn in-progress [starting-state]
  (let [correct-response (-> (:response starting-state) name Integer/parseInt)
        correct-state (assoc starting-state :response correct-response)
        state (core/play-turn! correct-state)]
    (dissoc state :response)))

(defn play-again [state]
  (let [corrected-state (assoc state :response (= :play-again (:response state)))]
    (core/maybe-play-again corrected-state)))

(defn process-input [state]
  (case (:status state)
  :welcome (core/maybe-load-save state)
  :found-save (found-save state)
  :config-x-type (core/config-x-type state)
  :config-x-difficulty (core/config-x-difficulty state)
  :config-o-type (core/config-o-type state)
  :config-o-difficulty (core/config-o-difficulty state)
  :select-board (select-board state)
  :in-progress (in-progress state)
  :tie (play-again state)
  :winner (play-again state)))