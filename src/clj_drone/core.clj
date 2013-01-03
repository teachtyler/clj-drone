(ns clj-drone.core
  (:import (java.net DatagramPacket DatagramSocket InetAddress))
  (:import (java.util BitSet))
  (:use [clojure.test]))


(def default-drone-ip "192.168.1.1")
(def default-at-port 5556)

(defn initialize [ip port]
  (def drone-host (InetAddress/getByName ip))
  (def at-port port)
  (def socket (DatagramSocket. ))
  (def counter (atom 0)))


(defn send-command [data]
  (.send socket
    (new DatagramPacket (.getBytes data) (.length data) drone-host at-port)))

(def commands
  {
    :take-off  {:command-class "AT*REF" :command-bit-vec [9 18 20 22 24 28]}
    :land      {:command-class "AT*REF" :command-bit-vec [18 20 22 24 28]}
    :emergency {:command-class "AT*REF" :command-bit-vec [8 18 20 22 24 28]}
    })


(defn build-command-int [command-bit-vec]
  (reduce #(bit-set %1 %2) 0 command-bit-vec))

(defn build-command [command-key counter]
  (let [{:keys [command-class command-bit-vec]} (command-key commands)]
    (str
      command-class
      "="
      counter
      ","
      (build-command-int command-bit-vec)
      "\r")))

(defn drone [command-key]
  (let [ seq-num (swap! counter inc)
         data (build-command command-key seq-num )]
    (.send socket
      (new DatagramPacket (.getBytes data) (.length data) drone-host at-port))))


;;; sample program
drone-host
(send-command "AT*REF=1,290718208\r")
(send-command "AT*REF=2,290717696\r")
(send-command "AT*REF=3,290718208\r")

(initialize default-drone-ip default-at-port)
(drone :take-off)
(drone :land)
(drone :emergency)


;;;; tests


(deftest building-command-int
  (is (= 290717696 (build-command-int [18 20 22 24 28])))
  (is (= 290718208 (build-command-int [9 18 20 22 24 28]))))

(deftest building-commands
  (is (= (build-command :take-off 1) "AT*REF=1,290718208\r"))
  (is (= (build-command :land 2) "AT*REF=2,290717696\r")))

(run-tests 'clj-drone.core)
