import sqlite3
from contextlib import closing
from typing import Protocol

from .config import settings


class ConversationRepository(Protocol):
    def initialize(self) -> None: ...

    def add_message(self, session_id: str, role: str, content: str) -> None: ...

    def get_history(self, session_id: str, limit: int) -> list[dict]: ...


class SQLiteConversationRepository:
    def __init__(self, path: str):
        self.path = path

    def _connect(self) -> sqlite3.Connection:
        return sqlite3.connect(self.path)

    def initialize(self) -> None:
        with closing(self._connect()) as connection:
            connection.execute(
                """CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )"""
            )
            connection.execute(
                "CREATE INDEX IF NOT EXISTS idx_session ON messages(session_id, id)"
            )
            connection.commit()

    def add_message(self, session_id: str, role: str, content: str) -> None:
        with closing(self._connect()) as connection:
            connection.execute(
                "INSERT INTO messages(session_id, role, content) VALUES (?,?,?)",
                (session_id, role, content),
            )
            connection.commit()

    def get_history(self, session_id: str, limit: int) -> list[dict]:
        with closing(self._connect()) as connection:
            rows = connection.execute(
                "SELECT role, content FROM messages "
                "WHERE session_id=? ORDER BY id DESC LIMIT ?",
                (session_id, limit),
            ).fetchall()
        return [{"role": role, "content": content} for role, content in reversed(rows)]


class DisabledConversationRepository:
    def initialize(self) -> None:
        pass

    def add_message(self, session_id: str, role: str, content: str) -> None:
        pass

    def get_history(self, session_id: str, limit: int) -> list[dict]:
        return []


repository: ConversationRepository
if settings.persistence_enabled:
    repository = SQLiteConversationRepository(settings.db_path)
else:
    repository = DisabledConversationRepository()
