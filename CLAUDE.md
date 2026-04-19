# Project Instructions for AI Agents

This file provides instructions and context for AI coding agents working on this project.

## Project Scale Context

- **Scope:** Solo project — Jerry + Claude. ClojureScript library, learning-focused.
- **Tool type:** Primitives / bindings layer. Consumers are separate downstream CLJS projects (REPL playgrounds, CLI tools, multi-agent orchestrators).
- **Codebase size:** Small and kept deliberately small. YAGNI hard — do not build abstractions before feeling their need.
- **Process overhead:** Minimal. Pragmatic over enterprise.
- **Default approach:** Translate-at-the-boundary (JS → CLJS data at the interop edge), immutable data, REPL-driven development. TDD per Jerry's global rules still applies.

<!-- BEGIN BEADS INTEGRATION v:1 profile:minimal hash:ca08a54f -->
## Beads Issue Tracker

This project uses **bd (beads)** for issue tracking. Run `bd prime` to see full workflow context and commands.

### Quick Reference

```bash
bd ready              # Find available work
bd show <id>          # View issue details
bd update <id> --claim  # Claim work
bd close <id>         # Complete work
```

### Rules

- Use `bd` for ALL task tracking — do NOT use TodoWrite, TaskCreate, or markdown TODO lists
- Run `bd prime` for detailed command reference and session close protocol
- Use `bd remember` for persistent knowledge — do NOT use MEMORY.md files

## Session Completion

**When ending a work session**, you MUST complete ALL steps below. Work is NOT complete until `git push` succeeds.

**MANDATORY WORKFLOW:**

1. **File issues for remaining work** - Create issues for anything that needs follow-up
2. **Run quality gates** (if code changed) - Tests, linters, builds
3. **Update issue status** - Close finished work, update in-progress items
4. **PUSH TO REMOTE** - This is MANDATORY:
   ```bash
   git pull --rebase
   git push
   git status  # MUST show "up to date with origin"
   ```
5. **Clean up** - Clear stashes, prune remote branches
6. **Verify** - All changes committed AND pushed
7. **Hand off** - Provide context for next session

**CRITICAL RULES:**
- Work is NOT complete until `git push` succeeds
- NEVER stop before pushing - that leaves work stranded locally
- NEVER say "ready to push when you are" - YOU must push
- If push fails, resolve and retry until it succeeds
<!-- END BEADS INTEGRATION -->


## Build & Test

_Pending scaffolding. `shadow-cljs.edn` + `deps.edn` land with the first code commit._

```bash
# After scaffolding:
# shadow-cljs watch <build-id>    # dev build with REPL
# shadow-cljs release <build-id>  # release build
```

## Architecture Overview

ClojureScript library wrapping the TypeScript Claude Agent SDK (`@anthropic-ai/claude-agent-sdk` on npm).

- **Target runtime:** Node, via `shadow-cljs`
- **Published as:** `io.github.snits/cljs-claude` on Clojars
- **License:** EPL-2.0 (matches Clojure ecosystem convention)
- **Design axiom:** Translate-at-the-boundary. JS/CLJS conversion happens in dedicated interop namespaces; domain code operates on CLJS data only. No JS interop leakage into the public API.
- **I/O discipline:** I/O-agnostic via plain function injection. Caller passes a `context` map (`{:session <atom> :send-fn <fn> ...}`); library code never opens sockets, streams, or processes directly. Pattern cribbed from `metosin/mcp-toolkit`.
- **No protocols / records / multimethods** until there's a real polymorphism need. Plain maps and functions first.
- **Reference architecture:** `metosin/mcp-toolkit` is the closest precedent (CLJC, promesa-based, I/O-agnostic). Treat as design template, not dependency.

### Downstream consumers (separate projects, not this repo)

- REPL playground
- CLI tools
- Multi-agent orchestrators

Consumers may run under `shadow-cljs` or `nbb`. Library authoring should be **nbb-friendly where reasonable** — keep macros tractable, avoid compile-time JS interop magic that only works under the full CLJS compiler.

## Conventions & Patterns

- **Async model:** `promesa` for promise interop. Add `core.async` helpers for streams if/when the promesa-wrapped async iterator feels ugly. Do not build both preemptively.
- **JS interop:** `applied-science/js-interop` for JS access; `cljs-bean` as escape hatch for performance-sensitive or large-object paths.
- **Data shape:** Immutable CLJS maps. Namespaced keywords when they aid clarity or prevent collision; plain keywords otherwise. No records unless there's a specific reason.
- **Schemas:** `malli` when schemas earn their keep (message shapes, coercion at the boundary). Not day one.
- **Build tool:** `shadow-cljs`. One `shadow-cljs.edn` for the library.
- **Testing:** TDD per Jerry's global rules. Library code should be testable without a live SDK connection — I/O-agnostic where possible.
- **Namespace layout:** Dedicated interop namespaces (e.g., `<name>.anthropic.client`) hold the JS boundary; pure domain namespaces (e.g., `<name>.agent.core`) hold effect-free logic. Grep-ability matters — boundary code should be physically separated from domain code.
