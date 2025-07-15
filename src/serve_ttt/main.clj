(ns serve-ttt.main
  (:require [serve-ttt.core :as core]
            [tic-tac-toe.core]
            [serve-ttt.core :refer [server-name]]
            [serve-ttt.handler])
  (:import [Router Router FileHandler]
           [Main Server]
           (java.io File)
           (serve_ttt.handler TttHandler))

  (:gen-class))

(def root-path (.toPath (File. "testroot")))

(def router (doto (Router. server-name)
              (.addRoute "GET" "/" (FileHandler. root-path core/server-name))
              (.addRoute "GET" "/*" (FileHandler. root-path core/server-name))
              (.addRoute "GET" "/ttt" (FileHandler. root-path core/server-name))
              (.addRoute "GET" "/ttt/*" (FileHandler. root-path core/server-name))
              (.addRoute "POST" "/ttt" (TttHandler.))
              (.addRoute "POST" "/ttt/*" (TttHandler.))))

(def server (Server. 80 "testroot" router))

(defn -main [& args]
  (println "Starting Tic-Tac-Toe in HTTP web server. Visit localhost/ttt to play")
  (.startServer server))
