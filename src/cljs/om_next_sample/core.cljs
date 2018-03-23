(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]
            [om-next-sample.text :as t]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]]))

(enable-console-print!)

(defonce app-state (atom {:root/text "Hello Chestnut!"
                          :root/textinput {:textinput/text "**init**"}
                          :root/foo-data {:foo/my-id "sample sample"}}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [state query] :as env} k params]
  {:value (if-let [v (get @state k)] v "not-found")})
;; (om/db->tree query v @state)

(defmethod mutate 'root/update-text
  [{:keys [state] :as env} _ {:keys [text]}]
  {:value {:keys [:root/textinput]}
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

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    `[:root/text
      {:root/textinput [:textinput/text]}
      {:root/foo-data ~(om/get-query Foo)}])
  Object
  (render [this]
    (let [{:keys [root/text root/foo-data root/textinput]} (om/props this)]
      (ui/mui-theme-provider
       {:mui-theme (ui/get-mui-theme)}
       (html
        [:div
         [:h1 text]
         [:br]
         (foo foo-data)
         (my-textinput (assoc textinput
                              :on-change-fn (fn [this text] (om/transact! this `[(root/update-text {:text ~text}) :root/text]))))
         ])))))

(defn render []
  (om/add-root! reconciler RootComponent (js/document.getElementById "app")))
