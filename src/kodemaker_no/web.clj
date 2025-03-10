(ns kodemaker-no.web
  (:require [clojure.core.memoize]
            [clojure.data.json :as json]
            [config :refer [export-directory]]
            [datomic-type-extensions.api :as d]
            [html5-walker.core :as html5-walker]
            [imagine.core :as imagine]
            [kodemaker-no.atomic :as atomic]
            [kodemaker-no.homeless :refer [wrap-content-type-utf-8]]
            [kodemaker-no.images :as images]
            [kodemaker-no.ingest :as ingest]
            [kodemaker-no.new-pages.blog :as blog]
            [kodemaker-no.rss :as rss]
            [optimus-img-transform.core :refer [transform-images]]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies]
            [prone.middleware :as prone]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [stasis.core :as stasis]))

(defn get-assets []
  (concat
   (assets/load-assets
    "public"
    [
     ;; ny
     "/css/kodemaker.css"
     "/css/pygments.css"
     #"/img/.*\..+"
     #"/foto/.*\..+"
     #"/fonts/.*\..+"
     #"/icons/.*\..+"

     ;; gammal
     #"/styles/.*\.css"
     "/favicon.ico"
     #"/certificates/.*\.pdf"
     #"/illustrations/.*\.jpg"
     #"/illustrations/.*\.png"
     #"/thumbs/.*\.jpg"
     #"/forside/.*\.jpg"
     #"/references/.*\.jpg"
     #"/fullsize-photos/.*\.jpg"
     #"/photos/.*\.jpg"
     #"/photos/.*\.svg"
     #"/photos/.*\.png"
     #"/logos/.*\.png"
     #"/logos/.*\.svg"
     #"/images/.*\.png"
     #"/images/.*\.jpg"
     #"/images/blogg/.*\.png"
     #"/images/blogg/.*\.gif"
     #"/videos/.*\.mp4"])
   (assets/load-bundle
    "public"
    "styles.css"
    ["/css/kodemaker.css"
     "/css/pygments.css"])))

(def optimize
  (-> (fn [assets options]
        (-> (map #(assoc % :context-path "/assets") assets)
            (transform-images {:regexp #"/fullsize-photos/.*\.jpg"
                               :quality 0.3
                               :width (* 920 2)
                               :progressive true})
            (transform-images {:regexp #"/photos/.*\.jpg"
                               :quality 0.3
                               :width (* 290 2)
                               :progressive true})
            (transform-images {:regexp #"/references/.*\.jpg"
                               :quality 0.3
                               :width (* 680 2)
                               :progressive true})
            (transform-images {:regexp #"/illustrations/.*\.jpg"
                               :quality 0.3
                               :width (* 210 2)
                               :progressive true})
            (transform-images {:regexp #"/thumbs/.*\.jpg"
                               :quality 0.3
                               :width (* 100 2)
                               :progressive false}) ; too small, will be > kb
            (optimizations/all options)
            (->> (remove :bundled)
                 (remove :outdated))))
      (clojure.core.memoize/lru {} :lru/threshold 3)))

(defn create-app [& [opts]]
  (-> (atomic/serve-pages (:conn opts))
      (wrap-resource "videos")
      (imagine/wrap-images images/image-asset-config)
      (optimus/wrap get-assets optimizations/none optimus.strategies/serve-live-assets-autorefresh)
      wrap-content-type
      wrap-content-type-utf-8
      prone/wrap-exceptions))

(defn extract-style-urls [node]
  (some->> (.getAttribute node "style")
           (re-seq #"url\((.+?)\)")
           (map second)))

(defn extract-images [html]
  (-> (for [node (html5-walker/find-nodes html [:img])]
        (.getAttribute node "src"))
      (into (mapcat extract-style-urls (html5-walker/find-nodes html [:.w-style-img])))
      (into (mapcat extract-style-urls (html5-walker/find-nodes html [:.section])))
      (into (->> (html5-walker/find-nodes html [:meta])
				         (filter #(= "og:image" (.getAttribute % "property")))
					       (map #(.getAttribute % "content"))))))

(defn get-images [pages-dir]
  (->> (stasis/slurp-directory pages-dir #"\.html+$")
       vals
       (mapcat extract-images)
       (into #{})))

(defn get-image-assets [pages-dir asset-config]
  (->> (get-images pages-dir)
       (filter #(imagine/image-url? % asset-config))))

(defn export-images [pages-dir dir asset-config]
  (doseq [image (get-image-assets pages-dir asset-config)]
    (-> image
        imagine/image-spec
        (imagine/inflate-spec asset-config)
        (imagine/transform-image-to-file (str dir image)))))

(defn- load-export-dir []
  (stasis/slurp-directory export-directory #"\.[^.]+$"))

(defn export-new [& args]
  (let [[format] (map read-string args)
        assets (optimize (get-assets) {})
        old-files (load-export-dir)
        request {:optimus-assets assets}
        conn (atomic/create-database (str "datomic:mem://" (d/squuid)))]
    (ingest/ingest-all conn "resources")
    (stasis/empty-directory! export-directory)
    (optimus.export/save-assets assets export-directory)
    (stasis/export-pages (atomic/get-pages (d/db conn) request) export-directory request)
    (spit (str export-directory "atom.xml") (rss/atom-xml (blog/blog-posts-by-published (d/db conn))))
		(println "Exporting images from <img> <meta property=\"og:image\"> and select style attributes")
    (export-images export-directory export-directory (assoc images/image-asset-config :cacheable-urls? true))
    (if (= format :json)
      (println (json/write-str (dissoc (stasis/diff-maps old-files old-files) :unchanged)))
      (do
        (println)
        (println "Export complete:")
        (stasis/report-differences old-files old-files)
        (println)))))

(comment
  (export-images "./build/" "./build/" images/image-asset-config)

  (get-image-assets "./build/" images/image-asset-config)

  (export-new)

  (-> "/image-assets/mega-banner/_/references/geir-oterhals.jpg"
      imagine/image-spec
      (imagine/inflate-spec images/image-asset-config)
      (imagine/transform-image-to-file "ui/resources/public/devcard_images/geir.jpg"))

  )
