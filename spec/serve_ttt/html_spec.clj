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
      (should-contain "value='3x3x3'" html)
      (should-contain "Choose Board Size" html)))

  (it "renders a display of the state"
    (let [html (sut/create-html {:status :display})]
      (should-contain "<p>Key: " html)
      (should-contain ", Value:" html)
      (should-contain "Current state:" html)))
  )