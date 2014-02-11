(ns kodemaker-no.pages.project-pages
  (:require [clojure.string :as str]
            [kodemaker-no.formatting :refer [comma-separated year-range]]
            [kodemaker-no.homeless :refer [compare*]]
            [kodemaker-no.markup :refer [link-if-url]]))

(defn- render-person [{:keys [url thumb full-name description years]}]
  [:div.media
   [:a.img.thumb.mts {:href url}
    [:img {:src thumb}]]
   [:div.bd
    [:h4.mtn full-name " " [:span.tiny.shy (year-range years)]]
    [:p description]]])

(defn- compare-by-years [a b]
  (or (compare* (count (:years b))
                (count (:years a)))
      (compare* (apply min (:years a))
                (apply min (:years b)))
      0))

(defn- render-people [people project]
  (list [:h2 "Våre folk på saken"]
        (->> (:people project)
             (sort compare-by-years)
             (map render-person))))

(defn- render-reference-quote [{:keys [photo author title quote email phone]} _]
  (list
   [:h2 "Hva sier kunden?"]
   [:div.media
    (when photo [:img.img.thumb.mts {:src photo}])
    [:div.bd
     [:h4.mtn author]
     (when title [:p.near title])
     [:p.near [:q quote]]]]))

(defn- render-endorsement [{:keys [photo author person title quote]}]
  [:div.media
   (when photo [:img.img.thumb.mts {:src photo}])
   [:div.bd
    [:h4.mtn author
     [:span.tiny " om "
      [:a {:href (:url person)} (:first-name person)]]]
    (when title [:p.near title])
    [:p [:q quote]]]])

(defn- render-endorsements [endorsements _]
  (list [:h2 "Referanser"]
        (map render-endorsement endorsements)))

(defn- render-tech [tech _]
  (list [:h3 "Teknologi"]
        [:p (comma-separated (map link-if-url tech)) "."]))

(defn- render-illustration [_ {:keys [site illustration]}]
  [:p [:a {:href site} [:img {:src illustration}]]])

(defn- strip-protocol [s]
  (str/replace s #"^[a-z]+://" ""))

(defn- maybe-include [project kw f]
  (when (kw project)
    (f (kw project) project)))

(defn- render-related-projects [projects project]
  (list [:h4 "Relaterte prosjekter"]
        [:ul
         (map #(list [:li [:a {:href (:url %)} (:name %)]]) projects)]))

(defn- project-page [project]
  {:title {:head (:name project)}
   :illustration (:logo project)
   :lead [:p (:description project)]
   :aside (list
           (maybe-include project :illustration render-illustration)
           (maybe-include project :related-projects render-related-projects))
   :body (list
          (maybe-include project :tech render-tech)
          (maybe-include project :reference render-reference-quote)
          (maybe-include project :people render-people)
          (maybe-include project :endorsements render-endorsements))})

(defn project-pages [projects]
  (into {} (map (juxt :url #(partial project-page %)) projects)))
