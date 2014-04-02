(ns global-growth.core
  (:require [global-growth.world-bank :as world-bank]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [hiccup.form :as form]))

; TODO: handle paging: right now just getting a large value
; for 'per_page' parameter.
; Maybe that is fine for simplicity. But that could be a
; problem if we get things with large result sets. Not
; getting large results as of now.

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

(defn view-indicators
  [indicator1 indicator2 year]
  (let [inds1 (world-bank/get-indicator-values indicator1 year)
        inds2 (world-bank/get-indicator-values indicator2 year)
        indicator-map (into {}
                            (map (fn [indicator]
                                   [(second indicator) (first indicator)])
                                 (world-bank/get-indicators)))]
  (layout "Sorted Indicators"
          [:h1 "Sorted Indicators"]
          [:div.row
           [:div.form-group.col-md-6
            (form/label indicator1 (get indicator-map indicator1))
            (if (empty? inds1)
              [:p "No indicator values for this year."]
              (indicator-list inds1))
            ]
           [:div.form-group.col-md-6
            (form/label indicator2 (get indicator-map indicator2))
            (if (empty? inds2)
              [:p "No indicator values for this year."]
              (indicator-list inds2))]])))

(defn main-page []
  (layout "World Bank Indicators"
          [:h1 "World Bank Indicators"]
          [:p "Choose one of these world development indicators."]
          (form/form-to {:role "form"} [:get "/indicators"]
                     [:div.row
                      [:div.form-group.col-md-5
                       (form/label "indicator1" "Indicator 1:  ")
                       (form/drop-down {:class "form-control"}
                                       "indicator1"
                                       (world-bank/get-indicators))]
                      [:div.form-group.col-md-5
                       (form/label "indicator2" "Indicator 2:  ")
                       (form/drop-down {:class "form-control"}
                                       "indicator2"
                                       (world-bank/get-indicators))]
                      [:div.form-group.col-md-2
                       (form/label "year" "Year: ")
                       (form/drop-down {:class "form-control"}
                                       "year"
                                       (reverse (range 1960 2013))
                                       2010)]]
                     (form/submit-button "Submit"))))

(defroutes main-routes
  (GET "/" [] (main-page))
  (GET "/indicators" [indicator1 indicator2 year]
        (view-indicators indicator1 indicator2 year)))

(def handler (site main-routes))

