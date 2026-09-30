# Rules for Antigravity IDE (Robot Assistant Project)

This project has strict architectural and safety rules. All AI agents MUST adhere to these rules when interacting with the codebase.

## 1. Fundamental Architectural Rules
*   **LLM is an Advisor, Not an Executor:** The Large Language Model (LLM) only *proposes* actions. It does NOT execute them directly. The Android application is the ultimate executor, and the FastAPI backend validates the proposals. (ADR-0001)
*   **No Direct Hardware Control:** The LLM must *never* be allowed to control hardware (motors, ESP32) directly.
*   **Agnostic Model Provider:** Do not lock the system into a single LLM provider (like OpenAI or Anthropic). All model access must go through the `AIProvider` abstraction (e.g., `LocalProvider`, `LaptopProvider`, `CloudProvider`, `MockProvider`). This allows swapping models via configuration.
*   **Local-First / Hybrid Architecture:** The system prioritizes local execution: Phone -> Laptop/Local Server -> Cloud. Do not create strict dependencies on cloud services if local alternatives exist.
*   **Safety Pipeline:** The Android app implements a safety pipeline: Schema Validation -> Permissions -> Whitelist -> Parameter Limits.

## 2. Coding & Development Guidelines
*   **Vietnamese First:** All documentation (README, ADRs, PR descriptions), user-facing text, and system prompts must be in Vietnamese, as this is a Vietnamese voice assistant.
*   **Decision Records (ADR):** Any significant architectural change MUST be documented in an Architectural Decision Record (ADR) in `docs/decisions/` before implementation.
*   **No Faking Information:** If the agent or model does not know something, it must explicitly state that it does not know. Do not hallucinate actions outside the predefined list.
*   **Test-Driven:** All new features (especially actions and parsing logic in the backend) must have accompanying tests in `backend/tests/`.
*   **Cross-Cutting Concerns:** Pay attention to evaluation metrics (action accuracy, latency, RAM), dataset strategy (golden test sets), and reliability testing across all phases.

## 3. Communication Protocol (The JSON Contract)
*   The LLM MUST output *ONLY* a valid JSON object. No extra text, markdown formatting (other than the json block if strictly necessary, but preferably raw json), or explanations outside the JSON.
*   **Format:** `{"response": "Robot's spoken reply", "action": {"type": "action_name", "params": {...}}}`
*   If no action is needed (just chatting), `"action": null`.
*   **Current Whitelisted Actions:**
    *   `get_time`: params `{}`
    *   `get_battery`: params `{}`
    *   `set_alarm`: params `{"time": "HH:MM"}` (24-hour format)
    *   `open_app`: params `{"package": "com.google.android.youtube | com.android.chrome | com.android.settings | com.spotify.music"}`
    *   `set_volume`: params `{"level": 0-100}`

## 4. Hardware Safety (For context, mostly handled by firmware)
*   **Layer 0 (Hardware):** Physical E-STOP cuts power. This overrides all software.
*   **Layer 1 (Firmware):** Watchdog timers, speed/time limits, and automatic stop on connection loss or obstacle detection.
