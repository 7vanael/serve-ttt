(ns serve-ttt.handler
  (:require [clojure.string :as str]
            [tic-tac-toe.core :as ttt-core]
            [serve-ttt.html :refer [create-html]]
            [serve-ttt.core :refer [server-name]]
            [serve-ttt.web-interface])
  (:import [Main RouteHandler]
           [Connection Response Request]))

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

(defn grid->string [board]
  (str (vec (flatten board))))

(defn reconstruct-grid [flattened-data]
  (let [count (count flattened-data)]
    (cond
      (= count 9) (->> flattened-data
                       (partition 3)
                       (mapv vec))

      (= count 16) (->> flattened-data
                        (partition 4)
                        (mapv vec))
      (= count 27) (->> flattened-data
                        (partition 9)
                        (mapv #(partition 3 %))
                        (mapv #(mapv vec %)))
      :else nil)))

(defn string->grid [board-as-string]
  (-> board-as-string
      clojure.edn/read-string
      vec
      reconstruct-grid))

(defn safe-name [attempt]
  (when attempt (name attempt)))

(defn state->cookies [{:keys [interface status active-player-index save players board] :as state}]
  {"status"              (name status)
   "active-player-index" (str active-player-index)
   "save"                (name save)
   "interface"           (name interface)
   "x-type"              (safe-name (get-in players [0 :play-type]))
   "o-type"              (safe-name (get-in players [1 :play-type]))
   "x-difficulty"        (safe-name (get-in players [0 :difficulty]))
   "o-difficulty"        (safe-name (get-in players [1 :difficulty]))
   "board"               (grid->string board)})

(defn cookies->state [cookies]
  (let [base-state (ttt-core/initial-state {:interface :web :save :sql})]
    (reduce (fn [state [coo-key cook-val]]
              (cond (or (nil? cook-val) (= "nil" cook-val)) state
                    (= coo-key "board") (assoc state :board (string->grid cook-val))
                    (= coo-key "x-type") (assoc-in state [:players 0 :play-type] (keyword cook-val))
                    (= coo-key "x-difficulty") (assoc-in state [:players 0 :difficulty] (keyword cook-val))
                    (= coo-key "o-type") (assoc-in state [:players 1 :play-type] (keyword cook-val))
                    (= coo-key "o-difficulty") (assoc-in state [:players 1 :difficulty] (keyword cook-val))
                    (= coo-key "active-player-index") (assoc state :active-player-index (Integer/parseInt cook-val))
                    (contains? base-state (keyword coo-key)) (assoc state (keyword coo-key) (keyword cook-val))
                    :else state))
            base-state
            cookies))
  )

(defn add-form-data-to-state [request state]
  (let [form-data (get-form-data request)
        response (-> form-data vals first keyword)]
      (cond-> state
              (seq form-data) (assoc :response response))))

(defn get-game-from-request [request]
  (let [base-state           (cookies->state (.getCookies request))
        state-with-form-data (add-form-data-to-state request base-state)]
    state-with-form-data))

(defn generate-response [html state]
  (let [cookies  (state->cookies state)
        response (Response. (str server-name) (int 200) (str "text/html") (str html))]
    (doseq [[k v] cookies]
      (.addCookie response (str k "=" v "; Path=/ttt; Max-Age=3600")))
    response))


(defn write-html-file [html status]
  (let [filename  (str (name status) ".html")
        file-path (str "testroot/ttt/" filename)]
    (try
      (spit file-path html)
      filename
      (catch Exception e
        (println "Error writing file:" (.getMessage e))
        nil))))

(defn handle-request [state & [write-fn]]
  (let [;write-fn      (or write-fn write-html-file)
        updated-state (ttt-core/update-state state (:response state))
        ;html          (create-html updated-state)
        ;filename      (write-fn html (:status updated-state))
        ;location      (str "/ttt/" filename)
        ;location      "/ttt/view" ;; this handler would load state from cookie, generate HTML, and send HTML in response
        cookies       (state->cookies updated-state)
        response      (Response. (str server-name) (int 302) (str "text/plain") (str "Redirecting"))]
    (.addHeader response "Location" "/ttt/view")
    (doseq [[k v] cookies]
      (.addCookie response (str k "=" v "; Path=/ttt; Max-Age=3600")))
    response))

(deftype TttPostHandler []
  RouteHandler
  (handle [this request]
    (let [state (get-game-from-request request)]
      (handle-request state))))

(deftype TttViewHandler []
  RouteHandler
  (handle [this request]
    (let [state (cookies->state (.getCookies request))
          html  (create-html state)]
      (generate-response html state))))