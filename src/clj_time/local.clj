(ns clj-time.local
  (require [clj-time.core :as time]
           [clj-time.coerce :as coerce]
           [clj-time.format :as fmt])
  (:import (org.joda.time DateTime)
           (org.joda.time.format DateTimeFormatter)
           java.util.Date java.sql.Timestamp))

(def ^{:doc "Map of local formatters for parsing and printing." :dynamic true}
  *local-formatters*
  (into {} (map
            (fn [[k #^DateTimeFormatter f]] [k (.withZone f #^DateTimeZone (time/default-time-zone))])
            fmt/formatters)))

(defprotocol ILocalCoerce
  (to-local-date-time [obj] "convert `obj` to a local Joda DateTime instance."))

(defn- as-local-date-time [obj]
  (-> obj coerce/to-date-time (time/to-time-zone (time/default-time-zone))))

(defn from-local-string
  "Return local DateTime instance from string using
   formatters in clj-time.format, returning first
   which parses."
  [s]
  (first
   (for [f (vals *local-formatters*)
         :let [d (try (fmt/parse f s) (catch Exception _ nil))]
         :when d] d)))

(extend-protocol ILocalCoerce
  nil
  (to-local-date-time [_]
    nil)

  Date
  (to-local-date-time [date]
    (as-local-date-time date))

  java.sql.Date
  (to-local-date-time [sql-date]
    (as-local-date-time sql-date))

  DateTime
  (to-local-date-time [date-time]
    (as-local-date-time date-time))

  Integer
  (to-local-date-time [integer]
    (as-local-date-time (long integer)))

  Long
  (to-local-date-time [long]
    (as-local-date-time
     long))

  String
  (to-local-date-time [string]
    (from-local-string string))

  Timestamp
  (to-date-time [timestamp]
    (as-local-date-time timestamp)))

(defn local-format-time [obj format-key]
  (when-let [fmt (format-key *local-formatters*)]
    (fmt/unparse fmt (to-local-date-time obj))))