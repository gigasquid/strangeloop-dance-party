(ns strangeloop-dance-party.drone
  (:require [clj-drone.core :refer :all]))


(comment 
  (drone-initialize)
  (drone :emergency)
  (drone :init-targeting)
  (drone :target-roundel-v)
  (drone :hover-on-roundel)

                                        ;Use ip and port for non-standard drone ip/port
                                        ;(initialize ip port)
  (drone :take-off)


  (drone :anim-double-phi-theta-mixed)
  (drone :anim-wave)
  (drone :anim-turnaround)
  (drone :anim-yaw-shake)
  (drone :anim-flip-right)

  (drone-do-for 2 :up 0.3)
  (drone-do-for 3.75 :fly 0.2 0 0 0.5) ; sprial
  (drone :hover)
  (drone :land))
