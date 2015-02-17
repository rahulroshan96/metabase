(ns metabase.models.card
  (:require [clojure.data.json :as json]
            [korma.core :refer :all]
            [metabase.api.common :refer [*current-user-id* org-perms-case]]
            [metabase.db :refer :all]
            (metabase.models [common :refer :all]
                             [hydrate :refer [realize-json]]
                             [org :refer [Org]]
                             [user :refer [User]])
            [metabase.util :refer [new-sql-date]]))

(defentity Card
  (table :report_card))

(defmethod pre-insert Card [_ {:keys [dataset_query visualization_settings] :as card}]
  (let [defaults {:created_at (new-sql-date)
                  :updated_at (new-sql-date)}]
    (-> (merge defaults card)
        (assoc :dataset_query (json/write-str dataset_query)
               :visualization_settings (json/write-str visualization_settings)))))

(defmethod post-select Card [_ {:keys [organization_id creator_id] :as card}]
  (-> card
      (realize-json :dataset_query :visualization_settings)
      (assoc :creator (sel-fn :one User :id creator_id)
             :organization (sel-fn :one Org :id organization_id))
      assoc-permissions-sets))
