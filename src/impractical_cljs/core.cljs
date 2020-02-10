(ns impractical-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [impractical-cljs.silly-names :refer [silly-name-component]]))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:current-project :silly-names}))

(def projects
  {:silly-names silly-name-component})

(defn app-container []
  [:div
   [:h1 "Impractical CLJS Projects"]
   [:div {:style {:display "flex"}}
    [:ul {:style {:flex "1"}}
     [:li
      {:on-click #(swap! app-state assoc :current-project :silly-names)
       :style {:cursor "pointer"}}
      "Silly names"]]
    [:div {:style {:flex "3"}}
     [(projects (:current-project @app-state))]]]])

(defn start []
  (reagent/render-component [app-container]
                            (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
