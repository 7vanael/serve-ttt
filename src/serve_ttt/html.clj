(ns serve-ttt.html
  (:require [clojure.string :as str]
            [serve-ttt.core :as core]))


(defn radio-option [name value checked?]
  (str "<label><input type='radio' name='" name "' value='" value "'"
       (when checked? " checked") "> "
       (clojure.string/capitalize value) "</label><br>"))

(defn radio-group [name options & {:keys [default-value]}]
  (let [default (or default-value (first options))]
    (apply str (map #(radio-option name % (= % default)) options))))

(defn form-page [title form-name options & {:keys [default-value]}]
  (str "<html><body>"
       "<h1>" title "</h1>"
       "<form method='POST' action='/ttt'>"
       (radio-group form-name options :default-value default-value)
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-config-x-type-page []
  (form-page "Choose X Player Type" "x-type" core/player-types))

(defn render-config-o-type-page []
  (form-page "Choose O Player Type" "o-type" core/player-types))

(defn render-config-x-difficulty-page []
  (form-page "Choose X Player Difficulty" "x-difficulty" core/difficulty-levels))

(defn render-config-o-difficulty-page []
  (form-page "Choose O Player Difficulty" "o-difficulty" core/difficulty-levels))

(defn render-config-board-page []
  (form-page "Choose Board Size" "board-size" core/board-sizes))

(defn render-welcome-page []
  (str "<html><body>"
       "<h1>Welcome to Tic-Tac-Toe!</h1>"
       "<p>Let's set up your game.</p>"
       "<form method='POST' action='/ttt'>"
       "<button type='submit' name='new-game' value='start'>Start Game Setup</button>"
       "</form>"
       "</body></html>"))

(defn render-display-state [state]
  (str "<html><body><h1>Current state:</h1>"
       (str/join (for [[key value] state]
                   (str "<p>Key: " key ", Value: " value "</p>")))
       "</body></html>"))

(defn create-html [state]
  (case (:status state)
    :welcome (render-welcome-page)
    :config-x-type (render-config-x-type-page)
    :config-x-difficulty (render-config-x-difficulty-page)
    :config-o-type (render-config-o-type-page)
    :config-o-difficulty (render-config-o-difficulty-page)
    :config-board (render-config-board-page)
    :display (render-display-state state)

    (str "<h1>Unknown state: " (:status state) "</h1>")))