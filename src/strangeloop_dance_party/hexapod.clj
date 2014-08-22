(ns strangeloop-dance-party.hexapod
  (:require [serial-port :as serial]))

(def modes [:walk-mode :translate-mode :rotate-mode :single-leg-mode])
(def LT-button 128)
(def RT-button 64)
(def L6-button 32)
(def L5-button 16)
(def L4-button8)
(def R3-button 4)
(def R2-button 2)
(def R1-button 1)
(def CENTER 128)
(declare port)


(def modes {0 :walk-mode
            1 :translate-mode
            2 :rotate-mode
            3 :single-leg-mode})

(def current-mode (atom 0))

(defn checksum [v]
  (mod (- 255 (reduce + v)) 256))

(defn vec->bytes [v]
  (byte-array (map #(-> % (Integer.) (.byteValue) (byte)) v)))

(defn build-packet [r-vert r-horz l-vert l-horz buttons]
  [255 ;header
   r-vert
   r-horz
   l-vert
   l-horz
   buttons
   0
   (checksum [r-vert r-horz l-vert l-horz buttons])])

(defn send-robot [packet]
  (serial/write port (vec->bytes packet)))

(defn good-range? [speed]
  (and (pos? speed) (>= 100 speed)))

(def command-queue (atom []))
(def default-command (build-packet CENTER CENTER CENTER CENTER 0))


;;values between 129-254
(defn up [speed]
  "joystick up for speed between 1-100"
  (if (good-range? speed)
    (int (+ 129 (* 125 (/ speed 100.0))))
    CENTER))

;;values between 0 and 125
(defn down [speed]
  "joystick down speed between 1-100"
  (if (good-range? speed)
    (int (- 125 (* 125 (/ speed 100.0))))
    CENTER))


;;walk forward Left Vertical 129-254
;;walk backwards Left Vertical 0 - 127
;;walk right Left Horizontal 129 - 254
;;walk left Left Horizontal 0 -127
;;middle at 128

;;turn right Right Horizontal 129-254
;;turn left Right Horizontal 0 - 127
;;Right Vertical always 128
;;middle at 128


(defn send-command-from-queue []
  (do (println "command queue is " @command-queue)
      (let [command (or (first @command-queue) default-command)]
       ; (println "Sending ..." command)
        (reset! command-queue (rest @command-queue))
        (send-robot command)
        (Thread/sleep 33))))

(defn add-command [command]
  (swap! command-queue conj command))


(defn sit-up []
  "raise up to a walking height"
  (add-command (build-packet CENTER CENTER CENTER CENTER L5-button)))

(defn change-mode [mode-value]
  "Change modes (left-top-button) until we reach the mode-value
     {0 :walk-mode 1 :translate-mode 2 :rotate-mode 3 :single-leg-mode}"
  (when-not (= mode-value (get modes @current-mode))
    (add-command (build-packet CENTER CENTER CENTER CENTER LT-button))
    (swap! current-mode #(mod (inc %) 4))
    (change-mode mode-value)))

(defn good-move-range? [x]
  (if (and (< 0 x)
             (< x 255))
    :good
    (do
      (println "bad move range: " x)
      false)))


(defn move [rv rh lv lh]
  "generic move with left and right joystick values must be in a range from 1 - 254"
  (when (and (good-move-range? rv) (good-move-range? rh)
             (good-move-range? lv) (good-move-range? lh))
    (add-command (build-packet rv rh lv lh 0))))

(defn walk-forward [speed]
  "walk forward speed between 1-100"
  (add-command (build-packet CENTER CENTER (up speed) CENTER 0)))

(defn walk-backwards [speed]
  "walk backwards speed between 1-100"
  (add-command (build-packet CENTER CENTER (down speed) CENTER 0)))

(defn walk-right [speed]
  "walk right speed between 1-100"
  (add-command (build-packet CENTER CENTER CENTER (up speed) 0)))

(defn walk-left [speed]
  "walk right speed between 1-100"
  (add-command (build-packet CENTER CENTER CENTER (down speed) 0)))

(defn turn-right [speed]
  "turn right speed between 1-100"
  (add-command (build-packet CENTER (up speed) CENTER CENTER 0)))

(defn turn-left [speed]
  "turn left speed between 1-100"
  (add-command (build-packet CENTER (down speed) CENTER CENTER 0)))


(defn stop []
  "stop hexapod"
  (add-command (build-packet CENTER CENTER CENTER CENTER 0)))

(defn good-bye []
  (serial/close port))

(def talk-on? (atom false))
(def robot-agent (agent []))

(defn start-communicator []
  (send robot-agent (fn [_]
                      (while @talk-on?
                        (send-command-from-queue)))))

;;; moves for the beat and amplitude

;;; need to be in translate-mode

(defn up-down [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move % CENTER CENTER CENTER) move-range)))


(defn twist-right-left [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move CENTER % CENTER CENTER) move-range)))

