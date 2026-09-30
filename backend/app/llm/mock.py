import json
import re

APP_PACKAGES = {
    "youtube": "com.google.android.youtube",
    "chrome": "com.android.chrome",
    "cài đặt": "com.android.settings",
    "spotify": "com.spotify.music",
}


class MockProvider:
    """Giả lập model bằng luật đơn giản: để test API khi chưa cài Ollama."""

    async def generate(self, messages: list[dict]) -> str:
        text = messages[-1]["content"].lower()
        if "mấy giờ" in text or "giờ rồi" in text:
            out = {"response": "", "action": {"type": "get_time", "params": {}}}
        elif "pin" in text:
            out = {"response": "", "action": {"type": "get_battery", "params": {}}}
        elif "báo thức" in text:
            m = re.search(r"(\d{1,2})\s*(?:giờ|h|:)\s*(\d{0,2})", text)
            hh, mm = (int(m.group(1)), int(m.group(2) or 0)) if m else (7, 0)
            out = {
                "response": f"Mình đặt báo thức {hh:02d}:{mm:02d} nhé.",
                "action": {"type": "set_alarm", "params": {"time": f"{hh:02d}:{mm:02d}"}},
            }
        elif text.startswith("mở "):
            name = text[3:].strip()
            package = APP_PACKAGES.get(name, f"unknown.{name}")
            out = {"response": f"Mình mở {name} nhé.", "action": {"type": "open_app", "params": {"package": package}}}
        else:
            out = {"response": "Mình nghe rồi, bạn nói thêm đi.", "action": None}
        return json.dumps(out, ensure_ascii=False)
