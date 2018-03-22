(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defui RootComponent
  Object
  (render [this]
    (html
     [:div
      [:h1
       (:text @app-state)]])))

(def root (om/factory RootComponent))

(defn render []
  (js/ReactDOM.render (root) (js/document.getElementById "app")))
