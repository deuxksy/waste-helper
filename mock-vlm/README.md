# Mock VLM Service — waste-helper

VLM 모델(Qwen3-VL-4B) 없이 gRPC 응답을 테스트하기 위한 Mock 서비스.

## 기술 스택

| 항목 | 기술 |
|------|------|
| Language | Python 3.12 |
| gRPC | grpcio + grpcio-tools |
| Health | Flask (HTTP :8000) |
| gRPC | :50051 |

## 구조

```
mock-vlm/
├── app.py               # Flask + gRPC 서버
├── vlm_service.proto    # Proto 정의 (빌드 시 stub 생성)
└── Dockerfile           # Python 이미지 + proto 컴파일
```

## 지원 폐기물 분류

| yolo_class | waste_type |
|------------|------------|
| plastic | 플라스틱 |
| glass | 유리 |
| paper | 종이 |
| metal | 금속캔 |
| food_waste | 음식물 쓰레기 |
| general | 일반 쓰레기 (기본값) |

## 포트

| Port | Protocol | 용도 |
|------|----------|------|
| 50051 | gRPC | `AnalyzeWaste()` RPC |
| 8000 | HTTP | `/health` 엔드포인트 |

## gRPC 인터페이스

```protobuf
service VLMInference {
  rpc AnalyzeWaste(AnalyzeRequest) returns (AnalyzeResponse);
}

message AnalyzeRequest {
  bytes image_data = 1;
  string yolo_class = 2;
  float confidence = 3;
  string region_code = 4;
}

message AnalyzeResponse {
  string waste_type = 1;
  DisposalMethod disposal_method = 2;
  string cost_info = 3;
  string warnings = 4;
  float confidence = 5;
}
```

## 실행

```bash
# Docker
docker build -t waste-helper/vlm-service:latest .
docker run -p 50051:50051 -p 8000:8000 waste-helper/vlm-service:latest

# K8s
make deploy-vlm
```

## Mock 응답 예시

`yolo_class=plastic` 요청 시:

```json
{
  "waste_type": "플라스틱",
  "disposal_method": {
    "method": "재활용 분리배출",
    "notes": ["라벨 제거 후 배출", "내용물 비우고 헹구기"],
    "items": [
      {"label": "본체", "action": "재활용"},
      {"label": "라벨", "action": "제거 후 일반 쓰레기"}
    ]
  },
  "confidence": 0.9
}
```
