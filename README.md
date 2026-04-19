# cljs-claude

ClojureScript bindings for the [Claude Agent SDK](https://github.com/anthropics/claude-agent-sdk-typescript).

[![Clojars Project](https://img.shields.io/clojars/v/io.github.snits/cljs-claude.svg)](https://clojars.org/io.github.snits/cljs-claude)

## Status

`0.1.0` — primitives layer. Exposes the I/O-agnostic [context-map pattern](#design) and a one-shot `chat` function. Callers supply their own `:send-fn` until the SDK-wrapped `send-fn` ships in `0.1.1` (see [Roadmap](#roadmap)).

## Install

### deps.edn / Leiningen

```clojure
;; deps.edn
{:deps {io.github.snits/cljs-claude {:mvn/version "0.1.0"}
        funcool/promesa             {:mvn/version "11.0.678"}}}
```

```clojure
;; project.clj
[io.github.snits/cljs-claude "0.1.0"]
```

### shadow-cljs

`shadow-cljs` consumes `deps.edn`, so declare the dep there:

```clojure
;; shadow-cljs.edn
{:deps {:aliases [:dev]}
 :builds {...}}
```

### nbb

```clojure
;; nbb.edn
{:deps {io.github.snits/cljs-claude {:mvn/version "0.1.0"}}}
```

Run with `nbb your-script.cljs`. (Requires `bb` on PATH for Clojars dep resolution.)

### npm peer dependency

Once you wire a real `:send-fn` to the upstream SDK, your project will also need:

```shell
npm install @anthropic-ai/claude-agent-sdk
```

`cljs-claude` itself does not pull the SDK in as a dep — that stays at the consumer's boundary.

## Usage

The library is I/O-agnostic. You pass a `context` map that carries a `:send-fn`; `chat` invokes it with a prepared request and returns a [promesa](https://github.com/funcool/promesa) promise.

```clojure
(require '[cljs-claude.client :as client]
         '[promesa.core :as p])

;; A send-fn takes a request map and returns a promise of the response.
;; For testing, return a resolved promise directly:
(def test-context
  {:send-fn (fn [request]
              (p/resolved {:role "assistant" :content "hello"}))})

(-> (client/chat test-context "hi")
    (p/then (fn [response]
              (println (:content response)))))
;; => hello
```

The request shape handed to `:send-fn` is:

```clojure
{:messages [{:role "user" :content <prompt>}]}
```

matching the Claude Messages API. Multi-turn extension is a `conj` on `:messages`.

## Design

Two axioms shape the codebase:

- **Translate at the boundary.** JS ↔ CLJS data conversion lives only in dedicated interop namespaces (coming in `0.1.1`). Public API surface is CLJS data all the way down.
- **I/O-agnostic.** Library code never opens sockets, processes, or streams directly. Everything effectful routes through functions on the `context` map. Swapping `:send-fn` for a fake is how you test; there are no mocks in the library.

Async is [promesa](https://github.com/funcool/promesa) end-to-end. Callers that need channel semantics (`alts!`, multi-stream composition) can wrap promesa values into `core.async` at their own boundary — `cljs-claude` does not pull `core.async` in as a dep.

Reference architecture: [metosin/mcp-toolkit](https://github.com/metosin/mcp-toolkit) — same I/O-agnostic, promesa-first pattern for JSON-RPC over stdio.

## Roadmap

- **`0.1.1`** — real `:send-fn` that wraps `@anthropic-ai/claude-agent-sdk`'s `query()`. Consumers will be able to skip hand-rolling one.

## License

Copyright © 2026 Jerry Snitselaar

Distributed under the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/). See [`LICENSE`](LICENSE).
