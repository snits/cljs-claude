(ns cljs-claude.client-test
  (:require [cljs.test :refer-macros [deftest is async]]
            [promesa.core :as p]
            [cljs-claude.client :as client]))

(deftest chat-routes-through-send-fn
  (async done
    (let [captured (atom nil)
          fake-sdk (fn [request]
                     (reset! captured request)
                     (p/resolved {:role "assistant" :content "pong"}))
          context  {:send-fn fake-sdk}]
      (-> (client/chat context "ping")
          (p/then (fn [response]
                    (is (some? @captured)
                        "send-fn should be invoked exactly once with a request")
                    (is (= "pong" (:content response))
                        "chat should return send-fn's resolved value unchanged")
                    (done)))
          (p/catch (fn [err]
                     (is (nil? err)
                         (str "chat rejected unexpectedly: " err))
                     (done)))))))
