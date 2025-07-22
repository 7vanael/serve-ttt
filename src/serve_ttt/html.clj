(ns serve-ttt.html
  (:require [clojure.string :as str]
            [serve-ttt.core :as core]))


(defn game-styles []
  (str "<style>"
       "body { font-family: Arial, sans-serif; text-align: center; }"
       "table { border-collapse: collapse; margin: 20px auto; }"
       "td { border: 2px solid #333; width: 80px; height: 80px; text-align: center; vertical-align: middle; }"
       ".occupied { font-size: 36px; font-weight: bold; background-color: #f0f0f0; }"
       ".empty { padding: 0; }"
       ".move-button { width: 100%; height: 100%; font-size: 24px; border: none; background-color: #e8f4fd; cursor: pointer; }"
       ".move-button:hover { background-color: #d0e8ff; }"
       ".current-player { font-size: 24px; margin: 20px 0; color: #2c5aa0; font-weight: bold; }"
       ".game-over { font-size: 28px; margin: 20px 0; color: #d63384; font-weight: bold; }"
       ".game-actions { margin: 30px 0; }"
       ".action-button { font-size: 18px; padding: 10px 20px; margin: 0 10px; cursor: pointer; background-color: #195787; color: white; border: none; border-radius: 5px; }"
       ".action-button:hover { background-color: rgba(32, 101, 138, 0.6); }"
       "</style>"))

(defn radio-option [name value checked?]
  (str "<label><input type='radio' name='" name "' value='" value "'"
       (when checked? " checked") "> "
       (clojure.string/capitalize value) "</label><br>"))

(defn radio-group [name options]
  (let [default (first options)]
    (apply str (map #(radio-option name % (= % default)) options))))

(defn form-page [title form-name options]
  (str "<html><head>"
       (game-styles)
       "</head><body>"
       "<h1>" title "</h1>"
       "<form method='POST' action='/ttt'>"
       (radio-group form-name options)
       "<button type='submit' class='action-button'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-welcome-page [state]
  ;(let [saved-game (ttt-core/load-game state)]
  ;(prn "saved-game:" saved-game)
  (str "<html><head>"
       (game-styles)
       "</head><body>"
       "<h1>Welcome to Tic-Tac-Toe!</h1>"
       "<p>Let's set up your game.</p>"
       "<form method='POST' action='/ttt'>"
       "<button type='submit' name='new-game' value='start' class='action-button'>Start Game Setup</button>"
       ; "<button type='submit' name='load-game' value='load' class='action-button'>Load Previous Game</button>")
       "</form>"
       "</body></html>"))
; )

(defn render-save-found [state]
  (str "<html><head>"
       (game-styles)
       "</head><body>"
       "<h1>Welcome to Tic-Tac-Toe!</h1>"
       "<p>Let's set up your game.</p>"
       "<form method='POST' action='/ttt'>"
       "<button type='submit' name='new-game' value='start' class='action-button'>Start Game Setup</button>"
       "<button type='submit' name='load-game' value='load' class='action-button'>Load Previous Game</button>"
       "</form>"
       "</body></html>"))

(defn render-cell [cell]
  (cond
    (= cell "X") (str "<td class='occupied'>" cell "</td>")
    (= cell "O") (str "<td class='occupied'>" cell "</td>")
    :else
    (str "<td class='empty'>"
         "<form method='POST' action='/ttt' style='display: inline;'>"
         "<button type='submit' name='selection' value='" cell "' class='move-button'>"
         cell
         "</button>"
         "</form>"
         "</td>")))

(defn render-static-cell [cell]
  (str "<td class='occupied'>" cell "</td>"))

(defn render-static-row [row]
  (str "<tr>"
       (apply str (map render-static-cell row))
       "</tr>"))

(defn render-board-row [row]
  (str "<tr>"
       (apply str (map render-cell row))
       "</tr>"))

(defn render-board-table [{:keys [board status]}]
  (str "<table>"
       (if (= :in-progress status)
         (apply str (map #(render-board-row %) board))
         (apply str (map #(render-static-row %) board)))
       "</table>"))

(defn render-game-announcement [{:keys [status active-player-index players] :as state}]
  (let [current-char (get-in players [active-player-index :character])]
    (case status
      :in-progress (str "<div class='current-player'>Player " current-char "'s turn</div>")
      :tie (str "<div class='game-over'>It's a tie!</div>")
      :winner (str "<div class='game-over'>Player " current-char " wins!</div>")
      #_(str "<div class='current-player'>Game Status: " (name status) "</div>"))))

(defn render-game-actions []
  (str "<div class='game-actions'>"
       "<form method='POST' action='/ttt' style='display: inline; margin-right: 10px;'>"
       "<button type='submit' name='action' value='play-again' class='action-button'>Play Again</button>"
       "</form>"
       "<form method='POST' action='/ttt' style='display: inline;'>"
       "<button type='submit' name='action' value='exit' class='action-button'>Exit</button>"
       "</form>"
       "</div>"))

(defn render-game-board [state]
  (str "<html>"
       "<head>"
       "<title>Tic-Tac-Toe</title>"
       (game-styles)
       "</head>"
       "<body>"
       "<h1>Tic-Tac-Toe</h1>"
       (render-game-announcement state)
       (render-board-table state)
       "</body>"
       "</html>"))

(defn render-game-over [state]
  (str "<html>"
       "<head>"
       "<title>Tic-Tac-Toe - Game Over</title>"
       (game-styles)
       "</head>"
       "<body>"
       "<h1>Tic-Tac-Toe</h1>"
       (render-game-announcement state)
       (render-board-table state)
       (render-game-actions)
       "</body>"
       "</html>"))

(defn render-exit []
  (str "<html>"
  "<head>"
  "<title>Tic-Tac-Toe - Exit</title>"
       (game-styles)
       "</head>"
       "<body>"
       "<h1>Tic-Tac-Toe</h1>"
       "<div class='game-over'>Thanks for Playing!</div>"))

(defn render-display-state [state]
  (str "<html><body><h1>Current state:</h1>"
       (str/join (for [[key value] state]
                   (str "<p>Key: " key ", Value: " value "</p>")))
       "</body></html>"))

(defn create-html [state]
  (case (:status state)
    :welcome (render-welcome-page state)
    :found-save (render-save-found state)
    :config-x-type (form-page "Choose X Player Type" "x-type" core/player-types)
    :config-x-difficulty (form-page "Choose X Player Difficulty" "x-difficulty" core/difficulty-levels)
    :config-o-type (form-page "Choose O Player Type" "o-type" core/player-types)
    :config-o-difficulty (form-page "Choose O Player Difficulty" "o-difficulty" core/difficulty-levels)
    :select-board (form-page "Choose Board Size" "board-size" core/board-sizes)
    :in-progress (render-game-board state)
    :tie (render-game-over state)
    :winner (render-game-over state)
    :game-over (render-exit)
    :display (render-display-state state)

    (str "<h1>Unknown state: " (:status state) "</h1>")))