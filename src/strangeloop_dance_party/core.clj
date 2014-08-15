(ns strangeloop-dance-party.core
  (:require [ellipso.core :as core]
            [ellipso.commands :as commands]
            [overtone.osc :refer :all]))

(comment
  ;; all the initialization  by hand goes here....
  ;; crosss your fingers and hope everyone connects okay


  ;; ls /dev/tty.Sphero*
 ;; init the sphero
  (def sphero (core/connect "/dev/tty.Sphero-RBR-AMP-SPP-1"))

  (comment  (core/disconnect sphero))

  (commands/execute sphero (commands/colour 0xFF0000)) ;;red
  (commands/execute sphero (commands/colour 0xFF8000))
  (commands/execute sphero (commands/roll 0 0))
  (commands/execute sphero (commands/roll 0x4B 0))
  (commands/execute sphero (commands/heading 60))
  
 
  
  )


(def PORT 4242)

; start a server and create a client to talk with it
(def server (osc-server PORT))

(def RED 0xFF0000)
(def YELLOW 0xFF8000)
(def BLUE 0x0000FF)
(def PURPLE 0xFF00FF)

(def sphero-action-list (atom  (map commands/colour [RED YELLOW BLUE PURPLE])))
(def sphero-action-counter (atom 0))

(defn sphero-action []
  (let [idx (mod @sphero-action-counter (count @sphero-action-list))
        _ (println idx)
        action (nth @sphero-action-list idx)
        _ (println action)]
    (commands/execute action))
  (swap! sphero-action-counter inc))

(sphero-action)

; Register a handler function for the /test OSC address
; The handler takes a message map with the following keys:
;   [:src-host, :src-port, :path, :type-tag, :args]
(osc-handle server "/meta-ex/beat" (fn [msg] (println " hi MMSG: " msg)))
