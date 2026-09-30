# Skills for Antigravity IDE (Robot Assistant Project)

To effectively work on this project, the AI agent needs proficiency in the following areas:

## 1. Core Languages & Frameworks
*   **Python (3.11+):** Advanced proficiency. This is the language for the backend, test scripts, and data processing.
*   **FastAPI:** Deep understanding of FastAPI for building REST APIs, dependency injection, routing, and Pydantic validation.
*   **Pytest:** Ability to write comprehensive unit and integration tests for the Python backend.
*   **Markdown:** Proficient in writing clear, structured documentation, ADRs, and issues in Markdown format.
*   **Kotlin / Android SDK (Future Phases):** Knowledge of Android app development, specifically relating to UI, background services, intents, and USB OTG/hardware communication.

## 2. System Architecture & Concepts
*   **LLM Integration:** Understanding of how to interface with LLMs (via API like Gemini, or locally via Ollama/llama.cpp). Knowledge of prompt engineering, specifically for enforcing strict JSON outputs ("Structured Output").
*   **State Machines:** Understanding how to design and implement finite state machines (e.g., IDLE -> WAKE -> LISTENING -> PROCESSING -> SPEAKING) for the Android Brain.
*   **Hardware Interfacing (Concepts):** Understanding of serial communication (USB/UART), watchdog timers, and basic embedded systems concepts for interacting with the ESP32 (Phase 9+).

## 3. Project-Specific Conventions
*   **JSON Contract Adherence:** Absolute strictness in generating and parsing the specific JSON format required by the system (`{"response": "...", "action": {...}}`).
*   **Vietnamese Language Nuances:** Ability to read, understand, and write technical documentation and user prompts accurately in Vietnamese. Understanding of Vietnamese context for STT/TTS is a plus.
*   **Safety-First Mindset:** Always prioritizing the safety layers (Schema validation, whitelists, E-STOP concepts) when designing or modifying features.

## 4. DevOps & Tooling
*   **Git & GitHub:** Standard branch/commit workflows.
*   **Docker:** Ability to create and manage Dockerfiles and docker-compose configurations (Phase 1).
*   **Bash Scripting:** Ability to read and write shell scripts for automation and testing.
