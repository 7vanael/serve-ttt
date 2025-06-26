(ns serve-ttt.main
  (:require [tic-tac-toe.core :as core]
            [serve-ttt.html :as html])
  (:import [Router Router]
           [Main Server RouteHandler]
           [Connection Response Request])
  (:gen-class))

(def server-name "ttt-service")

(defn get-game-from-state [request]
  #_(Oooh kay, this is where we put the cookies into state))

(defn generate-response [html]
  #_(Annd, here is where we put the state into cookies))

(def ttt-handler
  (reify RouteHandler
    (handle [this request]
      (let [state (get-game-from-request request)
            input (parse-form-response request)
            updated-state (if input (process-input state input) state)
            _     (core/save-game updated-state)
            html  (html/create-html updated-state)]
        (generate-response html))
      #_(Response. (str server-name) (int 200) (str "text/html") (str "<h1>Hello from " server-name " </h1>")))))

(def router (doto (Router. server-name) (.addRoute "GET" "/ttt" ttt-handler) (.addRoute "POST" "/ttt" ttt-handler)))
(def server (Server. 8080 "testroot" router))

(defn -main [& args]
  (println "Hello world")
  (.startServer server))
