(defproject hashids "0.1.0"
  :description "Generate short unique ids from integers"
  :url "https://github.com/jstrutz/hashids.clj"
  :scm {:name "git"
        :url "https://github.com/jstrutz/hashids.clj"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :signing {:gpg-key "j@jasonstrutz.com"}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :pom-addition [:developers [:developer
                              [:name "Jason Strutz"]
                              [:url "http://jasonstrutz.com"]
                              [:email "j@jasonstrutz.com"]
                              [:timezone "-8"]]]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.7.0"]]}})
