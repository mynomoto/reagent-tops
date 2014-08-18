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
(def invalid (atom {}))

(js/setInterval
  (fn []
    (swap! invalid select-keys (map first @words))
    (edn-xhr
      {:method :get
       :url "word"
       :on-complete #(swap! words (fn [x] (conj (vec (take-last 9 x)) [% :server])))}))
        1000)

(defn submit-word [word]
  (edn-xhr
    {:method :put
     :url "word"
     :data {:word word}
     :on-complete
     (fn [res]
       (if (= :ok res)
         (println "server response:" res)
         (swap! invalid merge res)))}))

(defn simple-component []
  [:div
   [:h1 "Reagent Tops"]
   [:input {:value @input
            :type "text"
            :on-change #(reset! input (-> % .-target .-value))}]
   [:button {:on-click #(do
                          (submit-word @input)
                          (swap! words (fn [x] (conj (vec (take-last 9 x)) [@input :local])))
                          (reset! input ""))}
    "Submit"]
   [:div
    (doall
      (for [[w o] (reverse @words)]
        [:p {:class (str (when (= o :local) "local")
                      (when (@invalid w) " invalid"))}
         w]))]])

(reagent/render-component [simple-component] (.-body js/document))
