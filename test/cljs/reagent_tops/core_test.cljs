(ns reagent-tops.core-test
  (:require-macros
    [cemerick.cljs.test
     :refer [is deftest with-test run-tests testing test-var]]
    [clojure.test.check.clojure-test :refer [defspec]]
    [dommy.macros :refer [sel sel1 node]])
  (:require
    [cemerick.cljs.test]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop :include-macros true]
    [clojure.string :as str]
    [dommy.utils :as utils]
    [dommy.core :as dommy]
    [reagent.core :as reagent]
    [reagent-tops.core :as t]))

(def runs 100)

(defn new-container! []
  (let [id (str "container-" (gensym))
        div (node [:div {:id id}])]
    (dommy/append! (.-body js/document) div)
    div))

(defn with-mounted-component [comp f]
  (let [div (new-container!)]
    (let [comp (reagent/render-component comp div #(f comp div))]
      (reagent/unmount-component-at-node div)
      (reagent/flush)
      true)))

(defspec server-word-item runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (with-mounted-component [t/word-item (assoc w :origin :server)]
      (fn [c div]
        (let [el (.-firstChild div)]
          (is (= 1 (.-childElementCount div)))
          (is (= (:word w) (dommy/text el)))
          (is (dommy/has-class? el "list-group-item"))
          (is (not (dommy/has-class? el "list-group-item-warning")))
          (is (not (dommy/has-class? el "list-group-item-success")))
          (is (not (dommy/has-class? el "list-group-item-danger")))
          (is (not (dommy/has-class? el "invalid"))))))))

(defspec local-word-item runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (with-mounted-component [t/word-item (assoc w :origin :local)]
      (fn [c div]
        (let [el (.-firstChild div)]
          (is (= 1 (.-childElementCount div)))
          (is (= (:word w) (dommy/text el)))
          (is (dommy/has-class? el "list-group-item"))
          (is (dommy/has-class? el "list-group-item-warning"))
          (is (not (dommy/has-class? el "list-group-item-success")))
          (is (not (dommy/has-class? el "list-group-item-danger")))
          (is (not (dommy/has-class? el "invalid"))))))))

(defspec local-word-item-valid runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (with-mounted-component [t/word-item (assoc w
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
          (is (not (dommy/has-class? el "invalid"))))))))

(defspec local-word-item-invalid runs
  (prop/for-all [w (gen/hash-map :word gen/string-ascii)]
    (with-mounted-component [t/word-item (assoc w
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
          (is (dommy/has-class? el "invalid")))))))

(defspec word-list-test runs
  (prop/for-all [w (gen/vector (gen/hash-map :word gen/string-ascii) 0 10)]
    (with-mounted-component [t/word-list (map #(assoc % :origin :server) w)]
      (fn [c div]
        (let [el (.-firstChild div)]
          (is (= 1 (.-childElementCount div)))
          (is (= (count w) (.-childElementCount el)))
          (is (dommy/has-class? el "list-group"))
          (is (= (str/join (map :word (reverse w))) (dommy/text el))))))))

(deftest tops-component-test
  (let [_ (reset! t/state [{:word "blabla" :origin :server}
                           {:word "bleble" :origin :server}])]
    (with-mounted-component [t/tops-component]
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
          (is (= (str/join (map :word (reverse @t/state))) (dommy/text wl))))))))
