(ns serve-ttt.mock-request-spec
  (:import [Connection RequestI]
           (java.util HashMap Map)))

(defn mock-request
  [method path & {:keys [error-code headers body query-string valid?]
                  :or {error-code 0
                       headers {}
                       body (byte-array 0)
                       query-string nil
                       valid? true}}]
  (let [headers-map (HashMap. ^Map headers)
        cookies-map (HashMap.)]
    (reify RequestI
      (getMethod [_] method)
      (getPath [_] path)
      (getErrorCode [_] error-code)
      (isValid [_] valid?)
      (getBody [_] body)
      (getQueryString [_] query-string)
      (getHeaders [_] headers-map)
      (getHeader [_ name] (.get headers-map name))
      (getCookies [_] cookies-map))))