(defn shift-forward-backwards [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move CENTER CENTER % CENTER) move-range)))

(defn shift-left-right [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move CENTER CENTER CENTER %) move-range)))


(defn wave1 [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move % % CENTER CENTER) move-range)))

(defn wave2 [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move % CENTER % CENTER) move-range)))


(defn wave3 [amplitude]
  "maps a set of moves through a range with an amplitude from 0-1
   -- if a move set is being peformed then don't send it"
  (assert (and (pos? amplitude) (>= 1 amplitude)) )
  (let [num-moves (int  (* amplitude 10))
        move-range (conj (vec (range 10 200 num-moves)) 200)]
    (println "num moves " num-moves " move-range " move-range)
    (map #(move % CENTER % CENTER) move-range)))

;;;;

(agent-errors robot-agent)
(comment

  ;; Use this command to see what port your serial port
  ;; is assinged to
  (serial/list-ports)

  ;; replace the USB0 with whater it shows
  (def port (serial/open "/dev/tty.usbserial-A60205ME" 38400))


  (reset! talk-on? true)
  (start-communicator)

  (comment (reset! talk-on? false))



  (sit-up)
  (change-mode :translate-mode)

  (up-down 0.5)
  (twist-right-left 0.5)
  (shift-forward-backwards 0.5)
  (shift-left-right 0.5)
  (wave1 0.5)
  (wave2 0.5)
  (wave3 0.5)
  

  
  ;; up down  range between 10 and 200 safter
  ;; 20 is slow mave through range
  ;; 100 is faster move through the range
  (map #(move % CENTER CENTER CENTER) (range 10 200 10))
  (map #(move % CENTER CENTER CENTER) (reverse (range 10 200 20)))

  ;; up down
  (map #(move % CENTER CENTER CENTER) (range 10 200 100))

  ;; twist right left
  (map #(move CENTER % CENTER CENTER) (range 10 200 20))
  (map #(move CENTER % CENTER CENTER) (range 10 200 100))


  ;; shift forward backwards
  (map #(move CENTER CENTER % CENTER) (range 10 200 20))

  ;; shift left right
  (map #(move CENTER CENTER CENTER %) (range 10 200 20))
  
  (map #(move %1 %2 CENTER CENTER) (range 10 200 50) (range 10 200 50))

  (map #(move %1 CENTER %2 CENTER) (range 10 200 10) (range 10 200 10))
  (map #(move %1 CENTER CENTER %2) (range 10 200 10) (range 10 200 10))

  (reset! talk-on? false)


  (change-mode :rotate-mode)
  @current-mode
  
  (repeat 5  (walk-forward 20))
  @talk-on
  @command-queue
  (reset! talk-on false)

  (reset! talk-on false)


  
  (sit-up)
  (walk-forward 20)
  (walk-backwards 10)
  (walk-right 10)
  (walk-left 10)
  (turn-right 10)
  (turn-left 10)

  (stop)

  (good-bye)

  )


