(ns cljs-claude.client
  "Public entry point for cljs-claude. I/O-agnostic: all SDK calls route
   through functions supplied on the `context` map, so library code never
   opens sockets, processes, or streams directly."
  (:require [promesa.core :as p]))

(defn chat
  "One-shot chat completion.

   Args:
     context - map with at least {:send-fn fn}. :send-fn receives the
               prepared request and returns a promesa promise resolving
               to the response.
     prompt  - user prompt (string).

   Returns:
     A promesa promise resolving to the response from :send-fn."
  [context prompt]
  (let [{:keys [send-fn]} context]
    (assert send-fn "cljs-claude: context must contain :send-fn")
    (send-fn {:messages [{:role "user" :content prompt}]})))
