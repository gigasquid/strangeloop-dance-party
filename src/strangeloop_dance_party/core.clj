(ns strangeloop-dance-party.core
  (:import  roombacomm.RoombaCommSerial)
  (:require [overtone.osc :refer :all]
            [ellipso.core :as core]
            [ellipso.commands :as commands]
            [serial-port :as serial]
            [strangeloop-dance-party.sphero :as sphero]
            [strangeloop-dance-party.roomba :as roomba]
            [strangeloop-dance-party.hexapod :as hexapod]
            [clj-drone.core :refer :all]))

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
  (.playNote roomba 69 40)
  (roomba/stop! roomba)
  (roomba/spin-right roomba 0.2)
  (spin-left roomba 0.2)
  (comment (.disconnect roomba))

  ;(def roomba roomba/roomba)


  ;;; Hexapod
  ; Use this command to see what port your serial port
  ;; is assinged to
  (serial/list-ports)

  ;; replace the USB0 with whater it shows
  (def port (serial/open "/dev/tty.usbserial-A60205ME" 38400))

  (reset! hexapod/talk-on? true)
  (hexapod/start-communicator port)

  (comment (reset! hexapod/talk-on? false))
  (comment (hexapod/good-bye))

  (hexapod/sit-up)
  (hexapod/change-mode :translate-mode)
  (hexapod/up-down 0.5)


  ;; drone
   (drone-initialize)
   (drone :take-off)
   (drone :land)
  
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


(defn current-amplitude []
  (get @incoming-data "/amp"))

(defn update-incoming-data [key val]
  (swap! incoming-data assoc key val))

(osc-handle server "/beat" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println " hi MMSG: " msg)
                             (println "In beat" (pr-str @incoming-data))
                             (println (get @incoming-data "/beat"))
                             (let [beat (get @incoming-data "/beat")]
                               (println (get @incoming-data "/beat"))
                              (sphero/sphero-action sphero beat)
                              (roomba/roomba-action roomba beat)
                               (hexapod/hexapod-action beat)
                               (println "hey")
                               )))

(osc-handle server "/amp" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println "In amp " (pr-str @incoming-data))
                             (let [amp (get @incoming-data "/amp")]
                               (println (get @incoming-data "/amp"))
                              (sphero/change-color sphero (* 10 amp))
                               )))



;;;; live coding


(comment


  ;; Sphero solo
  (sphero/change-sphero-color-channels {:red true :green false :blue false})
  (sphero/change-sphero-color-channels {:red false :green true :blue false})
  (sphero/change-sphero-color-channels {:red false :green false :blue true})
  (sphero/change-sphero-color-channels {:red true :green false :blue true})

  (sphero/change-sphero-beat-mod 8)
  ;;(sphero/change-sphero-moves (map commands/colour [RED YELLOW BLUE PURPLE]))
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 0)
                               (commands/roll (sphero/speed->hex 100) 180)])

  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 90)
                               (commands/roll (sphero/speed->hex 100) 270)])

    (sphero/stop! sphero)

  ;;; Rooombo solo


  (roomba/change-roomba-beat-mod 8)

  (roomba/change-roomba-moves [(fn [roomba] (roomba/spin-left roomba 0.8))
                               (fn [roomba] (roomba/spin-right roomba 0.8))])


  (roomba/change-roomba-moves [(fn [roomba] (roomba/forward roomba 0.8))
                               (fn [roomba] (roomba/backward roomba 0.8))])



  (roomba/stop! roomba)

  ;;;


  ;;; Sphero + Roomba

  (sphero/change-sphero-beat-mod 8)
  ;(change-sphero-moves (map commands/colour [RED YELLOW BLUE PURPLE]))
  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 0)
                               (commands/roll (sphero/speed->hex 100) 180)])

  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 90)
                               (commands/roll (sphero/speed->hex 100) 270)])

  (roomba/change-roomba-beat-mod 8)

  (roomba/change-roomba-moves [(fn [roomba] (roomba/spin-left roomba 0.8))
                               (fn [roomba] (roomba/spin-right roomba 0.8))])

  (sphero/change-sphero-beat-mod 8)
  (roomba/change-roomba-beat-mod 8)
  (roomba/stop! roomba)
  (sphero/stop! sphero)


  ;;;  Hexapod solo

  (hexapod/toggle-mode)

  (hexapod/change-hexapod-beat-mod 8)
  (hexapod/change-hexapod-moves [(fn [] )])

  (hexapod/change-hexapod-moves [(fn [] (hexapod/up-down 0.5))])

  (hexapod/change-hexapod-moves [(fn [] (hexapod/up-down (current-amplitude)))])

  (hexapod/change-hexapod-moves [(fn [] (hexapod/up-down 0.5))
                                 (fn [] (hexapod/twist-right-left 0.5))
                                 (fn [] (hexapod/shift-forward-backwards 0.5))
                                 (fn [] (hexapod/shift-left-right 0.5))])

  (hexapod/change-hexapod-moves [(fn [] (hexapod/up-down 0.5))
                                 (fn [] (hexapod/wave1 0.5))
                                 (fn [] (hexapod/wave2 0.5))
                                 (fn [] (hexapod/wave3 0.5))])


  (hexapod/toggle-mode)
  @hexapod/current-mode
  (hexapod/change-hexapod-beat-mod 6)
  (hexapod/change-hexapod-moves [(fn [] (dotimes [x 80] (hexapod/walk-forward 75)))
                                 (fn [] (dotimes [x 80] (hexapod/walk-backwards 75)))
                                 (fn [] (dotimes [x 80] (hexapod/walk-right 75)))
                                 (fn [] (dotimes [x 80] (hexapod/walk-left 75)))
                                 ])

  (hexapod/change-hexapod-moves [(fn [] )])


  ;;;;

  ;;; sphero + roomba + hexapod

  (hexapod/toggle-mode)

  (hexapod/change-hexapod-beat-mod 4)
  (hexapod/change-hexapod-moves [(fn [] (hexapod/up-down 0.5))
                                 (fn [] (hexapod/wave1 0.5))
                                 (fn [] (hexapod/wave2 0.5))
                                 (fn [] (hexapod/wave3 0.5))])

  (roomba/change-roomba-moves [(fn [roomba] (roomba/spin-left roomba 0.8))
                               (fn [roomba] (roomba/spin-right roomba 0.8))])
  (sphero/change-sphero-beat-mod 2)

  (sphero/change-sphero-moves [(commands/roll (sphero/speed->hex 100) 0)
                               (commands/roll (sphero/speed->hex 100) 180)])


  ;;; stop everyone
  (sphero/stop! sphero)
  (roomba/stop! roomba)
  (hexapod/change-hexapod-moves [(fn [] )])


  ;; drone comes in for the ending!
  )

