(ns om-next-sample.routes
  (:require [clojure.java.io :as io]
            [bidi.bidi :as bidi]
            [bidi.ring :refer [resources]]
            [ring.util.response :refer [response file-response resource-response redirect]]
            [om.next.server :as om]
            [taoensso.timbre :as timbre :refer [log trace debug info warn error fatal]]))

(defmulti mutatef om/dispatch)
(defmulti readf om/dispatch)

(defmethod readf :default
  [{:keys [state query] :as env} k params]
  (info "state=" state ",query=" query ",env=" env ",k=" k ",params=" params)
  {:value {:text "abc"}})

(def parser (om/parser {:read readf :mutate mutatef}))

(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
   :body    data})

(defn api
  [req]
  (let [{:keys [state]} req]
    (generate-response
      (parser {:state state} (:remote (:transit-params req))))))

(defn index-handler
  [req]
  (assoc (resource-response "index.html" {:root "public"})
         :headers {"Content-Type" "text/html; charset=UTF-8"}))

(def routes ["/" {"" {:get index-handler}
                  "css" {:get (resources {:prefix "public/css/"})}
                  "js" {:get (resources {:prefix "public/js/"})}
                  "api" {:post {[""] api}}}])

(defn home-routes
  [endpoint]
  routes)
