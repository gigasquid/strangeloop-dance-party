(ns strangeloop-dance-party.core
  (:require [overtone.osc :refer :all]
            [ellipso.core :as core]
            [ellipso.commands :as commands]
            [strangeloop-dance-party.sphero :as sphero]
            [strangeloop-dance-party.roomba :as roomba]))

;;This is the main file to work on for the live coding
;; first comment section is the stuff needed to initially connect to
;; all the robots

;; The oschandle stuff is how it listens for events.
;; To kick off the test simulation harness call bin/st-osc.tb 4249

;; Finally in the last comment section is the live coding bit to
;; give the robots actions and can the responses to the live music input

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


  ;;; roomba
   (def roomba (RoombaCommSerial. ))

  ;;Find your port for your Roomba
  (map println (.listPorts roomba))

  (def portname "/dev/tty.FireFly-943A-SPP")
  (.connect roomba portname)
  (.startup roomba)  ;;puts Roomba in safe Mode
  ;; What mode is Roomba in?
  (.modeAsString roomba)
  (.control roomba)
  (.updateSensors roomba) ; returns true if you are connected
  (.playNote roomba 72 40)
  (roomba/stop! roomba)
  (roomba/spin-right roomba 0.2)
  (spin-left roomba 0.2)
  (comment (.disconnect roomba))

  (def roomba roomba/roomba)
  )


(declare sphero)
(declare roomba)
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
                              ; (sphero/sphero-action sphero beat)
                               (roomba/roomba-action roomba beat))))

(osc-handle server "/amp" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println "In amp " (pr-str @incoming-data))
                             (let [amp (get @incoming-data "/amp")]
                               (println (get @incoming-data "/amp"))
                               ;(sphero/change-color sphero amp)
                               )))



;;;; live coding

(defn current-amplitude []
  (get @incoming-data "/amp"))


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

  ;;; roomba


  (roomba/change-roomba-beat-mod 4)

  (roomba/change-roomba-moves [(fn [roomba] (roomba/spin-left roomba (current-amplitude)))
                               (fn [roomba] (roomba/spin-right roomba (current-amplitude)))])

  (roomba/change-roomba-moves [(fn [roomba] (roomba/forward roomba (current-amplitude)))
                               (fn [roomba] (roomba/backward roomba (current-amplitude)))])


  (roomba/stop! roomba)

  )

