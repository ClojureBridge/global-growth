(ns global-growth.core
  (:require [clojure.pprint :as pp]
            [clj-http.client :as client]
            [cheshire.core :as json]))

;; CONSTANTS
(def BASE-URI "http://api.worldbank.org")

(defn parse-json [str]
  (json/parse-string str true))

;; WORLD BANK API CALLS
(defn get-api
  "Returns json object representing API response."
  [path qp]
  (let [base-path (str BASE-URI path)
        query-params (merge qp {:format "json" :per_page 10000})
        response (parse-json (:body (client/get base-path {:query-params query-params})))
        metadata (first response)
        results (second response)]
    {:metadata metadata
     :results results}))


(defn -main []
  (pp/pprint (get-api "/countries" {})))

