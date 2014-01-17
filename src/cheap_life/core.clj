(ns cheap-life.core
  (:use [compojure.core]
        [ring.middleware.params])
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :as hiccup]
            [hiccup.form :as f]))


;; CONSTANTS
(def BASE-URI "http://api.worldbank.org")
(def GAS-INDICATOR "EP.PMP.SGAS.CD")
(def GDP-INDICATOR "NY.GDP.PCAP.CD")

(defn parse-json [str]
  (json/parse-string str true))

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

(defn get-value-list 
  "Returns list of values for a key from API response"
  [path query-params key1 key2]
  ;TODO: handle paging
  (let 
    [result (get-api path query-params)]
    (map vector
      (map key1 (last result))
      (map key2 (last result)))))

(defn get-value-map 
  "Returns map of values from API response"
  [path query-params]
  ;TODO: handle paging
  (last (get-api path query-params)))

(defn get-indicator-list []
  (get-value-list "/indicators" {} :name :id))

(defn get-indicator-all 
  "Returns indicator for a specified year for all countries"
  [indicator year key]
  (get-value-map (str "/countries"
                      "/all"
                      "/indicators"
                      "/" indicator)
                 {:date year}))

(defn get-indicator 
  "Returns indicator for a country for a specified year"
  [indicator country year key]
  (get-value (str "/countries"
                  "/" country
                  "/indicators"
                  "/" indicator)
             {:date year}
             key ))

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

;(println (str "2012 price of gas in Uruguay: " (get-gas "UY" "2012")))
;(println (str "2012 price of gas in United States: " (get-gas "US" "2012")))
;(println (str "2012 GDP for Uruguay: " (get-gdp "UY" "2012")))
;(println (str "2012 GDP United States: " (get-gdp "US" "2012")))
;(println (apply str "2012 GDP all countries: " (get-gdp-all "2012")))
;(println (apply str "All indicators: " (get-indicator-list))))
;(get-indicator-list)

(defn layout 
  [title & content]
  (hiccup/html
    [:head [:title title]]
    [:body content]))

(defn view-ind
  [indicator1 indicator2]
  (layout "Indicators Chosen"
          [:h1 "Indicators Chosen"]
          [:p indicator1]
          [:p indicator2]))


(defn main-page []
  (let [indicators (get-indicator-list)]
    (layout "World Bank Indicators"
          [:h1 "World Bank Indicators"]
          [:p "Choose one of these world development indicators."]
          (f/form-to [:post "/choose-ind" ]
                     (f/label "indicator1" "Indicator 1:  ")
                     (f/drop-down "indicator1" indicators)
                     [:br]
                     (f/label "indicator2" "Indicator 2:  ")
                     (f/drop-down "indicator2" indicators)
                     [:br][:br] 
                     (f/submit-button "Submit")))))

(defroutes main-routes 
  (GET "/" [] (main-page))
  (POST "/choose-ind" [indicator1 indicator2]
        (view-ind indicator1 indicator2)))
        

(defn -main
    [& args]
  (jetty/run-jetty (wrap-params main-routes)
                   {:port 5000}))
                 
