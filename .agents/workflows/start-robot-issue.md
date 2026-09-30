---
name: start-robot-issue
description: Starts one scoped Robot repository issue and guides it from current phase and code inspection through implementation, verification, and a user-friendly handoff.
---

# Start one Robot issue

1. Read the repository `AGENTS.md` and relevant project handoff/architecture/roadmap/issue/ADR. Treat current code and Git state as evidence; report stale documentation.
2. Identify the active phase from the latest user request plus current handoff/roadmap and keep the task to one issue. This workflow covers the whole repository and all phases; do not default to Phase 1 when the user directs another phase.
3. Apply `/robot-task`. Also apply the relevant specialist skill: `/backend-development`, `/android-development`, `/ai-model-evaluation`, `/embedded-robot-safety`, or `/research-and-adr`.
4. Preserve existing user changes. Make only the requested scoped changes; do not commit, push, or deploy unless requested.
5. Run checks only when verification is requested or is part of the task. Report exact checks and results; do not imply unrun checks passed.
6. Respond in plain Vietnamese with what changed, evidence, remaining unknowns, and one next step when the user is proceeding hands-on.
