(ns cheap-life.core
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:gen-class))

;; CONSTANTS
(def BASE_URI "http://api.worldbank.org")
(def GAS_INDICATOR "EP.PMP.SGAS.CD")
(def GDP_INDICATOR "NY.GDP.PCAP.CD")

(defn- parse-json [str]
  (json/parse-string str true))

(defn get-api [query-str]
  "Returns json object representing API response"
  (parse-json (:body 
    (client/get 
      (str BASE_URI
          query-str 
          "&format=json" )))))

(defn get-value [query-str key]
  "Returns single value from API response"
  (get-in (last (get-api query-str)) [0 key]))

(defn get-value-map [query-str]
  "Returns map of values from API response"
  ;TODO: handle paging
  (last (get-api query-str)))
  

(defn get-indicator-all [indicator year key]
  "Returns indicator for a specified year for all countries"
  (get-value-map (str "/countries"
                   "/all"
                   "/indicators"
                   "/" indicator
                   "?date=" year)
                ))

(defn get-indicator [indicator country year key]
  "Returns indicator for a country for a specified year"
  (get-value (str "/countries"
                "/" country
	        "/indicators"
	        "/" indicator
	        "?date=" year)
           key ))

(defn get-gas [country year]
  "Returns the pump price for gasoline (US$ per liter) 
	in a country for a specified year"
  (get-indicator GAS_INDICATOR country year :value))

(defn get-gdp [country year]
  "Returns the GDP per capita (current US$)
        in a country for a specified year"
  (get-indicator GDP_INDICATOR country year :value))
          
(defn get-gdp-all [year]
  "Returns the GDP per capita (current US$)
        for a specified year for all countries"
  (get-indicator-all GDP_INDICATOR year :value))


(defn -main
    "Prints the price of gasoline in Uruguay."
    [& args]
    (println (str "2012 price of gas in Uruguay: " (get-gas "UY" "2012")))
    (println (str "2012 price of gas in United States: " (get-gas "US" "2012")))
    (println (str "2012 GDP for Uruguay: " (get-gdp "UY" "2012")))
    (println (str "2012 GDP United States: " (get-gdp "US" "2012")))
    (println (apply str "2012 GDP all countries: " (get-gdp-all "2012"))))
                 
                  
