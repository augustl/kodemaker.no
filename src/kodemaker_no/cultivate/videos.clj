(ns kodemaker-no.cultivate.videos
  (:require [kodemaker-no.cultivate.util :as util]
            [kodemaker-no.formatting :refer [to-id-str]]
            [kodemaker-no.homeless :refer [update-in-existing update-vals compare*]]))

(defn find-video [^String url]
  (when url
    (cond
     (.startsWith url "http://www.youtube.com/watch?v=") {:type :youtube, :id (subs url 31)}
     (.startsWith url "http://vimeo.com/album/") {:type :vimeo, :id (second (re-find #"http://vimeo.com/album/\d+/video/(\d+)" url))}
     (re-find #"https?://vimeo.com/\d+" url) {:type :vimeo, :id (second (re-find #"https?://vimeo.com/(\d+)" url))}
     (re-find #"https?://vimeo.com/user\d+/review/\d+/\S+" url) {:type :vimeo, :id (second (re-find #"https?://vimeo.com/user\d+/review/(\d+)/\S+" url))}
     :else nil)))

(defn- create-embed-code [url]
  (let [{:keys [type id]} (find-video url)]
    (case type
      :youtube [:div.video-embed
                [:iframe {:src (str "http://www.youtube.com/embed/" id)
                          :frameborder "0"
                          :allowfullscreen true}]]
      :vimeo [:div.video-embed
              [:iframe {:src (str "//player.vimeo.com/video/" id "?title=0&amp;byline=0&amp;portrait=0")
                        :frameborder "0"
                        :allowfullscreen true}]]
      nil)))

(defn- create-video-page-for-presentation? [presentation]
  (and (not (:direct-link? presentation))
       (find-video (:video (:urls presentation)))))

(defn- video-id [video]
  (or (:id video)
      (keyword (to-id-str (:title video)))))

(defn- video-url [video]
  (if (create-video-page-for-presentation? video)
    (str "/" (name (video-id video)) "/")
    (-> video :urls :video)))

(defn- slide-url [video]
  (get-in video [:urls :slides]))

(defn- cultivate-video [raw-content {:keys [blurb title urls by tech date] :as video}]
  (let [override (-> raw-content :video-overrides (get (video-id video)))]
    (merge
     {:title title
      :by by
      :blurb blurb
      :date date
      :tech (map (partial util/look-up-tech raw-content) tech)
      :url  (or (video-url video) (slide-url video))
      :embed-code (create-embed-code (:video urls))
      :direct-link? (not (create-video-page-for-presentation? video))}
     override)))

(defn- get-with-byline [key]
  (fn [person]
    (->> (key person)
         (map #(assoc % :by {:name (first (:name person))
                             :url (util/url person)})))))

(defn- add-occurrence [v1 v2]
  (-> (if (map? (:by v1))
        (assoc v1 :by [(:by v1)])
        v1)
      (update-in [:by] conj (:by v2))))

(defn- combine-videos [videos]
  (-> (first videos)
      (assoc
          :by (map :by videos))))

(defn replace-presentation-video-urls-1 [pres]
  (if (create-video-page-for-presentation? pres)
    (assoc-in pres [:urls :video] (video-url pres))
    pres))

(defn replace-video-urls [m]
  (update-in-existing m [:presentations] #(map replace-presentation-video-urls-1 %)))

(defn compare-by-datetime-and-title [a b]
  (or (compare* (-> b :date)
                (-> a :date))
      (compare* (:title a)
                (:title b))
      0))

(defn cultivate-videos [raw-content]
  (->> raw-content :people vals
       (mapcat (get-with-byline :presentations))
       (map (partial cultivate-video raw-content))
       (group-by :url)
       vals
       (map combine-videos)
       (sort compare-by-datetime-and-title)))
