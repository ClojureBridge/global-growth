(ns leiningen.new.clojurebridge
  (:require [leiningen.new.templates :refer [renderer year project-name
                                             ->files sanitize-ns name-to-path
                                             multi-segment]]
            [leiningen.core.main :as main]))

(defn clojurebridge
  "A shell project for the ClojureBridge curriculum."
  [name]
  (let [render (renderer "clojurebridge")
        main-ns (sanitize-ns name)
        data {:raw-name name
              :name (project-name name)              
              :namespace main-ns
              :nested-dirs (name-to-path main-ns)
              :year (year)}]
    (main/info "Generating a project called" name "based on the 'clojurebridge' template.")
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["README.md" (render "README.md" data)]
             [".gitignore" (render "gitignore" data)]
             ["src/{{nested-dirs}}/core.clj" (render "core.clj" data)]
             ["src/{{nested-dirs}}/web.clj" (render "web.clj" data)]             
             ["test/{{nested-dirs}}/core_test.clj" (render "test.clj" data)]
             "resources")))
