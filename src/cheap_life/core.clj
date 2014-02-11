(ns cheap-life.core
  (:use [compojure.core]
        [ring.middleware.params])
  (:require [clojure.set :as set]
            [clojure.pprint :as pp]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [hiccup.form :as f]))

;TODO: handle paging: right now just getting a large value
; for 'per_page' parameter.
; Maybe that is fine for simplicity. But that could be a
; problem if we get things with large result sets. Not
; getting large results as of now.

;; CONSTANTS
(def BASE-URI "http://api.worldbank.org")
(def GAS-INDICATOR "EP.PMP.SGAS.CD")
(def GDP-INDICATOR "NY.GDP.PCAP.CD")

(defn parse-json [str]
  (json/parse-string str true))

;; WORLD BANK API CALLS
(defn get-api
  "Returns json object representing API response"
  [path qp]
  (let
    [base-path (str BASE-URI path)
     query-params (merge qp {:format "json" :per_page 10000})]
    (parse-json (:body
                  (client/get base-path {:query-params query-params})))))

(defn get-value
  "Returns single value from API response"
  [path query-params key]
  (get-in (last (get-api path query-params)) [0 key]))

(defn get-value-map
  "Returns relation of two keys from API response"
  [path query-params key1 key2]
  (let
    [result (get-api path query-params)]
    (into {} (map (fn[x] {(key1 x) (key2 x)}) (last result)))))

(defn get-indicator-map []
  "Gets map of indicators.
  /sources/2/indicators:   All world development indicators (about 1300)
  --- Other possibilities ---
  /topics/16/indicators:   All urban development
  /indicators:             All Indicators (about 8800)"
  (get-value-map "/sources/2/indicators" {} :name :id))

(def indicator-map (get-indicator-map))

(defn get-indicator-all
  "Returns indicator for a specified year for all countries"
  [indicator year key1 key2]
  (get-value-map (str "/countries"
                      "/all"
                      "/indicators"
                      "/" indicator)
                 {:date (str year)}
                  key1
                  key2))

(defn get-indicator
  "Returns indicator for a country for a specified year"
  [indicator country year key]
  (get-value (str "/countries"
                  "/" country
                  "/indicators"
                  "/" indicator)
             {:date year}
             key ))

(defn sorted-indicator-map
  "Sort the map of indicator numeric values"
  [inds]
  (sort-by val >
           (into {} (for [[k v] inds
                          :when (not (nil? v))]
                      [(:value k) (read-string v)]))))


; These are examples. May want to take these out unless
; they serve as useful examples for learning.
(defn get-gas
  "Returns the pump price for gasoline (US$ per liter)
	in a country for a specified year"
  [country year]
  (get-indicator GAS-INDICATOR country year :value))

(defn get-gdp
  "Returns the GDP per capita (current US$)
        in a country for a specified year"
  [country year]
  (get-indicator GDP-INDICATOR country year :value))

(defn get-gdp-all
  "Returns the GDP per capita (current US$)
        for a specified year for all countries"
  [year]
  (get-indicator-all GDP-INDICATOR year :value))


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
    [:div.container
     content]]))

(defn view-ind
  [indicator1 indicator2 year]
  (let [inds1 (sorted-indicator-map
                (get-indicator-all indicator1 year :country :value))
        inds2 (sorted-indicator-map
                (get-indicator-all indicator2 year :country :value))]
  (layout "Sorted Indicators"
          [:h1 "Sorted Indicators"]
          [:div.row
           [:div.form-group.col-md-5
            (f/label indicator1 (get (set/map-invert indicator-map) indicator1))[:br]
            (if (empty? inds1)
              [:p "No indicator values for this year."]
              (f/drop-down {:class "form-control" :size 10} indicator1 inds1))
            ]
           [:div.form-group.col-md-5
            (f/label indicator2 (get (set/map-invert indicator-map) indicator2))[:br]
            (f/drop-down {:class "form-control" :size 10} indicator2 inds2)]])))

(defn main-page []
  (layout "World Bank Indicators"
          [:h1 "World Bank Indicators"]
          [:p "Choose one of these world development indicators."]
          (f/form-to {:role "form"} [:post "/choose-ind"]
                     [:div.row
                      [:div.form-group.col-md-5
                       (f/label "indicator1" "Indicator 1:  ")
                       (f/drop-down {:class "form-control"} "indicator1" (seq indicator-map))]
                      [:div.form-group.col-md-5
                       (f/label "indicator2" "Indicator 2:  ")
                       (f/drop-down {:class "form-control"} "indicator2" (seq indicator-map))]
                      [:div.form-group.col-md-2
                       (f/label "year" "Year: ")
                       (f/drop-down {:class "form-control"} "year" (reverse (range 1960 2013)))]]
                     (f/submit-button "Submit"))))

(defroutes main-routes
  (GET "/" [] (main-page))
  (POST "/choose-ind" [indicator1 indicator2 year]
        (view-ind indicator1 indicator2 year)))

(def handler (wrap-params main-routes))

