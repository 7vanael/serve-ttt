(ns serve-ttt.main
  (:require [serve-ttt.handler :refer [ttt-handler]]
            [serve-ttt.core :refer [server-name]])
  (:import [Router Router]
           [Main Server])
  (:gen-class))

(def router (doto (Router. server-name)
              (.addRoute "GET" "/ttt" ttt-handler)
              (.addRoute "POST" "/ttt" ttt-handler)))

(def server (Server. 80 "testroot" router))

(defn -main [& args]
  (println "Starting Tic-Tac-Toe in HTTP web server. Visit localhost/ttt to play")
  (.startServer server))
