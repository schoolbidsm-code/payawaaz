"""PayAwaaz backend: event log + status API."""
from flask import Flask, request, jsonify
import time

app = Flask(__name__)
EVENTS = []          # newest first, capped
CAP = 300

def add_event(e):
    e["ts"] = time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime())
    EVENTS.insert(0, e)
    del EVENTS[CAP:]

@app.route("/ping")
def ping():
    return jsonify(ok=True, service="payawaaz-backend", time=int(time.time()))

@app.route("/log", methods=["POST"])
def log():
    d = request.get_json(force=True, silent=True) or {}
    add_event({"type": d.get("type", "event"), "pkg": d.get("pkg", ""),
               "title": d.get("title", ""), "text": (d.get("text", "") or "")[:300],
               "note": d.get("note", "")})
    return jsonify(ok=True)

@app.route("/events")
def events():
    return jsonify(count=len(EVENTS), events=EVENTS)

@app.route("/view")
def view():
    rows = "".join(
        f"<tr><td>{e['ts']}</td><td>{e['type']}</td><td>{e['pkg']}</td>"
        f"<td>{e['title']}</td><td>{e['text']}</td></tr>"
        for e in EVENTS[:100])
    return ("<html><head><title>PayAwaaz events</title></head><body>"
            "<h2>PayAwaaz live events (latest 100)</h2>"
            "<table border=1 cellpadding=6><tr><th>time UTC</th><th>type</th>"
            "<th>package</th><th>title</th><th>text</th></tr>" + rows +
            "</table></body></html>")

@app.route("/")
def home():
    return jsonify(service="payawaaz-backend", endpoints=["/ping", "/log", "/events", "/view"])

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(__import__("os").environ.get("PORT", 8000)))
