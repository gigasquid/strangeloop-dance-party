(ns strangeloop-dance-party.sphero
  (:require [ellipso.core :as core]
            [ellipso.commands :as commands]))


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

(defn stop! [sphero]
  (change-sphero-moves [])
  (commands/execute sphero (commands/roll 0 0)))


(defn speed->hex [speed]
  "takes a value from 0-100 and translates it to hex"
  (Long/decode (str "0x"(format "%02x" speed) )))

(defn rgb->hex [r g b]
  (Long/decode (str "0x"
                (apply str (map #(format "%02x" %) [r g b])))))

(defn change-color [sphero amplitude]
  "given and amplitude between 0-1 changes the color according
   to what color channels are open"
  (let [color-int (int (* 255 amplitude))
        r (if (:red @sphero-color-channels) color-int 0)
        g (if (:green @sphero-color-channels) color-int 0)
        b (if (:blue @sphero-color-channels) color-int 0)]
    (println [r g b])
    (commands/execute sphero (commands/colour (rgb->hex r g b)))))


(defn do-action? [beat-num]
  (and
   (pos? (count @sphero-action-list))
   (zero? (mod beat-num @sphero-beat-mod))))


(defn sphero-action [sphero beat]
  (try
    (when (do-action? beat)
     (let [idx (mod @sphero-action-counter (count @sphero-action-list))
           _ (println idx)
           action (nth @sphero-action-list idx)
           _ (println action)]
       (do (commands/execute sphero action))
       (swap! sphero-action-counter inc)))
    (catch Exception e (println "Sphero errror" (.getMessage e) (pr-str (.getStackTrace e))))))

