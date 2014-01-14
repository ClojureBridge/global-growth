(ns cheap-life.core
  (:use compojure.core)
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]))
            

;; CONSTANTS
(def BASE_URI "http://api.worldbank.org")
(def GAS_INDICATOR "EP.PMP.SGAS.CD")
(def GDP_INDICATOR "NY.GDP.PCAP.CD")

(defn parse-json [str]
  (json/parse-string str true))

(defn get-api [path qp]
  "Returns json object representing API response"
  (let 
    [base_path (str BASE_URI path)
     query-params (merge qp {:format "json" :per_page 10000})]
    (parse-json (:body 
                  (client/get base_path {:query-params query-params})))))

(defn get-value [path query-params key]
  "Returns single value from API response"
  (get-in (last (get-api path query-params)) [0 key]))

(defn get-value-list [path query-params key1 key2]
  "Returns list of values for a key from API response"
  ;TODO: handle paging
  (let 
    [result (get-api path query-params)]
    (zipmap 
      (map key1 (last result))
      (map key2 (last result)))))

(defn get-value-map [path query-params]
  "Returns map of values from API response"
  ;TODO: handle paging
  (last (get-api path query-params)))

(defn get-indicator-list []
  (get-value-list "/indicators" {} :id :name))

(defn get-indicator-all [indicator year key]
  "Returns indicator for a specified year for all countries"
  (get-value-map (str "/countries"
                      "/all"
                      "/indicators"
                      "/" indicator)
                 {:date year}))

(defn get-indicator [indicator country year key]
  "Returns indicator for a country for a specified year"
  (get-value (str "/countries"
                  "/" country
                  "/indicators"
                  "/" indicator)
             {:date year}
             key ))

(defn get-gas [country year]
  "Returns the pump price for gasoline (US$ per liter) 
	in a country for a specified year"
  (get-indicator GAS_INDICATOR country year :value))

(defn get-gdp 
  "Returns the GDP per capita (current US$)
        in a country for a specified year"
  [country year]
  (get-indicator GDP_INDICATOR country year :value))
          
(defn get-gdp-all 
  "Returns the GDP per capita (current US$)
        for a specified year for all countries"
  [year]
  (get-indicator-all GDP_INDICATOR year :value))

;(println (str "2012 price of gas in Uruguay: " (get-gas "UY" "2012")))
;(println (str "2012 price of gas in United States: " (get-gas "US" "2012")))
;(println (str "2012 GDP for Uruguay: " (get-gdp "UY" "2012")))
;(println (str "2012 GDP United States: " (get-gdp "US" "2012")))
;(println (apply str "2012 GDP all countries: " (get-gdp-all "2012")))
;(println (apply str "All indicators: " (get-indicator-list))))

(def main-routes
  (GET "/" [] "This is the cheap-life web app."))

(defn -main
    [& args]
  (jetty/run-jetty main-routes
                   {:port 5000}))
                 
