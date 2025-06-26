(ns serve-ttt.html)

(defn render-config-x-difficulty-page []
  (str "<html><body>"
       "<h1>Choose X Player Difficulty</h1>"
       "<form method='POST' action='/ttt'>"
       "<label><input type='radio' name='x-type' value='easy' checked> Easy</label><br>"
       "<label><input type='radio' name='x-type' value='medium'> Medium</label><br>"
       "<label><input type='radio' name='x-type' value='hard'> Hard</label><br>"
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
       "<button type='submit' name='action' value='start'>Start Game Setup</button>"
       "</form>"
       "</body></html>"))

(defn create-html [state]
  (case (:status state)
    :welcome (render-welcome-page)
    :config-x-type (render-config-x-type-page)
    :config-x-difficulty (render-config-x-difficulty-page)

    (str "<h1>Unknown state: " (:status state) "</h1>")))