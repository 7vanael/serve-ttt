(ns serve-ttt.html
  (:require [clojure.string :as str]))

(defn render-config-o-difficulty-page []
  (str "<html><body>"
       "<h1>Choose O Player Difficulty</h1>"
       "<form method='POST' action='/ttt'>"
       "<label><input type='radio' name='o-difficulty' value='easy' checked> Easy</label><br>"
       "<label><input type='radio' name='o-difficulty' value='medium'> Medium</label><br>"
       "<label><input type='radio' name='o-difficulty' value='hard'> Hard</label><br>"
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-config-o-type-page []
  (str "<html><body>"
       "<h1>Choose O Player Type</h1>"
       "<form method='POST' action='/ttt'>"
       "<label><input type='radio' name='o-type' value='human' checked> Human</label><br>"
       "<label><input type='radio' name='o-type' value='computer'> Computer</label><br>"
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-config-x-difficulty-page []
  (str "<html><body>"
       "<h1>Choose X Player Difficulty</h1>"
       "<form method='POST' action='/ttt'>"
       "<label><input type='radio' name='x-difficulty' value='easy' checked> Easy</label><br>"
       "<label><input type='radio' name='x-difficulty' value='medium'> Medium</label><br>"
       "<label><input type='radio' name='x-difficulty' value='hard'> Hard</label><br>"
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

(defn render-config-x-type-page []
  (str "<html><body>"
       "<h1>Choose X Player Type</h1>"
       "<form method='POST' action='/ttt'>"
       "<label><input type='radio' name='x-type' value='human' checked> Human</label><br>"
       "<label><input type='radio' name='x-type' value='computer'> Computer</label><br>"
       "<button type='submit'>Next</button>"
       "</form>"
       "</body></html>"))

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
    :display (render-display-state state)

    (str "<h1>Unknown state: " (:status state) "</h1>")))