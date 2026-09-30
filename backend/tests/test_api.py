from fastapi.testclient import TestClient

from app.main import app


def test_flow():
    with TestClient(app) as c:
        assert c.get("/health").json()["provider"] == "mock"

        r = c.post("/chat", json={"session_id": "t", "text": "Bây giờ là mấy giờ?"})
        assert r.status_code == 200 and r.json()["action"]["type"] == "get_time"

        r = c.post("/chat", json={"session_id": "t", "text": "Đặt báo thức 7 giờ 30"})
        assert r.json()["action"] == {"type": "set_alarm", "params": {"time": "07:30"}}

        r = c.post("/chat", json={"session_id": "t", "text": "mở youtube"})
        assert r.json()["action"] == {"type": "open_app", "params": {"package": "com.google.android.youtube"}}

        r = c.post("/chat", json={"session_id": "t", "text": "mở banking"})
        assert r.json()["action"] is None  # package không nằm trong whitelist

        r = c.post("/chat", json={"session_id": "t", "text": "Xin chào"})
        assert r.json()["action"] is None and r.json()["response"]


def test_ws():
    with TestClient(app) as c, c.websocket_connect("/ws") as ws:
        ws.send_json({"session_id": "w", "text": "pin còn bao nhiêu"})
        assert ws.receive_json()["action"]["type"] == "get_battery"
