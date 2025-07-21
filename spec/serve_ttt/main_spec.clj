(ns serve-ttt.main-spec
  (:require [speclj.core :refer :all]
            [serve-ttt.main :refer :all]))

(describe "main serve-ttt"
  (with-stubs)
  (it "has a router with routes"
    (let [routes (.getRoutes router)]
      (should= 7 (count routes)))))
