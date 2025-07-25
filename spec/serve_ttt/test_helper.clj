(ns serve-ttt.test-helper
  (:require [tic-tac-toe.core :as core]))

(def mock-db (atom {}))

(defmethod core/save-game :mock [state]
  (let [game-id (or (:game-id state) (inc (count @mock-db)))
        saved-state (assoc state :game-id game-id)]
    (swap! mock-db assoc game-id saved-state)
    saved-state))

(defmethod core/load-game :mock [state]
  (if-let [game-id (:game-id state)]
    (if-let [saved-game (get @mock-db game-id)]
      (assoc saved-game :interface (:interface state))
      (core/save-game (core/initial-state {:interface (:interface state) :save :mock})))
    (if-let [in-progress-game (->> @mock-db
                                   vals(filter #(= (:status %) :in-progress))
                                   (sort-by :game-id)
                                   last)]
      (assoc in-progress-game :status :found-save :interface (:interface state))
      (core/save-game (core/initial-state {:interface (:interface state) :sae :mock})))))

;(defmethod core/load-game :mock [state] (if (nil? @mock-db)
;                                          state
;                                          (assoc @mock-db :status :found-save)))


(defmethod core/delete-save :mock [_] (reset! mock-db {}))

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