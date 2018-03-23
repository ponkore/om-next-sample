(ns om-next-sample.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]))

(enable-console-print!)

(defonce app-state (atom {:root/text "Hello Chestnut!"
                          :root/foo-data {:foo/my-id "sample sample"}
                          :root/textinput {:textinput/text "init"}}))

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

(defui ^:once TextInput
  static om/IQuery
  (query [this]
    [:textinput/text])
  Object
  (componentDidMount [this]
    (let [{:keys [textinput/text]} (om/props this)]
      (om/update-state! this assoc
                        :composing? (atom false)
                        :old-val (atom text))
      (when-not (empty? text)
        (let [this-node (-> (om/react-ref this "this-node-ref") dom/node)]
          (set! (.-value this-node) text)))))
  (componentWillUnmount [this]
    (let [{:keys [composing? old-val]} (om/get-state this)]
      (reset! old-val "")
      (reset! composing? false)))
  (render [this]
    (let [{:keys [textinput/text]} (om/props this)
          {:keys [composing? old-val]} (om/get-state this)]
      (html
       [:input {:type "text"
                :ref "this-node-ref"
                ;; :value (str text)
                :on-composition-start (fn [event] (reset! composing? true))
                :on-composition-end (fn [event]
                                      (let [v (-> event .-target .-value)]
                                        (reset! composing? false)
                                        (when-not (= v @old-val)
                                          (reset! old-val v)
                                          (om/transact! this `[(textinput/update-text {:text ~v})
                                                               :textinput/text]))))
                :on-input (fn [event]
                            (let [v (-> event .-target .-value)]
                              (when-not @composing?
                                (when-not (= v @old-val)
                                  (reset! old-val v)
                                  (om/transact! this `[(textinput/update-text {:text ~v})
                                                       :textinput/text])))))}]))))

(def my-textinput (om/factory TextInput))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    `[:root/text
      {:root/textinput ~(om/get-query TextInput)}
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
