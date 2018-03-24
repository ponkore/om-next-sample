(ns om-next-sample.text
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]]))

;; TODO spec による props の型付け
;;      それと didmount 時にチェックを入れる
;; 必須項目 :id
;; optional :default-value :on-change-fn [this new-value]
(def own-props [:on-change-fn])

(defui TextInput
  Object
  (componentWillMount [this]
    (let [{:keys [text]} (om/props this)]
      (om/update-state! this assoc
                        :composing? (atom false)
                        :old-val (atom text))))
  (componentWillUnmount [this]
    (let [{:keys [composing? old-val]} (om/get-state this)]
      (reset! old-val "")
      (reset! composing? false)))
  (render [this]
    (let [{:keys [on-change-fn]} (om/props this)
          on-change-fn (or on-change-fn (fn [_ _] ))
          {:keys [composing? old-val]} (om/get-state this)
          props (apply dissoc (om/props this) own-props)
          props (assoc props
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
                                         (on-change-fn this v))))))]
      (ui/text-field props))))
