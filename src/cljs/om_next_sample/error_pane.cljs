(ns om-next-sample.error-pane
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :refer [html]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]]))

(defui ErrorPane
  static om/IQuery
  (query [this]
    '[:error/code :error/message])
  Object
  (render [this]
    (let [{:keys [error/code error/message]} (om/props this)]
      (html
       [:div
        [:h3 (str code)]
        [:h4 (str message)]]))))
