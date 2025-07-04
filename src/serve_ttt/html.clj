(ns serve-ttt.html
  (:require [clojure.string :as str]
            [serve-ttt.core :as core]
            [tic-tac-toe.persistence.postgresql]
            [tic-tac-toe.core :as ttt-core]))


(defn radio-option [name value checked?]
  (str "<label><input type='radio' name='" name "' value='" value "'"
       (when checked? " checked") "> "
       (clojure.string/capitalize value) "</label><br>"))

(defn radio-group [name options]
  (let [default (first options)]
    (apply str (map #(radio-option name % (= % default)) options))))

(defn form-page [title form-name options]
  (str "<html><body>"
       "<h1>" title "</h1>"
       "<form method='POST' action='/ttt'>"
       (radio-group form-name options)
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-welcome-page [state]
  (println "render-welcome")
  (let [saved-game (ttt-core/load-game state)]
    (prn "saved-game:" saved-game)
    (str "<html><body>"
         "<h1>Welcome to Tic-Tac-Toe!</h1>"
         "<p>Let's set up your game.</p>"
         "<form method='POST' action='/ttt'>"
         "<button type='submit' name='new-game' value='start'>Start Game Setup</button>"
         (when saved-game
           "<button type='submit' name='load-game' value='load'>Load Previous Game</button>")
         "</form>"
         "</body></html>"))
  )

(defn render-display-state [state]
  (str "<html><body><h1>Current state:</h1>"
       (str/join (for [[key value] state]
                   (str "<p>Key: " key ", Value: " value "</p>")))
       "</body></html>"))

(defn create-html [state]
  (println "create-html")
  (prn "state:" state)
  (case (:status state)
    :welcome (render-welcome-page state)
    :config-x-type (form-page "Choose X Player Type" "x-type" core/player-types)
    :config-x-difficulty (form-page "Choose X Player Difficulty" "x-difficulty" core/difficulty-levels)
    :config-o-type (form-page "Choose O Player Type" "o-type" core/player-types)
    :config-o-difficulty (form-page "Choose O Player Difficulty" "o-difficulty" core/difficulty-levels)
    :select-board (form-page "Choose Board Size" "board-size" core/board-sizes)
    :display (render-display-state state)

    (str "<h1>Unknown state: " (:status state) "</h1>")))