(defproject jstrutz/hashids "1.0.0"
  :description "Generate short unique ids from integers"
  :url "https://github.com/jstrutz/hashids.clj"
  :scm {:name "git"
        :url "https://github.com/jstrutz/hashids.clj"}
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :signing {:gpg-key "j@jasonstrutz.com"}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :pom-addition [:developers [:developer
                              [:name "Jason Strutz"]
                              [:url "http://jasonstrutz.com"]
                              [:email "j@jasonstrutz.com"]
                              [:timezone "-8"]]]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.7.0"]]}})
