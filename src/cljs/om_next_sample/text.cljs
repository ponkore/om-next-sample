(ns om-next-sample.text
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]))

(defui TextInput
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
    (let [{:keys [composing? old-val]} (om/get-state this)]
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
                                          (om/transact! this `[(textinput/update-text {:text ~v}) :textinput/text]))))
                :on-input (fn [event]
                            (let [v (-> event .-target .-value)]
                              (when-not @composing?
                                (when-not (= v @old-val)
                                  (reset! old-val v)
                                  (om/transact! this `[(textinput/update-text {:text ~v}) :textinput/text])))))}]))))

;; parser から切り離した版(使い回し可能)
(defui TextInput2
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
    (let [{:keys [on-change-fn]} (om/props this)
          on-change-fn (if on-change-fn on-change-fn (fn [_ _] ))
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
                                          (on-change-fn this v))))
                :on-input (fn [event]
                            (let [v (-> event .-target .-value)]
                              (when-not @composing?
                                (when-not (= v @old-val)
                                  (reset! old-val v)
                                  (on-change-fn this v)))))}]))))
