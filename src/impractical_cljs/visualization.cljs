(ns impractical-cljs.visualization
  (:require ["d3" :as d3]
            [impractical-cljs.data :refer [data]]))

(def height 600)
(def width 960)

(def color (.scaleOrdinal d3 (.-schemeCategory10 d3)))

(def simulation
  (-> d3
      (.forceSimulation)
      (.force "link" (-> (.forceLink d3)
                         (.id #(.-id %))))
      (.force "charge" (.forceManyBody d3))
      (.force "center" (.forceCenter d3 (/ width 2) (/ height 2)))))

(defn drag-started [d]
  (if (zero? (.. d3 -event -active))
    (.restart (.alphaTarget simulation 0.3)))
  (set! (.-fx d) (.-x d))
  (set! (.-fy d) (.-y d)))

(defn dragged [d]
  (set! (.-fx d) (.. d3 -event -x))
  (set! (.-fy d) (.. d3 -event -y)))

(defn drag-ended [d]
  (if (zero? (.. d3 -event -active))
    (.alphaTarget simulation 0))
  (set! (.-fx d) nil)
  (set! (.-fy d) nil))

(defn generate-graph [data]
  (let [graph (clj->js data)
        svg (.select d3 "svg")
        link (-> svg
                 (.append "g")
                 (.attr "class" "links")
                 (.selectAll "line")
                 (.data (.-links graph))
                 (.enter)
                 (.append "line")
                 (.attr "stroke-width" (fn [d] (Math/sqrt (.-value d)))))
        node (doto
                 (-> svg
                     (.append "g")
                     (.attr "class" "nodes")
                     (.selectAll "circle")
                     (.data (.-nodes graph))
                     (.enter)
                     (.append "circle")
                     (.attr "r" 5)
                     (.attr "fill" #(color (.-group %)))
                     (.call (-> d3
                                (.drag)
                                (.on "start" drag-started)
                                (.on "drag" dragged)
                                (.on "end" drag-ended))))
               (.append "text")
               (.text (fn [d] (.-id d))))]
    (-> simulation
        (.nodes (.-nodes graph))
        (.on "tick" (fn []
                      (-> link
                          (.attr "x1" #(.. % -source -x))
                          (.attr "y1" #(.. % -source -y))
                          (.attr "x2" #(.. % -target -x))
                          (.attr "y2" #(.. % -target -y)))
                      (-> node
                          (.attr "cx" (fn [d] (.-x d)))
                          (.attr "cy" (fn [d] (.-y d)))))))
    (-> simulation
        (.force "link")
        (.links (.-links graph)))))

(defn init-viz []
  (-> d3
      (.select "#graph")
      (.selectAll "*")
      (.remove))
  (-> d3
      (.select "#graph")
      (.append "svg")
      (.attr "height" height)
      (.attr "width" width))
  (generate-graph data))
