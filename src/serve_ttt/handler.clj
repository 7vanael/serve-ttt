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

;(defn grid->string [board]
;  (str (vec (flatten board))))
;
;(defn reconstruct-grid [flattened-data]
;  (let [count (count flattened-data)]
;    (cond
;      (= count 9) (->> flattened-data
;                       (partition 3)
;                       (mapv vec))
;
;      (= count 16) (->> flattened-data
;                        (partition 4)
;                        (mapv vec))
;      (= count 27) (->> flattened-data
;                        (partition 9)
;                        (mapv #(partition 3 %))
;                        (mapv #(mapv vec %)))
;      :else nil)))
;
;(defn string->grid [board-as-string]
;  (-> board-as-string
;      clojure.edn/read-string
;      vec
;      reconstruct-grid))
;
;(defn safe-name [attempt]
;  (when attempt (name attempt)))
;
;(defn state->cookies [{:keys [interface status active-player-index save players board] :as state}]
;  {"status"              (name status)
;   "active-player-index" (str active-player-index)
;   "save"                (safe-name save)
;   "interface"           (name interface)
;   "x-type"              (safe-name (get-in players [0 :play-type]))
;   "o-type"              (safe-name (get-in players [1 :play-type]))
;   "x-difficulty"        (safe-name (get-in players [0 :difficulty]))
;   "o-difficulty"        (safe-name (get-in players [1 :difficulty]))
;   "board"               (grid->string board)})
;
;(defn cookies->state [cookies]
;  (let [base-state (ttt-core/initial-state {:interface :web})]
;    (reduce (fn [state [coo-key cook-val]]
;              (cond (or (nil? cook-val) (= "nil" cook-val)) state
;                    (= coo-key "board") (assoc state :board (string->grid cook-val))
;                    (= coo-key "x-type") (assoc-in state [:players 0 :play-type] (keyword cook-val))
;                    (= coo-key "x-difficulty") (assoc-in state [:players 0 :difficulty] (keyword cook-val))
;                    (= coo-key "o-type") (assoc-in state [:players 1 :play-type] (keyword cook-val))
;                    (= coo-key "o-difficulty") (assoc-in state [:players 1 :difficulty] (keyword cook-val))
;                    (= coo-key "active-player-index") (assoc state :active-player-index (Integer/parseInt cook-val))
;                    (contains? base-state (keyword coo-key)) (assoc state (keyword coo-key) (keyword cook-val))
;                    :else state))
;            base-state
;            cookies))
;  )

(defn add-form-data-to-state [request state]
  (let [form-data (get-form-data request)
        response (-> form-data vals first keyword)]
      (cond-> state
              (seq form-data) (assoc :response response))))

(defn get-game-id-from-cookies [cookies]
  (when-let [game-id-str (get cookies "game-id")]
    (when-not (or (nil? game-id-str) (= "nil" game-id-str) (= "" game-id-str))
      (Integer/parseInt game-id-str))))

(defn get-game-from-request [request]
  (let [game-id (get-game-id-from-cookies (.getCookies request))
        base-state {:interface :web :save *save-method*}
        base-state-with-id (if game-id (assoc base-state :game-id game-id) base-state)
        loaded-state (ttt-core/load-game base-state-with-id)
        state-with-form-data (add-form-data-to-state request loaded-state)]
    state-with-form-data))

(defn generate-get-response [html state]
  (let [response (Response. (str server-name) (int 200) (str "text/html") (str html))
        ;cookies  (state->cookies state)
        ]
    (when-let [game-id  (:game-id state)]
      (.addCookie response (str "game-id=" game-id "; Path=/ttt; Max-Age=3600")))
    response))


(defn handle-request [state]
  (let [updated-state (core/save-game (process-input state))
        ;cookies       (state->cookies updated-state)
        response      (Response. (str server-name) (int 302) (str "text/plain") (str "Redirecting"))]
    (.addHeader response "Location" "/ttt/view")
    (when-let [game-id  (:game-id updated-state)]
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