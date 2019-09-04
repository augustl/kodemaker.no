(ns ui.sections.seymour-section
  (:require [ui.elements :as e]
            [ui.layout :as l]))

(defn render [{:keys [seymours]}]
  [:div.section {:style (l/add-pønt {} [{:kind :greater-than
                                         :position "bottom -550px left -310px"}
                                        {:kind :ascending-line
                                         :position "top -500px right -440px"}])}
   [:div.content.whitespaceorama
    [:div.trigrid
     (for [seymour seymours]
       [:div
        (e/seymour seymour)])]]])
