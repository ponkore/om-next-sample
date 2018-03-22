(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]))

(enable-console-print!)

(defonce app-state (atom {:root/text "Hello Chestnut!"
                          :root/foo-data {:foo/my-id "sample sample"}}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} k params]
  {:value (let [_ (.log js/console "read key=" (name k) " => " (get @state k))]
            (get @state k))})

(def parser (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler
   {:state app-state
    :normalize true
    ;; :merge-tree (fn [a b] (debug "|merge" a b) (merge a b))
    :parser parser}))

(defui ^:once Foo
  static om/IQuery
  (query [this]
    [:foo/my-id])
  Object
  (render [this]
    (let [{:keys [foo/my-id]} (om/props this)]
      (html
       [:div
        [:h2 (str my-id)]]))))

(def foo (om/factory Foo))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    `[:root/text
      {:root/foo-data ~(om/get-query Foo)}])
  Object
  (render [this]
    (let [{:keys [root/text root/foo-data]} (om/props this)]
      (html
       [:div
        [:h1
         text]
       (foo foo-data)]))))

(defn render []
  (om/add-root! reconciler RootComponent (js/document.getElementById "app")))
