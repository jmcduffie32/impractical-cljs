(ns impractical-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [impractical-cljs.silly-names :refer [silly-name-component]]
            [impractical-cljs.visualization :refer [init-viz]]))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:current-project :silly-names
                          :game {:apples '([250 250])
                                 :snake {:body '([100 100])
                                         :dir [1 0]}}}))

(def initial-game-state {:apples '([250 250])
                         :snake {:body '([100 100])
                                 :dir [1 0]}})

(defn init-game [game-state]
  (swap! app-state #(assoc % :game game-state)))

(def projects
  {:silly-names silly-name-component})

(defn apple [[x y]]
  [:circle {:cx (str x) :cy (str y) :r "4"}])

(defn apples []
  [:g
   (map apple (get-in @app-state [:game :apples]))])

(defn snake []
  [:g
   (for [[x y] (get-in @app-state [:game :snake :body])]
     [:rect {:x (- x 4) :y (- y 4) :height 8 :width 8}])])

(defn move-snake [body dir]
  (println clj->js body)
  (let [deltax (* (dir 0) 8)
        deltay (* (dir 1) 8)]
    (map (fn [[x y]] [(+ x deltax) (+ y deltay)])  body)))

(defn tick []
  (swap! app-state (fn [{:keys [game] :as state}]
                     (let [body (get-in game [:snake :body])
                           dir (get-in game [:snake :dir])]
                       (assoc-in state
                                 [:game :snake :body]
                                 (move-snake body dir))))))


(defn handle-key-press [e]
  (let [keyCode (.-code e)
        newDirection (case keyCode
                       "ArrowUp"    [0 -1]
                       "ArrowDown"  [0 1]
                       "ArrowRight" [1 0]
                       "ArrowLeft"  [-1 0])]
    (swap! app-state #(assoc-in % [:game :snake :dir] newDirection))))

(defn listen-for-keypresses []
  (.addEventListener js/document "keydown" handle-key-press))

(defn app-container []
  [:svg {:height "500px" :width "500px"}
   [apples]
   [snake]]
  ;; [:div
  ;;  [:h1 "Impractical CLJS Projects"]
  ;;  [:div {:style {:display "flex"}}
  ;;   [:ul {:style {:flex "1"}}
  ;;    [:li
  ;;     {:on-click #(swap! app-state assoc :current-project :silly-names)
  ;;      :style {:cursor "pointer"}}
  ;;     "Silly names"]]
  ;;   [:div {:style {:flex "3"}}
  ;;    [(projects (:current-project @app-state))]]]]
  )

(defn start []
  (do 
    ;; (init-viz)
    (reagent/render-component [app-container]
                              (. js/document (getElementById "app")))
    (init-game initial-game-state)
    (listen-for-keypresses)
    (js/setInterval tick 1000)
    ))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
