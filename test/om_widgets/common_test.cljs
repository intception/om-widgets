(ns om-widgets.common-test
  (:require [dommy.core :as dommy :refer-macros [sel sel1]]))


(defn create-el [tag & [text]]
  (let [el (dommy/create-element tag)]
    (when text
      (dommy/set-text! el text))
    el))

(defn id->keyword [id]
  (keyword (str "#" id)))

(defn class->keyword [class]
  (keyword (str "." class)))

(defn new-container! []
  (let [id (str "container-" (gensym))
        container (-> (create-el :div)
                      (dommy/set-attr! :id id))]
    (dommy/append! (sel1 js/document :body) container)
    (sel1 (id->keyword id))))

