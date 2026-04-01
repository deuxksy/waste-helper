"""Mock VLM Service for local development without GPU."""

import threading
from flask import Flask, jsonify

app = Flask(__name__)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


@app.route("/predict", methods=["POST"])
def predict():
    return jsonify({
        "result": "mock_vlm_result",
        "confidence": 0.95,
        "class": "recyclable",
    })


@app.route("/", methods=["GET"])
def index():
    return jsonify({"service": "mock-vlm", "status": "running"})


def start_grpc_placeholder():
    """Placeholder: listen on gRPC port 50051 without actual gRPC server."""
    import socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(("0.0.0.0", 50051))
    sock.listen(1)
    while True:
        conn, _ = sock.accept()
        conn.close()


if __name__ == "__main__":
    grpc_thread = threading.Thread(target=start_grpc_placeholder, daemon=True)
    grpc_thread.start()
    app.run(host="0.0.0.0", port=8000)
