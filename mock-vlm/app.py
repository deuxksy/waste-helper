"""Mock VLM gRPC Service — returns realistic waste classification results."""

import json
import os
from concurrent import futures

import grpc
from flask import Flask, jsonify
from google.protobuf.json_format import MessageToDict
import vlm_service_pb2
import vlm_service_pb2_grpc

MOCK_MODE = os.getenv("MOCK_MODE", "true").lower() == "true"

# 폐기물 분류 mock 데이터
WASTE_DATA = {
    "plastic": {
        "waste_type": "플라스틱",
        "method": "재활용 분리배출",
        "notes": ["라벨 제거 후 배출", "내용물 비우고 헹구기"],
        "items": [
            {"label": "본체", "action": "재활용"},
            {"label": "라벨", "action": "제거 후 일반 쓰레기"},
            {"label": "뚜껑", "action": "재활용 (재질 확인)"},
        ],
        "cost_info": "무료 (재활용품)",
        "warnings": "열 수 있는 뚜껑과 본체가 다른 재질인 경우 분리 배출",
    },
    "glass": {
        "waste_type": "유리",
        "method": "재활용 분리배출",
        "notes": ["깨진 유리는 신문지로 감싸 배출"],
        "items": [
            {"label": "유리병", "action": "재활용"},
            {"label": "뚜껑", "action": "금속/플라스틱별 분리"},
        ],
        "cost_info": "무료 (재활용품)",
        "warnings": "도자기/내열유리는 일반 쓰레기",
    },
    "paper": {
        "waste_type": "종이",
        "method": "재활용 분리배출",
        "notes": ["비닐 코팅된 종이는 일반 쓰레기", "스테이플 제거"],
        "items": [
            {"label": "종이", "action": "재활용"},
        ],
        "cost_info": "무료 (재활용품)",
        "warnings": "음식물이 묻은 종이는 일반 쓰레기",
    },
    "metal": {
        "waste_type": "금속캔",
        "method": "재활용 분리배출",
        "notes": ["내용물 비우고 헹구기", "찌그러뜨려 배출"],
        "items": [
            {"label": "캔", "action": "재활용"},
        ],
        "cost_info": "무료 (재활용품)",
        "warnings": "에어로졸 캔은 완전히 비운 후 배출",
    },
    "food_waste": {
        "waste_type": "음식물 쓰레기",
        "method": "음식물 쓰레기 전용 수거",
        "notes": ["물기 제거 후 배출", "전용 봉투 사용"],
        "items": [],
        "cost_info": "종량제 봉투 비용 (지자체별 상이)",
        "warnings": "조개껍데기, 과일껍질(귤, 오렌지)은 일반 쓰레기",
    },
    "general": {
        "waste_type": "일반 쓰레기",
        "method": "종량제 봉투 배출",
        "notes": ["지자체 지정 봉투 사용"],
        "items": [],
        "cost_info": "종량제 봉투 비용",
        "warnings": "대형 폐기물은 별도 신고 후 배출",
    },
}


class MockVLMService(vlm_service_pb2_grpc.VLMInferenceServicer):
    def AnalyzeWaste(self, request, context):
        yolo_class = request.yolo_class or "general"
        data = WASTE_DATA.get(yolo_class, WASTE_DATA["general"])

        items = [
            vlm_service_pb2.DisposalItem(label=i["label"], action=i["action"])
            for i in data["items"]
        ]

        disposal = vlm_service_pb2.DisposalMethod(
            method=data["method"],
            notes=data["notes"],
            items=items,
        )

        return vlm_service_pb2.AnalyzeResponse(
            waste_type=data["waste_type"],
            disposal_method=disposal,
            cost_info=data["cost_info"],
            warnings=data["warnings"],
            confidence=max(request.confidence, 0.85),
        )


def serve_grpc():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=4))
    vlm_service_pb2_grpc.add_VLMInferenceServicer_to_server(
        MockVLMService(), server
    )
    server.add_insecure_port("0.0.0.0:50051")
    server.start()
    print("gRPC server started on port 50051")
    server.wait_for_termination()


# Flask 앱 (health check용)
flask_app = Flask(__name__)


@flask_app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


@flask_app.route("/", methods=["GET"])
def index():
    return jsonify({"service": "mock-vlm", "status": "running"})


if __name__ == "__main__":
    import threading

    # gRPC 서버를 별도 스레드에서 실행
    grpc_thread = threading.Thread(target=serve_grpc, daemon=True)
    grpc_thread.start()

    # Flask는 HTTP health check용
    flask_app.run(host="0.0.0.0", port=8000)
