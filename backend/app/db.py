import sqlite3
from contextlib import closing

from .config import settings


def _conn() -> sqlite3.Connection:
    return sqlite3.connect(settings.db_path)


def init_db() -> None:
    with closing(_conn()) as c:
        c.execute(
            """CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )"""
        )
        c.execute("CREATE INDEX IF NOT EXISTS idx_session ON messages(session_id, id)")
        c.commit()


def add_message(session_id: str, role: str, content: str) -> None:
    with closing(_conn()) as c:
        c.execute(
            "INSERT INTO messages(session_id, role, content) VALUES (?,?,?)",
            (session_id, role, content),
        )
        c.commit()


def get_history(session_id: str, limit: int) -> list[dict]:
    with closing(_conn()) as c:
        rows = c.execute(
            "SELECT role, content FROM messages WHERE session_id=? ORDER BY id DESC LIMIT ?",
            (session_id, limit),
        ).fetchall()
    return [{"role": r, "content": t} for r, t in reversed(rows)]
