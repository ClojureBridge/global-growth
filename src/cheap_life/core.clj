(ns cheap-life.core
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:gen-class))

;; CONSTANTS
(def BASE_URI "http://api.worldbank.org")

(defn- parse-json [str]
  (json/parse-string str true))

(defn get-api [query-str]
  "Returns json object representing API response"
  (parse-json (:body (client/get (str BASE_URI 
                          query-str 
			  "&format=json" ))))) 

(defn get-gas [country year]
  "Returns the pump price for gasoline (US$ per liter) 
	in a country for a specified year"
  (get-in (last
            (get-api (str "/countries"
                "/" country
		        "/indicators"
		        "/EP.PMP.SGAS.CD"
		        "?date=" year)))
            [0 :value] ))
                


(defn -main
    "Prints the price of gasoline in Uruguay."
    [& args]
    (println (str "Price of gas in Uruguay: " (get-gas "UY" "2012"))))
                 
                  
