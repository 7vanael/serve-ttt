(ns serve-ttt.test-helper
  (:require [tic-tac-toe.core :as core]))

(def mock-db (atom nil))

(defmethod core/save-game :mock [state] (reset! mock-db state))
(defmethod core/load-game :mock [state] (if (nil? @mock-db)
                                          state
                                          (assoc @mock-db :status :found-save)))
(defmethod core/delete-save :mock [_] (reset! mock-db nil))

(defn state-create [{:keys [interface board active-player-index status x-type o-type x-difficulty o-difficulty cells save response]
                     :or   {board               nil
                            active-player-index 0
                            status              :welcome
                            x-type              nil
                            o-type              nil
                            x-difficulty        nil
                            o-difficulty        nil
                            save                :mock}}]
  (cond-> {:board               board
           :active-player-index active-player-index
           :status              status
           :players             [{:character "X" :play-type x-type :difficulty x-difficulty}
                                 {:character "O" :play-type o-type :difficulty o-difficulty}]}
          (some? cells) (assoc :cells cells)
          (some? interface) (assoc :interface interface)
          (some? response) (assoc :response response)
          (some? save) (assoc :save save)))