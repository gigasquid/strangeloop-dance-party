(ns strangeloop-dance-party.core
  (:require [overtone.osc :refer :all]
            [ellipso.core :as core]
            [ellipso.commands :as commands]
            [strangeloop-dance-party.sphero :as sphero]))

(comment
  ;; all the initialization  by hand goes here....
  ;; crosss your fingers and hope everyone connects okay


  ;; ls /dev/tty.Sphero*
 ;; init the sphero
  (def sphero (core/connect "/dev/tty.Sphero-RBR-AMP-SPP"))

  (comment  (core/disconnect sphero))

  (commands/execute sphero (commands/colour 0xFF0000)) ;;red
  (commands/execute sphero (commands/colour 0xFF8000))
  (commands/execute sphero (commands/roll 0 0))
  (commands/execute sphero (commands/roll 0x4B 0))

  (comment  (core/disconnect sphero))

  )


(declare sphero)
(defonce PORT 4249)

; start a server and create a client to talk with it
(defonce server (osc-server PORT))
(def incoming-data (atom {}))


; Register a handler function for the /test OSC address
; The handler takes a message map with the following keys:
;   [:src-host, :src-port, :path, :type-tag, :args]

(defn update-incoming-data [key val]
  (swap! incoming-data assoc key val))

(osc-handle server "/beat" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println " hi MMSG: " msg)
                             (println "In beat" (pr-str @incoming-data))
                             (println (get @incoming-data "/beat"))
                             (let [beat (get @incoming-data "/beat")]
                               (println (get @incoming-data "/beat"))
                               (sphero/sphero-action sphero beat))))

(osc-handle server "/amp" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println "In amp " (pr-str @incoming-data))
                             (let [amp (get @incoming-data "/amp")]
                               (println (get @incoming-data "/amp"))
                               (sphero/change-color sphero amp))))



;;;; live coding

(comment

  (sphero/change-sphero-color-channels {:red true :green true :blue true})
  (sphero/change-sphero-color-channels {:red true :green false :blue true})
  (sphero/change-sphero-beat-mod 2)
  ;(change-sphero-moves (map commands/colour [RED YELLOW BLUE PURPLE]))
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 70) 0)
                               (commands/roll (sphero/speed->hex 70) 180)])
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 0)
                               (commands/roll (sphero/speed->hex 100) 180)])
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 70) 90)
                               (commands/roll (sphero/speed->hex 70) 270)])
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 90)
                               (commands/roll (sphero/speed->hex 100) 270)])
  (sphero/stop! sphero)
  )

