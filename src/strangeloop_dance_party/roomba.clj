(ns strangeloop-dance-party.roomba
  (:import  roombacomm.RoombaCommSerial))


(defn forward [roomba amp]
  "amplitude 0-1 is matched to speed is between 0 -500"
  (let [speed (int (* amp 500))]
    (.goStraightAt roomba speed)))

(defn backward [roomba amp]
  "amplitude 0-1 is matched to speed is between 0 -500"
  (let [speed (* -1 (int (* amp 500)))]
    (.goStraightAt roomba speed)))

(defn spin-left [roomba amp]
  "amplitude 0-1 is matched to speed is between 0 -500"

  (println "Spinning left with " amp)
  (let [speed (int (* amp 500))]
    (.spinLeftAt roomba speed)))

(defn spin-right [roomba amp]
  "amplitude 0-1 is matched to speed is between 0 -500"
  (let [speed (int (* amp 500))]
    (.spinRightAt roomba speed)))


(def roomba-action-list (atom []))
(def roomba-action-counter (atom 0))
(def roomba-beat-mod (atom 1))


(defn change-roomba-beat-mod [num]
  (reset! roomba-beat-mod num))

(defn change-roomba-moves [action-list]
  (reset! roomba-action-list action-list)
  (reset! roomba-action-counter 0))


(defn stop! [roomba]
  (change-roomba-moves [])
  (.stop roomba))


(defn do-action? [beat-num]
  (and
   (pos? (count @roomba-action-list))
   (zero? (mod beat-num @roomba-beat-mod))))


(defn roomba-action [roomba beat]
  (try
    (when (do-action? beat)
     (let [idx (mod @roomba-action-counter (count @roomba-action-list))
           _ (println idx)
           action (nth @roomba-action-list idx)
           _ (println action)]
       (do (action roomba))
       (swap! roomba-action-counter inc)))
    (catch Exception e (println "Roomba errror" (.getMessage e) (pr-str (.getStackTrace e))))))


(comment

  (change-roomba-beat-mod 2)
  (change-roomba-moves [#(spin-right (get @incoming-data "/amp"))
                        #(spin-left (get @incoming-data "/amp"))])
   (get @incoming-data "/amp")
  )
