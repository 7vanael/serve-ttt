(ns serve-ttt.handler
  (:require [clojure.string :as str]
            [tic-tac-toe.core :as core]
            [tic-tac-toe.core :as ttt-core]
            [serve-ttt.html :refer [create-html]]
            [serve-ttt.core :refer [server-name]]
            [serve-ttt.web-interface :refer [process-input]])
  (:import [Main RouteHandler]
           [Connection Response Request]))

(def ^:dynamic *save-method* :sql)

(defn split-on-equals [string]
  (let [[k v] (str/split string #"=" 2)]
    [k v]))

(defn get-form-data [request]
  (let [body-bytes (.getBody request)
        body       (String. (bytes body-bytes) "UTF-8")
        fields     (str/split body #"&")]
    (if (= 0 (count body))
      {}
      (into {} (map split-on-equals fields)))))

(defn add-form-data-to-state [request state]
  (let [form-data (get-form-data request)
        response  (-> form-data vals first keyword)]
    (cond-> state
            (seq form-data) (assoc :response response))))

(defn get-game-id-from-cookies [cookies]
  (when-let [game-id-str (get cookies "game-id")]
    (when-not (or (nil? game-id-str) (= "nil" game-id-str) (= "" game-id-str))
      (Integer/parseInt game-id-str))))

(defn get-game-from-request [request]
  (let [game-id              (get-game-id-from-cookies (.getCookies request))
        base-state           {:interface :web :save *save-method*}
        base-state-with-id   (if game-id (assoc base-state :game-id game-id) base-state)
        loaded-state         (ttt-core/load-game base-state-with-id)
        state-with-form-data (add-form-data-to-state request loaded-state)]
    state-with-form-data))

(defn generate-get-response [html state]
  (let [response (Response. (str server-name) (int 200) (str "text/html") (str html))]
    (when-let [game-id (:game-id state)]
      (.addCookie response (str "game-id=" game-id "; Path=/ttt; Max-Age=3600")))
    response))


(defn handle-request [state]
  (let [updated-state (core/save-game (process-input state))
        response      (Response. (str server-name) (int 302) (str "text/plain") (str "Redirecting"))]
    (.addHeader response "Location" "/ttt/view")
    (when-let [game-id (:game-id updated-state)]
      (.addCookie response (str "game-id=" game-id "; Path=/ttt; Max-Age=3600")))
    response))

(deftype TttPostHandler []
  RouteHandler
  (handle [this request]
    (let [state (get-game-from-request request)]
      (handle-request state))))

(deftype TttViewHandler []
  RouteHandler
  (handle [this request]
    (let [state (get-game-from-request request)
          html  (create-html state)]
      (generate-get-response html state))))