(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]
            [om-next-sample.error-pane :as e]
            [om-next-sample.text :as t]
            [om-next-sample.util :as util]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]]))

(enable-console-print!)

(defonce app-state (atom {:error-info {:error/code "EE0001" :error/message "あいうえお"}
                          :root/text {:text "Hello Chestnut!"}
                          :root/textinput {:text "**init**"}
                          :root/foo-data {:foo/my-id "sample sample"}}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [state query ast] :as env} k params]
  (info "read" k ",q=" query)
  (if-let [v (get @state k)]
    {:value (om/db->tree query v @state)}
    {:value "not-found"}))

(defmethod read :root/text
  [{:keys [state query ast] :as env} k params]
  (info "**root/text read" k ", x=" (om/db->tree query (get @state k) @state))
  (if-let [v (get @state k)]
    {:value (om/db->tree query v @state) :remote true}
    {:value "not-found"}))

(defmethod mutate 'root/update-text
  [{:keys [state] :as env} _ {:keys [text]}]
  {:value {:keys [:root/textinput]}
   :action (fn [] (swap! state assoc-in [:root/textinput :text] text))})

(def parser (om/parser {:read read :mutate mutate}))

(def reconciler
  (om/reconciler
   {:state app-state
    :normalize true
    ;; :merge-tree (fn [a b] (debug "|merge" a b) (merge a b))
    :parser parser
    :send (util/transit-post "/api")}))

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

(def error-pane (om/factory e/ErrorPane))

(def my-textinput (om/factory t/TextInput))

(defui ^:once RootComponent
  static om/IQueryParams
  (params [this]
    {:param1 1 :param2 "2"})
  static om/IQuery
  (query [this]
    `[{(:root/text {:param1 ~'?param1 :param2 ~'?param2}) [:text]}
      {:error-info ~(om/get-query e/ErrorPane)}
      {:root/foo-data ~(om/get-query Foo)}
      {:root/textinput [:text]}])
  Object
  (render [this]
    (let [{:keys [error-info
                  root/text
                  root/foo-data
                  root/textinput]} (om/props this)
          text (:text text)]
      (ui/mui-theme-provider
       {:mui-theme (ui/get-mui-theme)}
       (html
        [:div
         (error-pane error-info)
         [:h1 text]
         [:br]
         (foo foo-data)
         (my-textinput (assoc textinput ;; must have {:text "string"}
                              :id "hoge"
                              :default-value (:text textinput)
                              :on-change-fn (fn [this text] (om/transact! this `[(root/update-text {:text ~text}) :root/text]))))
         ])))))

(defn render []
  (om/add-root! reconciler RootComponent (js/document.getElementById "app")))
