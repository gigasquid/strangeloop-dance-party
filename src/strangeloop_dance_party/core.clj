(ns strangeloop-dance-party.core
  (:require [ellipso.core :as core]
            [ellipso.commands :as commands]
            [overtone.osc :refer :all]))

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
  (commands/execute sphero (commands/heading 60))

  (comment  (core/disconnect sphero))

  )


(def PORT 4242)

; start a server and create a client to talk with it
(def server (osc-server PORT))
(def incoming-data (atom {}))




;;; Sphero specific things

(def RED 0xFF0000)
(def YELLOW 0xFF8000)
(def BLUE 0x0000FF)
(def PURPLE 0xFF00FF)


(def sphero-action-list (atom []))
(def sphero-action-counter (atom 0))
(def sphero-beat-mod (atom 1))
(def sphero-color-channels (atom {:red true :green false :blue false}))


(defn change-sphero-beat-mod [num]
  (reset! sphero-beat-mod num))

(defn change-sphero-moves [action-list]
  (reset! sphero-action-list action-list)
  (reset! sphero-action-counter 0))

(defn change-sphero-color-channels [colormap]
  (reset! sphero-color-channels colormap))

(defn stop! []
  (change-sphero-moves [])
  (commands/execute sphero (commands/roll 0 0)))


(defn rgb->hex [r g b]
  (Long/decode (str "0x"
                (apply str (map #(format "%02x" %) [r g b])))))

(defn change-color [amplitude]
  "given and amplitude between 0-1 changes the color according
   to what color channels are open"
  (let [color-int (int (* 255 amplitude))
        r (if (:red @sphero-color-channels) color-int 0)
        g (if (:green @sphero-color-channels) color-int 0)
        b (if (:blue @sphero-color-channels) color-int 0)]
    (println [r g b])
    (commands/execute sphero (commands/colour (rgb->hex r g b)))))

(change-color 0.5)

(defn do-action? []
  (let [beat-num (get @incoming-data "/beat")]
   (and
    (pos? (count @sphero-action-list))
    (zero? (mod beat-num @sphero-beat-mod)))))


(defn sphero-action []
  (try
    (when (do-action?)
     (let [idx (mod @sphero-action-counter (count @sphero-action-list))
           _ (println idx)
           action (nth @sphero-action-list idx)
           _ (println action)]
       (do (commands/execute sphero action))
       (swap! sphero-action-counter inc)))
    (catch Exception e (println "Sphero errror" (.getMessage e) (pr-str (.getStackTrace e))))))


(comment 
  (sphero-action)
  (do-action?)
  (stop!)

  (change-sphero-color-channels {:red true :green true :blue true})
  (change-sphero-beat-mod 4)
  (change-sphero-moves (map commands/colour [RED YELLOW BLUE PURPLE]))
  (change-sphero-moves [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])
  (change-sphero-moves [(commands/roll 0x4B 90) (commands/roll 0x4B 270)]))

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
                             (sphero-action)))

(osc-handle server "/amp" (fn [msg]
                             (update-incoming-data (:path msg) (first (:args msg)))
                             (println "In amp " (pr-str @incoming-data))
                             (let [amp (get @incoming-data "/amp")]
                               (println (get @incoming-data "/amp"))
                               (change-color amp))))


