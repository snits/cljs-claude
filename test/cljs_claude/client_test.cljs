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

(deftest chat-propagates-rejection
  (async done
    (let [boom     (ex-info "upstream boom" {:code 500})
          fake-sdk (fn [_request] (p/rejected boom))
          context  {:send-fn fake-sdk}]
      (-> (client/chat context "ping")
          (p/then (fn [_response]
                    (is false "chat should have rejected, not resolved")
                    (done)))
          (p/catch (fn [err]
                     (is (= boom err)
                         "chat should propagate send-fn's rejection unchanged")
                     (done)))))))

(deftest chat-asserts-send-fn-present
  (is (thrown-with-msg? js/Error #"send-fn"
        (client/chat {} "hi"))
      "chat must reject a context map without :send-fn at the API surface,
       not let a downstream nil-invocation TypeError leak out of the library"))

(deftest chat-passes-nil-prompt-through
  (async done
    (let [captured (atom nil)
          fake-sdk (fn [request]
                     (reset! captured request)
                     (p/resolved :ok))
          context  {:send-fn fake-sdk}]
      (-> (client/chat context nil)
          (p/then (fn [_]
                    (is (= {:messages [{:role "user" :content nil}]}
                           @captured)
                        "chat is passthrough on prompt — nil reaches send-fn as :content nil")
                    (done)))
          (p/catch (fn [err]
                     (is false (str "chat rejected unexpectedly on nil prompt: " err))
                     (done)))))))

(deftest chat-returns-resolved-value-unchanged
  (async done
    (let [weird-shape [:not-a-map 42 "hello"]
          fake-sdk   (fn [_request] (p/resolved weird-shape))
          context    {:send-fn fake-sdk}]
      (-> (client/chat context "ping")
          (p/then (fn [response]
                    (is (= weird-shape response)
                        "chat returns send-fn's resolved value unchanged — no coercion")
                    (done)))
          (p/catch (fn [err]
                     (is false (str "chat rejected unexpectedly: " err))
                     (done)))))))
