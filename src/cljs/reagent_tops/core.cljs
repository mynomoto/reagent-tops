(ns reagent-tops.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [clojure.string :as str]
    [cljs.reader :as reader]
    [goog.events :as events])
  (:import
    [goog.net XhrIo]
    goog.net.EventType
    [goog.events EventType]))

(enable-console-print!)

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})

(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
      (fn [e]
        (on-complete (reader/read-string (.getResponseText xhr)))))
    (. xhr
      (send url (meths method) (when data (pr-str data))
        #js {"Content-Type" "application/edn"}))))

(def state (atom []))

(defn update-state []
  (edn-xhr
    {:method :get
     :url "word"
     :on-complete #(when-not (str/blank? %)
                     (swap! state
                       (fn [x]
                         (conj (vec (take-last 9 x))
                           {:word % :origin :server}))))}))

(defn init [interval]
  (js/setInterval update-state interval))

(defn submit-word [word]
  (edn-xhr
    {:method :put
     :url "word"
     :data {:word word}
     :on-complete
     (fn [res]
       (swap! state #(mapv (fn [x]
                             (cond
                               (= (:invalid res) (:word x))
                               (assoc x :invalid true)

                               (= (:valid res) (:word x))
                               (assoc x :valid true)

                               :else x))
                       %)))}))

(defn word-item [{:keys [word origin invalid valid]}]
  [:li.list-group-item
   {:class (str
             (when (and (= origin :local) (not invalid) (not valid))
               "list-group-item-warning")
             (when (and (= origin :local) valid)
               " list-group-item-success")
             (when invalid " invalid list-group-item-danger"))}
   word])

(defn word-list [words]
  [:ul.list-group
   (for [word (reverse words)]
     [word-item word])])

(defn word-input []
  (let [input (atom "")]
    (fn []
      [:div.input-group
       [:input.form-control
        {:value @input
         :type "text"
         :on-change #(reset! input (-> % .-target .-value))}]
       [:span.input-group-btn
        [:button.btn.btn-primary
         {:on-click #(do
                       (when-not (str/blank? @input)
                         (submit-word @input)
                         (swap! state
                           (fn [x]
                             (conj (vec (take-last 9 x))
                               {:word @input
                                :origin :local})))
                         (reset! input "")))}
         "Submit"]]])))

(defn tops-component []
  [:div.row
   [:div.col-lg-4.col-md-5.col-sm-6
    [:h1 "Reagent Tops"]
    [word-input]
    [word-list @state]]])

(when (js/document.getElementById "tops")
  (reagent/render-component [tops-component] (js/document.getElementById "tops"))
  (update-state)
  (init 1000))
