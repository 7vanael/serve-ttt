(ns serve-ttt.web-interface
  (:require [clojure.test :refer :all]
            [tic-tac-toe.core :as core])
  (:import [Connection Response]))

(defmethod core/start-game :web [state]
  (core/initial-state (:interface state) (:save state)))
