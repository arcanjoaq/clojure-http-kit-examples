(ns clojure-http-kit-examples.02-json-test
  (:require
   [clojure-http-kit-examples.02-json :as json]
   [state-flow.api :as flow :refer [defflow flow match?]]))

(defflow json-test
  (flow "When I execute get user"
        [:let [res (json/get-user 1)]]

        (flow "Then the response will be 200"
              (match? {} res)))

  (flow "When I execute save post"
        [:let [res (json/save-post {:user-id 1
                                    :title "My first post"
                                    :description "This is my first post"})]]

        (flow "Then the response will be 200"
              (match? {:id number?
                       :user-id 1
                       :title "My first post"
                       :description "This is my first post"} res)))

  (flow "When I execute update post"
        [:let [post {:id 1
                     :user-id 1
                     :title "My first post"
                     :description "This is my first post"}
               res (json/update-post post)]]

        (flow "Then the response will be 200"
              (match? post res)))

  (flow "When I execute delete user"
        [:let [res (json/delete-user 1)]]

        (flow "Then the response will be 200"
              (match? {} res))))
