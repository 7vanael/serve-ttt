(ns serve-ttt.html-spec
  (:require [speclj.core :refer :all]
            [serve-ttt.html :as sut]))

(describe "html"
  (it "renders welcome page"
    (let [html (sut/create-html {:status :welcome :save :edn})]
      (should-contain "Welcome to Tic-Tac-Toe!" html)))

  (it "renders config x-type page"
    (let [html (sut/create-html {:status :config-x-type})]
      (should-contain "name='x-type'" html)
      (should-contain "value='human'" html)
      (should-contain "value='computer'" html)
      (should-contain "Choose X Player Type" html)))

  (it "renders config o-type page"
    (let [html (sut/create-html {:status :config-o-type})]
      (should-contain "name='o-type'" html)
      (should-contain "value='human'" html)
      (should-contain "value='computer'" html)
      (should-contain "Choose O Player Type" html)))

  (it "renders config x-difficulty page"
    (let [html (sut/create-html {:status :config-x-difficulty})]
      (should-contain "name='x-difficulty'" html)
      (should-contain "value='easy'" html)
      (should-contain "value='medium'" html)
      (should-contain "value='hard'" html)
      (should-contain "Choose X Player Difficulty" html)))

  (it "renders config o-difficulty page"
    (let [html (sut/create-html {:status :config-o-difficulty})]
      (should-contain "name='o-difficulty'" html)
      (should-contain "value='easy'" html)
      (should-contain "value='medium'" html)
      (should-contain "value='hard'" html)
      (should-contain "Choose O Player Difficulty" html)))

  (it "renders select board-size page"
    (let [html (sut/create-html {:status :select-board})]
      (should-contain "name='board-size'" html)
      (should-contain "value='3x3'" html)
      (should-contain "value='4x4'" html)
      ;(should-contain "value='3x3x3'" html)
      (should-contain "Choose Board Size" html)))

  (context "single cell rendering"
    (it "renders an occupied space with X"
      (should= "<td class='occupied'>X</td>" (sut/render-cell "X")))

    (it "renders an occupied space with O"
      (should= "<td class='occupied'>O</td>" (sut/render-cell "O")))

    (it "renders an available space with the number as a clickable form"
      (should= (str "<td class='empty'><form method='POST' action='/ttt' "
                    "style='display: inline;'><button type='submit' name='selection' "
                    "value='3' class='move-button'>3</button></form></td>")
               (sut/render-cell 3)))

    (it "renders a static cell, not clickable even if it's a number"
      (should= "<td class='occupied'>3</td>" (sut/render-static-cell 3)))
    )

  (context "row rendering"
    (it "renders a static, occupied row"
      (let [expected (str "<tr><td class='occupied'>X</td><td class='occupied'>O</td>"
                          "<td class='occupied'>X</td></tr>")
            row ["X" "O" "X"]]
        (should= expected (sut/render-static-row row))))

    (it "renders a static row with open spaces"
      (let [expected (str "<tr><td class='occupied'>X</td><td class='occupied'>2</td>"
                          "<td class='occupied'>3</td></tr>")
            row ["X" 2 3]]
        (should= expected (sut/render-static-row row))))

    (it "renders an occupied row"
      (let [expected (str "<tr><td class='occupied'>X</td><td class='occupied'>O</td>"
                          "<td class='occupied'>X</td></tr>")
            row ["X" "O" "X"]]
        (should= expected (sut/render-board-row row))))

    (it "renders a row with playable spaces"
      (let [expected (str "<tr>"
                          "<td class='occupied'>X</td>"
                          "<td class='empty'>"
                          "<form method='POST' action='/ttt' style='display: inline;'>"
                          "<button type='submit' name='selection' value='2' class='move-button'>"
                          "2</button></form></td>"
                          "<td class='empty'>"
                          "<form method='POST' action='/ttt' style='display: inline;'>"
                          "<button type='submit' name='selection' value='3' class='move-button'>"
                          "3</button></form></td>"
                          "</tr>")
            row ["X" 2 3]]
        (should= expected (sut/render-board-row row))))
    )

  (it "renders tie page"
    (let [html (sut/create-html {:status :tie :board [["X" "X" "O"]
                                                      ["O" "O" "X"]
                                                      ["X" "O" "X"]]})]
      (should-contain "<div class='game-over'>It's a tie!</div>" html)
      (should-contain "value='play-again'" html)
      (should-contain "value='exit'" html)
      (should-not-contain "<td class='empty'>" html)
      (should-not-contain "<button type='submit' name='selection'" html)))

  (it "renders winner page"
    (let [html (sut/create-html {:status  :winner :active-player-index 1
                                 :board   [["X" "X" "O"]
                                           [4 "O" "X"]
                                           ["O" 8 9]]
                                 :players [{:character "X" :play-type :human}
                                           {:character "O" :play-type :human}]})]
      (should-contain "<div class='game-over'>Player O wins!</div>" html)
      (should-contain "value='play-again'" html)
      (should-contain "value='exit'" html)
      (should-not-contain "<td class='empty'>" html)
      (should-not-contain "<button type='submit' name='selection'" html)
      (should-contain (str "<tr><td class='occupied'>O</td><td class='occupied'>8</td>"
                           "<td class='occupied'>9</td></tr>") html)))

  (it "renders an active board in-progress"
    (let [html (sut/create-html {:status  :in-progress :active-player-index 1
                                 :board   [["X" "X" 3]
                                           [4 "O" "X"]
                                           ["O" 8 9]]
                                 :players [{:character "X" :play-type :human}
                                           {:character "O" :play-type :human}]})]
      (should-contain "<div class='current-player'>Player O's turn</div>" html)
      (should-contain "<td class='empty'>" html)
      (should-contain "<button type='submit' name='selection' value='3" html)
      (should-contain (str "<tr><td class='occupied'>O</td>"
                           "<td class='empty'><form method='POST' action='/ttt' style='display: inline;'>"
                           "<button type='submit' name='selection' value='8' class='move-button'>"
                           "8</button></form></td>"
                           "<td class='empty'><form method='POST' action='/ttt' style='display: inline;'>"
                           "<button type='submit' name='selection' value='9' class='move-button'>"
                           "9</button></form></td>"
                           "</tr>") html)))

  (it "renders a display of the state"
    (let [html (sut/create-html {:status :display})]
      (should-contain "<p>Key: " html)
      (should-contain ", Value:" html)
      (should-contain "Current state:" html)))

  )