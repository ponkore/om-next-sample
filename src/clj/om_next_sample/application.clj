(ns om-next-sample.application
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.middleware :refer [new-middleware]]
            [system.components.jetty :refer [new-web-server]]
            [om-next-sample.config :refer [config]]
            [om-next-sample.routes :refer [home-routes]]))

(defn app-system [config]
  (component/system-map
   :routes     (new-endpoint home-routes)
   :middleware (new-middleware {:middleware (:middleware config)})
   :handler    (-> (new-handler)
                   (component/using [:routes :middleware]))
   :http       (-> (new-web-server (:http-port config))
                   (component/using [:handler]))))

(defn -main [& _]
  (let [config (config)]
    (-> config
        app-system
        component/start)
    (println "Started om-next-sample on" (str "http://localhost:" (:http-port config)))))
