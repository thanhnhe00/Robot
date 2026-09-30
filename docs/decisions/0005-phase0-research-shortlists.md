# ADR-0005: Phase 0 research shortlists and deferred device choices
Date: 2026-09-30
Status: Proposed

Decision:
Use the documented candidates and defer device-dependent selection to the phase that measures or integrates each component.

Why:
Official documentation establishes APIs and candidate support, but it does not establish performance, compatibility, Vietnamese quality, or USB charging behavior on the project’s specific Samsung Galaxy Z Flip5 (Snapdragon 8 Gen 2 for Galaxy, 8GB RAM, 512GB storage).

Alternatives:
- Select one runtime/model/voice/transport from documentation alone.
- Keep every research question open until all hardware is purchased and tested.

Trade-offs:
- The shortlist enables Phase 1 and later spike planning without claiming unmeasured compatibility.
- Runtime/model selection waits for Phase 4 measurements; voice selection waits for Phase 5; phone-to-ESP32 transport waits for Phase 9 hardware checks.
- “DONE” in the Phase 0 research backlog means desk research and limits are documented, not that device benchmarking is complete.

Chosen:
- LLM: llama.cpp CPU/GGUF as Phase 4 baseline candidate; MLC/Adreno and ExecuTorch Qualcomm/QNN (`SM8550`) as optional comparisons, subject to device smoke tests. Shortlist Qwen3-0.6B, Qwen3-1.7B and Gemma 3 1B IT, subject to license review and benchmark.
- Voice: compare Android on-device speech APIs and sherpa-onnx for Vietnamese STT; Android TTS and sherpa/Piper voices for TTS. Vietnamese wake word remains unselected.
- Android: Kotlin is the current integration-oriented proposal, not an accepted framework decision.
- ESP32 link: USB, BLE and Wi-Fi remain open; validate Z Flip5 USB host and charge-through behavior before choosing.

Evidence:
- `docs/research/A-llm.md`
- `docs/research/B-voice.md`
- `docs/research/C-android-hardware.md`

Acceptance required:
Project owner review before changing this ADR to Accepted or Rejected.
