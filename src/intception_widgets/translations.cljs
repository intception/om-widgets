(ns intception-widgets.translations
  (:use [net.unit8.tower :only [t]]))

(def widgets-translations
  {:dev-mode? false
   :fallback-locale :en
   :dictionary
   {:es {:button {}
         :datepicker {}
         :grid {:pager
                       {:previous-page "« Previa"
                        :next-page "« Siguiente"
                        :total "%d en total"}}
         :modal-box {}
         :radio-button {}
         :missing! "<Traducción no encontrada>"}
    :en {:button {}
         :datepicker {}
         :grid {:default-pager
                       {:previous-page "« Previous"
                        :next-page "« Next"
                        :total-rows "Total rows: %d"}}
         :modal-box {}
         :radio-button {}
         :missing! "<Translation not found>"}}})

(defn translate
  [ns lang & args]
  (apply t lang widgets-translations
     (keyword (name ns)
              (clojure.string/join "/" (map name (take-while keyword? args))))
     (drop-while keyword? args)))
