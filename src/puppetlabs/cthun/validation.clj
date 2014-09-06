(ns puppetlabs.cthun.validation
  (:require [clojure.tools.logging :as log]
            [puppetlabs.kitchensink.core :as ks]
            [cheshire.core :as cheshire]
            [schema.core :as s]))

(def ISO8601
  "Schema validates if string conforms to ISO8601"
  (s/pred ks/datetime? 'datetime?))

(def Endpoint
  "Pattern that matches valid endpoints"
  (s/pred (partial re-matches #"cth://.*") 'endpoint?))

; Message types
(def ClientMessage
  "Defines the message format expected from a client"
  {(s/required-key :version) s/Str
   (s/required-key :id) s/Int
   (s/required-key :endpoints) [Endpoint]
   (s/required-key :data_schema) s/Str
   (s/required-key :sender) Endpoint
   (s/required-key :expires) ISO8601
   (s/required-key :hops) [{s/Keyword ISO8601}] ;; TODO(richardc): should be in terms of Endpoint
   (s/optional-key :data) {s/Keyword s/Any}})

; Server message data types
(def LoginMessageData
  "Defines the data field in a login message body"
  {(s/required-key :type) s/Str
   (s/required-key :user) s/Str})

(defn check-schema
  "Check if the JSON matches the ClientMessage schema.
  Returns message on success.
  Throws on failure."
  [json]
  (s/validate ClientMessage json))

(defn validate-message
  "Validates the structure of a message.
  Returns message on success.
  Returns false on invalid json.
  Throws on valid json with an invalid schema"
  [message]
  (let [json (try (cheshire/parse-string message true)
                  (catch Exception e false))]
    (when json
      (check-schema json))))


(defn validate-login-data
  "Validate the structure of a login message data field"
  [data]
  (s/validate LoginMessageData data))