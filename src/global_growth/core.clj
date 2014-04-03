(ns global-growth.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(defn -main
  [& args]
  (println "Hello, world!"))

;;;; MODULE 4

;; (defn get-api
;;   "Returns map representing API response."
;;   [path params]
;;   (let [url (str "http://api.worldbank.org" path)
;;         query-params (merge params {:format "json" :per_page 10000})
;;         response (json/parse-string
;;                   (:body (client/get url {:query-params query-params})) true)
;;         metadata (first response)
;;         results (second response)]
;;     {:metadata metadata
;;      :results results}))

;; (defn get-country-and-value
;;   [response]
;;   (for [item (:results response)]
;;     (vector (get-in item [:country :value]) (get item :value))))

;; (defn remove-aggregate-countries
;;   "Remove all countries that aren't actually countries, but are aggregates."
;;   [countries]
;;   (remove (fn [country]
;;             (= (get-in country [:region :value]) "Aggregates")) countries))

;; ;; Get set of country ids so we can filter out aggregate values.
;; (def countries
;;   (delay
;;     (let [countries (remove-aggregate-countries (:results (get-api "/countries" {})))]
;;       (set (map :name countries)))))

;; (defn get-indicator-values
;;   "Returns indicator values for a specified year for all countries."
;;   [indicator-code year]
;;   (let [response (get-api (str "/countries/all/indicators/" indicator-code)
;;                           {:date (str year)})
;;         values (get-country-and-value response)]
;;     (for [[country value] values
;;           :when (and (not (nil? value))
;;                      (contains? @countries country))]
;;       [country (read-string value)])))

;;;; MODULE 5

;; (defn take-top-values
;;   [indicator-values num-values]
;;   (take num-values
;;         (sort-by second > indicator-values)))

;; (defn get-values
;;   "Returns a relation of two keys from API response."
;;   [path query-params key1 key2]
;;   (let [response (get-api path query-params)]
;;     (for [item (:results response)]
;;       [(key1 item) (key2 item)])))

;; (def indicators
;;   (delay (get-values "/topics/16/indicators" {} :name :id)))

;; (defn get-indicators []
;;   "Gets vector of indicators.
;;   /topics/16/indicators:   All urban development
;;   --- Other possibilities ---
;;   /sources/2/indicators:   All world development indicators (about 1300)
;;   /indicators:             All Indicators (about 8800)"
;;   @indicators)

;; (defn -main
;;   [& args]
;;   (let [indicator-values (get-indicator-values "EN.POP.DNST" 2010)
;;         top-10-values (take-top-values indicator-values 10)]
;;     (doseq [value top-10-values]
;;       (println (str (first value) " " (second value))))))
