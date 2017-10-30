(set-env! :resource-paths #{"src/cljs"}
          :source-paths   #{"test/cljs"}
          :dependencies   '[[cljs-ajax                 "0.7.2"]
                            [org.clojure/clojurescript "1.9.946" :scope "provided"]
                            [re-frame                  "0.10.2"  :scope "provided"]

                            [adzerk/bootlaces      "0.1.13"    :scope "test"]
                            [adzerk/boot-test      "1.2.0"     :scope "test"]])

(def project 'oconn/re-frame-request)
(def +version+ "0.1.0-SNAPSHOT")

(task-options!
 pom {:project     project
      :version     +version+
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/oconn/re-frame-request"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(require '[adzerk.boot-test      :refer [test]]
         '[adzerk.bootlaces      :refer :all])

(bootlaces! +version+ :dont-modify-paths? true)

(deftask build
  "Build and install the project locally."
  []
  (comp
   (pom)
   (jar)
   (install)))
