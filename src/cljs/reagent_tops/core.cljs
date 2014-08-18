(ns reagent-tops.core
  (:require
    [reagent.core :as reagent :refer [atom]]
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

(def words (atom []))
(def input (atom ""))

(defn init [interval]
  (js/setInterval
    (fn []
      (edn-xhr
        {:method :get
         :url "word"
         :on-complete #(swap! words
                         (fn [x]
                           (conj (vec (take-last 9 x))
                             {:word % :origin :server})))}))
    interval))

(defn submit-word [word]
  (edn-xhr
    {:method :put
     :url "word"
     :data {:word word}
     :on-complete
     (fn [res]
       (when-not (= :ok res)
         (swap! words #(mapv (fn [x]
                               (if (= res (:word x))
                                 (assoc x :invalid true)
                                 x))
                         %))))}))

(defn input-word []
  [:div
   [:input
    {:value @input
     :type "text"
     :on-change #(reset! input (-> % .-target .-value))}]
   [:button
    {:on-click #(do
                  (submit-word @input)
                  (swap! words
                    (fn [x]
                      (conj (vec (take-last 9 x))
                        {:word @input
                         :origin :local})))
                  (reset! input ""))}
    "Submit"]])

(defn word-view [{:keys [word origin invalid]}]
  [:p {:class (str (when (= origin :local) "local")
                (when invalid " invalid"))}
   word])

(defn tops-component []
  [:div
   [:h1 "Reagent Tops"]
   [input-word]
   [:div
    (for [word (reverse @words)]
      [word-view word])]])

(reagent/render-component [tops-component] (.-body js/document))

(init 1000)
