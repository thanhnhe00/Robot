# Workflows for Antigravity IDE (Robot Assistant Project)

This document outlines the standard workflows for contributing to the Robot project.

## 1. General Task Workflow
1.  **Select an Issue:** Pick ONE issue from the current active Phase (refer to `docs/ROADMAP.md`).
2.  **Plan & Confirm:** Formulate a plan for the task and summarize any uncertainties. Ask the user for confirmation before writing code.
3.  **Branching:** Create a new branch for the feature or fix (e.g., `feat/phase1-aiprovider`).
4.  **Implement & Test:** Write the code in small, commit-able chunks. Follow conventions. Run and write tests (`cd backend && python -m pytest -q`).
5.  **Documentation:** Update `docs/` and create ADRs (`docs/decisions/`) if architectural decisions were made. Update checklists in the relevant issue markdown file.
6.  **Report:** Report back to the user with what was done, evidence (test results, benchmarks, logs), remaining work, and any assumptions made (labeled as `ASSUMPTION` or `CHƯA BIẾT`).
7.  **Review:** Wait for the user to test the changes and confirm before moving to the next task.

## 2. Working with the Backend (FastAPI)
*   **Environment Setup:**
    ```bash
    cd backend
    python -m venv .venv
    source .venv/bin/activate  # Linux/macOS
    # .venv\Scripts\activate   # Windows
    pip install -r requirements.txt
    cp .env.example .env
    ```
*   **Running the Server:**
    ```bash
    cd backend
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    ```
*   **Testing:**
    ```bash
    cd backend
    python -m pytest -q
    ```
*   **API Testing:** You can test the API using curl:
    ```bash
    curl -X POST http://localhost:8000/chat \
      -H "Content-Type: application/json" \
      -d '{"session_id":"thanh","text":"Bây giờ là mấy giờ?"}'
    ```

## 3. Adding a New Action
1.  **Update the Backend:**
    *   Add the new action type and its validation logic to `backend/app/actions.py` (specifically in `_validate_params`).
    *   Add unit tests for the new validation logic in `backend/tests/`.
2.  **Update Prompts:**
    *   Update the system prompts (e.g., in `ai/prompts/v1/system.md`) to instruct the LLM on how and when to use the new action, including the expected JSON format.
3.  **Update Android (Phase 3+):**
    *   Implement the actual execution logic for the action in the Android app's Action Executor.
4.  **Update Documentation:**
    *   Document the new action in `.agents/rules.md` and `README.md` (or the relevant architecture doc).

## 4. Creating an ADR (Architectural Decision Record)
1.  Create a new markdown file in `docs/decisions/` named sequentially (e.g., `0006-use-websockets-for-telemetry.md`).
2.  Include: Title, Status (Proposed/Accepted/Rejected), Context (why this decision is needed), Decision (what is being chosen), and Consequences (pros/cons/impacts).
3.  Do not implement the major change until the ADR is marked as "Accepted".
