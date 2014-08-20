(ns reagent-tops.core-test
  (:require-macros
    [cemerick.cljs.test
     :refer [is deftest with-test run-tests testing test-var]]
    [clojure.test.check.clojure-test :refer [defspec]]
    [dommy.macros :refer [sel sel1 node]]
    [reagent.ratom :refer [reaction]]
    [reagent.debug :refer [dbg println log]])
  (:require
    [cemerick.cljs.test :as t]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop :include-macros true]
    [clojure.string :as str]
    [dommy.utils :as utils]
    [dommy.core :as dommy]
    [reagent.core :as reagent :refer [atom]]
    [reagent.ratom :as rv]
    [reagent-tops.core :refer [tops-component word-view input-word submit-word word-list words-state]]))

(def runs 100)

(defn running [] (rv/running))

(def isClient (not (nil? (try (.-document js/window)
                              (catch js/Object e nil)))))

(def rflush reagent/flush)

(defn add-test-div [name]
  (let [doc js/document
        body (.-body js/document)
        div (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when isClient
    (let [div (add-test-div "_testreagent")]
      (let [comp (reagent/render-component comp div #(f comp div))]
        (reagent/unmount-component-at-node div)
        (reagent/flush)))))

(defspec server-word-view runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (when isClient
      (with-mounted-component [word-view (assoc w :origin :server)]
        (fn [c div]
          (let [el (.-firstChild div)]
            (is (= 1 (.-childElementCount div)))
            (is (= (:word w) (dommy/text el)))
            (is (dommy/has-class? el "list-group-item"))
            (is (not (dommy/has-class? el "list-group-item-warning")))
            (is (not (dommy/has-class? el "list-group-item-success")))
            (is (not (dommy/has-class? el "list-group-item-danger")))
            (is (not (dommy/has-class? el "invalid"))))))
      true)))

(defspec local-word-view runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (when isClient
      (with-mounted-component [word-view (assoc w :origin :local)]
        (fn [c div]
          (let [el (.-firstChild div)]
            (is (= 1 (.-childElementCount div)))
            (is (= (:word w) (dommy/text el)))
            (is (dommy/has-class? el "list-group-item"))
            (is (dommy/has-class? el "list-group-item-warning"))
            (is (not (dommy/has-class? el "list-group-item-success")))
            (is (not (dommy/has-class? el "list-group-item-danger")))
            (is (not (dommy/has-class? el "invalid"))))))
      true)))

(defspec local-word-view-valid runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (when isClient
      (with-mounted-component [word-view (assoc w
                                           :origin :local
                                           :valid true)]
        (fn [c div]
          (let [el (.-firstChild div)]
            (is (= 1 (.-childElementCount div)))
            (is (= (:word w) (dommy/text el)))
            (is (dommy/has-class? el "list-group-item"))
            (is (not (dommy/has-class? el "list-group-item-warning")))
            (is (dommy/has-class? el "list-group-item-success"))
            (is (not (dommy/has-class? el "list-group-item-danger")))
            (is (not (dommy/has-class? el "invalid"))))))
      true)))

(defspec local-word-view-invalid runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (when isClient
      (with-mounted-component [word-view (assoc w
                                           :origin :local
                                           :invalid true)]
        (fn [c div]
          (let [el (.-firstChild div)]
            (is (= 1 (.-childElementCount div)))
            (is (= (:word w) (dommy/text el)))
            (is (dommy/has-class? el "list-group-item"))
            (is (not (dommy/has-class? el "list-group-item-warning")))
            (is (not (dommy/has-class? el "list-group-item-success")))
            (is (dommy/has-class? el "list-group-item-danger"))
            (is (dommy/has-class? el "invalid")))))
      true)))

(defspec word-list-test runs
  (prop/for-all [w (gen/vector (gen/hash-map :word gen/string-ascii) 0 10)]
    (when isClient
      (with-mounted-component [word-list (map #(assoc % :origin :server) w)]
        (fn [c div]
          (let [el (.-firstChild div)]
            (is (= 1 (.-childElementCount div)))
            (is (= (count w) (.-childElementCount el)))
            (is (dommy/has-class? el "list-group"))
            (is (= (str/join (map :word (reverse w))) (dommy/text el))))))
      true)))

(deftest tops-component-test
  (when isClient
    (let [_ (reset! words-state [{:word "blabla" :origin :server}
                                 {:word "bleble" :origin :server}])]
      (with-mounted-component [tops-component]
        (fn [c div]
          (let [elp (.-firstChild div)
                elc (.-firstChild elp)
                h (aget (.-childNodes elc) 0)
                wl (aget (.-childNodes elc) 2)]
            (is (= 1 (.-childElementCount div)))
            (is (= 1 (.-childElementCount elp)))
            (is (= 3 (.-childElementCount elc)))
            (is (= 3 (-> elc .-childNodes .-length)))
            (is (dommy/has-class? elp "row"))
            (is (dommy/has-class? elc "col-lg-4"))
            (is (dommy/has-class? elc "col-md-5"))
            (is (dommy/has-class? elc "col-sm-6"))
            (is (= "Reagent Tops" (dommy/text h)))
            (is (= 2 (.-childElementCount wl)))
            (is (dommy/has-class? wl "list-group"))
            ))))))
