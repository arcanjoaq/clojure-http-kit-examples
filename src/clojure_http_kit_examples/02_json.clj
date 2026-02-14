(ns clojure-http-kit-examples.02-json
  (:require
   [camel-snake-kebab.core :as camel-snake-kebab]
   [cheshire.core :as cheshire]
   [org.httpkit.client :as http]
   [schema.core :as s]))

(s/set-fn-validation! true)

(s/defschema User
  "A user"
  {:id s/Num
   :name s/Str
   :email s/Str
   :username s/Str
   :phone s/Str
   :website s/Str
   :company {s/Keyword s/Any}
   :address {s/Keyword s/Any}})

(s/defschema Post
  "A post"
  {(s/optional-key :id) s/Int
   :user-id s/Int
   :title s/Str
   :description s/Str})

(s/defn get-user :- User
  [user-id :- s/Num]
  (s/validate User (-> @(http/request {:url (str "https://jsonplaceholder.typicode.com/users/" user-id)
                                       :method :get
                                       :timeout 1000
                                       :connect-timeout 2000
                                       :idle-timeout 60000
                                       :as :text})
                       :body
                       (cheshire/decode camel-snake-kebab/->kebab-case-keyword))))

(s/defn save-post :- Post
  [post :- Post]
  (s/validate Post
              (-> @(http/request
                    {:url "https://jsonplaceholder.typicode.com/posts"
                     :method :post
                     :headers {"Content-Type" "application/json"
                               "Accept" "application/json"}
                     :timeout 1000
                     :connect-timeout 2000
                     :idle-timeout 60000
                     :as :text
                     :body (cheshire/generate-string post {:key-fn camel-snake-kebab/->camelCaseString})})
                  :body
                  (cheshire/decode camel-snake-kebab/->kebab-case-keyword))))

(s/defn update-post :- Post
  [post :- Post]
  (s/validate Post
              (-> @(http/request {:url (str "https://jsonplaceholder.typicode.com/posts/" (:id post))
                                  :method :put
                                  :headers {"Content-Type" "application/json"
                                            "Accept" "application/json"}
                                  :timeout 1000
                                  :connect-timeout 2000
                                  :idle-timeout 60000
                                  :as :text
                                  :body (cheshire/generate-string post {:key-fn camel-snake-kebab/->camelCaseString})})
                  :body
                  (cheshire/decode camel-snake-kebab/->kebab-case-keyword))))

(s/defn delete-user
  [user-id :- s/Int]
  @(http/request {:url (str "https://jsonplaceholder.typicode.com/users/" user-id)
                  :method :delete
                  :timeout 1000
                  :connect-timeout 2000
                  :idle-timeout 60000
                  :as :text}))

(comment
  (get-user 1)

  (save-post {:user-id 1
              :title "My first post"
              :description "This is my first post"})

  (update-post {:id 1
                :user-id 1
                :title "My first post"
                :description "This is my first post"})

  (delete-user 1))
