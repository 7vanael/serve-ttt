(ns serve-ttt.main
  (:require [tic-tac-toe.core]
            ;[tic-tac-toe.persistence.postgresql]
            [serve-ttt.core :refer [server-name]])
  (:import [Router Router]
           [Main Server]
           [serve_ttt.handler TttHandler])
  (:gen-class))

(def router (doto (Router. server-name)
              (.addRoute "GET" "/ttt" (TttHandler.))
              (.addRoute "POST" "/ttt" (TttHandler.))))

(def server (Server. 80 "testroot" router))

(defn -main [& args]
  (println "Starting Tic-Tac-Toe in HTTP web server. Visit localhost/ttt to play")
  (.startServer server))
