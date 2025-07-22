(ns serve-ttt.main
  (:require [serve-ttt.core :as core]
            [tic-tac-toe.core]
            [tic-tac-toe.persistence.postgresql]
            [serve-ttt.core :refer [server-name]]
            [serve-ttt.handler])
  (:import [Router Router FileHandler]
           [Main Server]
           (java.io File)
           (serve_ttt.handler TttPostHandler TttViewHandler))

  (:gen-class))

(def root-path (.toPath (File. "testroot")))

(def router (doto (Router. server-name)
              (.addRoute "GET" "/ttt/view" (TttViewHandler.))
              (.addRoute "GET" "/" (FileHandler. root-path core/server-name))
              (.addRoute "GET" "/*" (FileHandler. root-path core/server-name))
              (.addRoute "POST" "/*" (TttPostHandler.))))

(def server (Server. 80 "testroot" router))

(defn -main [& args]
  (println "Starting Tic-Tac-Toe in HTTP web server. Visit localhost to play")
  (.startServer server))
