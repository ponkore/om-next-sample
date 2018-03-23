(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]
            [om-next-sample.text :as t]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]]))

(enable-console-print!)

(defonce app-state (atom {:root/text "Hello Chestnut!"
                          :root/foo-data {:foo/my-id "sample sample"}
                          :root/textinput {:textinput/text "init"}}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [state query] :as env} k params]
  {:value (let [_ (info "read env=" env)]
            (if-let [v (get @state k)]
              (let [_ (info "db->tree result=" (om/db->tree query v @state))]
                v)
              "not-found"))})

(defmethod mutate 'textinput/update-text
  [{:keys [state] :as env} _ {:keys [text]}]
  {:value {:keys [:textinput/text]}
   :action (fn [] (swap! state assoc-in [:root/textinput :textinput/text] text))})

(def parser (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler
   {:state app-state
    :normalize true
    ;; :merge-tree (fn [a b] (debug "|merge" a b) (merge a b))
    :parser parser}))

(defui Foo
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

(def my-textinput (om/factory t/TextInput))

#_(fn [this text]
    (om/transact! this `[(textinput/update-text {:text ~text}) :textinput/text]))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    `[:root/text
      {:root/textinput ~(om/get-query t/TextInput)}
      {:root/foo-data ~(om/get-query Foo)}])
  Object
  (render [this]
    (let [{:keys [root/text root/foo-data root/textinput]} (om/props this)]
      (html
       [:div
        [:h1
         text]
        (foo foo-data)
        [:br]
        (my-textinput textinput)]))))

(defn render []
  (om/add-root! reconciler RootComponent (js/document.getElementById "app")))
