(ns clojoy.ch17.dsl
  (:require [clojure.set :as ra]))

(def artists
  #{{:artist "Burial" :genre-id 1}
    {:artist "Magma" :genre-id 2}
    {:artist "Can" :genre-id 3}
    {:artist "Faust" :genre-id 3}
    {:artist "Iknoika" :genre-id 3}
    {:artist "Grouper"}})

(def genres
  #{{:genre-id 1 :genre-name "Dubstep"}
    {:genre-id 2 :genre-name "Zeuhl"}
    {:genre-id 3 :genre-name "Prog"}
    {:genre-id 4 :genre-name "Drone"}})

(def ALL identity)

(ra/select ALL genres)
;#{{:genre-id 4, :genre-name "Drone"}
;  {:genre-id 2, :genre-name "Zeuhl"}
;  {:genre-id 3, :genre-name "Prog"}
;  {:genre-id 1, :genre-name "Dubstep"}}

