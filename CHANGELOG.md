# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `README.md` documenting the I/O-agnostic context-map pattern, install snippets for `deps.edn` / `shadow-cljs` / `nbb`, and the design axioms.
- `CHANGELOG.md` (this file).
- Error-path test coverage for `cljs-claude.client/chat`: rejection propagation, missing-`:send-fn` assertion, nil-prompt passthrough, response passthrough-unchanged.

### Changed

- `cljs-claude.client/chat` now asserts `:send-fn` is present on the `context` map. A missing `:send-fn` previously produced a confusing CLJS-internal `TypeError`; it now raises a clear error at the API surface.

## [0.1.0] — 2026-04-19

Initial release to [Clojars](https://clojars.org/io.github.snits/cljs-claude). Primitives layer only — downstream consumers supply their own `:send-fn`.

### Added

- `cljs-claude.client/chat` — one-shot chat completion routed through a caller-supplied `:send-fn` on the `context` map. Returns a [promesa](https://github.com/funcool/promesa) promise.
- Scaffolding: `deps.edn`, `shadow-cljs.edn`, `pom.xml`, `package.json`, EPL-2.0 `LICENSE`.
- Dev aliases: `:dev`, `:test`, `:outdated`, `:nrepl`, `:jar`, `:deploy`.

### Notes

- Shipped intentionally docs-free as a release-pipeline exercise. Docs added in [Unreleased].
- Shipped without a real SDK-wrapping `:send-fn` by design — see roadmap in `README.md`.

[Unreleased]: https://github.com/snits/cljs-claude/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/snits/cljs-claude/releases/tag/v0.1.0
