# VLM Service — waste-helper

Qwen3-VL-4B 모델 기반 폐기물 이미지 분류 gRPC 마이크로서비스.

## 기술 스택

| 항목 | 기술 |
|------|------|
| Language | Python 3.11+ |
| Framework | FastAPI + gRPC |
| AI Model | Qwen3-VL-4B (Hugging Face Transformers) |
| Runtime | PyTorch + Accelerate |
| 패키지 관리 | uv (hatchling) |

## 구조

```
vlm-service/
├── app/                  # 애플리케이션
│   ├── main.py           # FastAPI + gRPC 서버 진입점
│   ├── grpc_server.py    # gRPC 서비스 구현
│   ├── model.py          # VLM 모델 로딩 & 추론
│   └── config.py         # 설정 (pydantic-settings)
├── proto/
│   └── vlm_service.proto # gRPC 서비스 정의
├── tests/                # pytest 테스트
├── Dockerfile            # GPU 포함 프로덕션 이미지
├── pyproject.toml        # 의존성 & 빌드 설정
└── requirements.txt      # pip 호환 의존성
```

## gRPC 인터페이스

`mock-vlm/README.md`의 Proto 정의와 동일.

## 실행

```bash
# 로컬 (GPU 필요)
uv sync
python -m app.main

# Docker (GPU)
docker build -t waste-helper/vlm-service:latest .
docker run --gpus all -p 50051:50051 -p 8000:8000 waste-helper/vlm-service:latest
```

> **참고**: GPU가 없는 환경에서는 `mock-vlm/` 서비스를 대신 사용.
