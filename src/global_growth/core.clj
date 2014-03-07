(ns global-growth.core
  (:require [compojure.core :refer :all]
            [ring.middleware.params :refer :all]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [hiccup.form :as f]))


(defn remove-aggregate-countries
  "Remove all countries that aren't actually countries, but are aggregates."
  [countries]
  (remove (fn [country]
            (= (get-in country [:region :value]) "Aggregates")) countries))

(defn get-country-ids
  "Get set of country ids so we can filter out aggregate values."
  []
  (let [countries (remove-aggregate-countries (:results (get-api "/countries" {})))]
    (set (map :iso2Code countries))))


(def country-ids (get-country-ids))



;; WEB APP

(defn layout
  [title & content]
  (page/html5
   [:head
    [:title title]
    (page/include-css "//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap.min.css")
    (page/include-css "//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap-theme.min.css")
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
   [:body
    [:nav.navbar.navbar-default {:role "navigation"}
     [:div.container-fluid
      [:div.navbar-header
       [:a.navbar-brand {:href "/"} "World Bank Indicators"]]]]
    [:div.container
     content]]))

(defn ordered-list
  [coll]
  [:ol
   (for [list-item coll]
     [:li list-item])])

(defn format-indicator-value
  [value]
  (if (number? value)
    (format "%,.2f" (float value))
    (str value)))

(defn indicator-list
  [indicators]
  (ordered-list
   (for [country-pair indicators]
     (let [country (first country-pair)
           value (second country-pair)]
       (str country " (" (format-indicator-value value) ")")))))

(defn view-ind
  [indicator1 indicator2 year]
  (let [inds1 (sorted-indicator-map
                (<<FILL IN THE BLANK>>))
        inds2 (sorted-indicator-map
                (<<FILL IN THE BLANK>>))]
  (layout "Sorted Indicators"
          [:h1 "Sorted Indicators"]
          [:div.row
           [:div.form-group.col-md-6
            (f/label indicator1 (get (set/map-invert indicator-map) indicator1))
            (if (empty? inds1)
              [:p "No indicator values for this year."]
              (indicator-list inds1))
            ]
           [:div.form-group.col-md-6
            (f/label indicator2 (get (set/map-invert indicator-map) indicator2))
            (if (empty? inds2)
              [:p "No indicator values for this year."]
              (indicator-list inds2))]])))

(defn main-page []
  (layout "World Bank Indicators"
          [:h1 "World Bank Indicators"]
          [:p "Choose one of these world development indicators."]
          (f/form-to {:role "form"} [:get "/choose-ind"]
                     [:div.row
                      [:div.form-group.col-md-5
                       (f/label "indicator1" "Indicator 1:  ")
                       (f/drop-down {:class "form-control"} "indicator1" (seq indicator-map))]
                      [:div.form-group.col-md-5
                       (f/label "indicator2" "Indicator 2:  ")
                       (f/drop-down {:class "form-control"} "indicator2" (seq indicator-map))]
                      [:div.form-group.col-md-2
                       (f/label "year" "Year: ")
                       (f/drop-down {:class "form-control"} "year" (reverse (range 1960 2013)) 2010 )]]
                     (f/submit-button "Submit"))))

(defroutes main-routes
  (GET "/" [] (main-page))
  (GET "/choose-ind" [indicator1 indicator2 year]
        (view-ind indicator1 indicator2 year)))

(def handler (wrap-params main-routes))

