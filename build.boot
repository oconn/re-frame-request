(set-env! :resource-paths #{"src"}

          :dependencies   '[[cljs-ajax                 "0.8.0"]
                            [org.clojure/clojurescript "1.10.520" :scope "provided"]
                            [re-frame                  "0.10.6"  :scope "provided"]

                            [adzerk/bootlaces  "0.1.13" :scope "test"]])

(require '[adzerk.bootlaces :refer :all]
         '[boot.git         :refer [last-commit]])

(def project 'oconn/re-frame-request)
(def +version+ "0.1.3")

(task-options!
 pom {:project     project
      :version     +version+
      :description "ClojureScript (re-frame) library to improve XHR state management."
      :url         "https://github.com/oconn/re-frame-request"
      :scm         {:url "https://github.com/oconn/re-frame-request"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 push {:repo "deploy-clojars"
       :ensure-clean true
       :ensure-tag (last-commit)
       :ensure-version +version+})

(bootlaces! +version+ :dont-modify-paths? true)

(deftask install-local
  "Build and install the project locally."
  []
  (comp (pom)
        (jar)
        (install)))

(deftask dev
  "Watches for changes and then installs locally"
  []
  (comp (watch)
        (install-local)))

(deftask deploy-snapshot
  "Deploys a new build to clojars"
  []
  (comp (build-jar)
        (push-snapshot)))

(deftask deploy-release
  "Deploys a release build to clojars"
  []
  (comp (build-jar)
        (push-release)))
