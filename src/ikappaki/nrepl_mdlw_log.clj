(ns ikappaki.nrepl-mdlw-log
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [nrepl.middleware :as middleware :refer [set-descriptor!]]
   [nrepl.middleware.session :refer [session]]))

(defn wrap-log
  [h]
  (let [filename "nrepl-mdlw-log.log"
        _ (io/delete-file filename true)
        out (io/file filename)]
    (println :created out)
    (fn [{:keys [op id] :as msg}]
      (let [before (java.time.Instant/now)]
        (try
          (println :printing... out id (str before))
          (spit out (format ":----- :id %s :when %s\n:dbg/in\n%S:_____\n"
                            id before (with-out-str (pp/pprint msg)))
                :append true)
          (catch Exception e
            (println :wrap-log-error (str e))))
        (let [_ret (h msg)
              after (java.time.Instant/now)]
          (spit out (format "\n:===== :id %s :when %s :op %s :diff-ms %s\n\n"
                            id after op (.toMillis (java.time.Duration/between before after)))
                :append true))))))

(def middleware '[ikappaki.nrepl-mdlw-log/wrap-log])

(set-descriptor! #'wrap-log
                 {:requires #{}
                  :expects #{#'session}})
