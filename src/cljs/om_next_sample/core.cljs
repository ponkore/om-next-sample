(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]))

(enable-console-print!)

(defonce app-state (atom {:root/text "Hello Chestnut!"
                          :root/foo-data {:foo/my-id "sample sample"}
                          :root/textinput {:textinput/value "init"}}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [state query] :as env} k params]
  {:value (let [_ (.log js/console "read env keys=" (str (keys env)))]
            (if-let [v (get @state k)]
              (let [_ (.log js/console "db->tree result=" (str (om/db->tree query v @state)))]
                v)
              "not-found"))})

(defmethod mutate 'textinput/update-text
  [{:keys [state] :as env} _ {:keys [value]}]
  {:value {:keys [:textinput/value]}
   :action (fn [] (swap! state assoc-in [:root/textinput :textinput/value] value))})

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

;; TODO: composing ...
(defui ^:once TextInput
  static om/IQuery
  (query [this]
    [:textinput/value])
  Object
  (render [this]
    (let [{:keys [textinput/value]} (om/props this)]
      (html
       [:input {:type "text"
                :on-change (fn [event]
                             (om/transact! this `[(textinput/update-text {:value ~(-> event .-target .-value)})
                                                  :textinput/value]))}
        value]))))

(def my-textinput (om/factory TextInput))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    `[:root/text
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
