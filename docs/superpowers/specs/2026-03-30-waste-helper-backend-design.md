# 내 손안의 AI 폐기물 처리 도우미 — Backend 설계 문서

> 작성일: 2026-03-30
> 상태: Draft → Approved

## 1. 프로젝트 개요

**목적**: 일반 시민이 스마트폰 카메라로 폐기물을 촬영하면, AI가 실시간 분류하고 지역별 분리배출 요령·비용·수거일정을 안내하는 모바일 앱 + 백엔드 시스템.

**핵심 가치**:
- 온디바이스 YOLO로 실시간 1차 분류 (오프라인 동작 보장)
- VLM 기반 상세 분석 (배출 요령, 지역 규정, 비용 안내)
- 공공데이터 + LLM 하이브리드로 정확한 지역별 정보 제공

## 2. 시스템 아키텍처

```
┌─────────────┐    WiFi/5G    ┌──────────────────┐    gRPC/REST    ┌─────────────────┐
│  React Native │ ──────────▶ │  JHipster API     │ ────────────▶ │  VLM Service      │
│  Mobile App   │            │  Server (Spring)   │              │  (Qwen3-VL-4B)   │
│  - YOLO TFLite │◀──────────│  - REST API        │◀─────────────│  - vLLM runtime   │
└─────────────┘  result     └──────────────────┘  detail info   └─────────────────┘
      │                          │          ▲                                  ▲
      │ classification           │          │                                  │
      │ result (tiny payload)    │          │                                  │
      │                          ▼          │                                  │
                        ┌──────────────┐  │  ┌──────────────┐
                        │  PostgreSQL   │──┘  │  Vector DB    │
                        │  (영구 저장)   │     │  (Milvus)     │
                        └──────────────┘     └──────────────┘
                               ▲
                        ┌──────┴──────┐
                        │   Redis      │
                        │  (캐시/세션) │
                        └─────────────┘
```

### 핵심 원칙

- **Edge-First**: YOLO 온디바이스 1차 분류 → 네트워크 트래픽 최소화
- **VLM은 독립 마이크로서비스**: JHipster API 서버에서 gRPC로 호출, 장애 격리
- **게스트 우선**: 인증 없이 분류 가능, 히스토리/즐겨찾기만 로그인 필요

## 3. 모바일 앱 (React Native)

### 기술 스택

- React Native + TypeScript
- YOLO → TFLite 변환 → 온디바이스 추론
- 상태관리: Zustand
- 네비게이션: React Navigation
- 타겟: iOS + Android

### 화면 구성

| 화면 | 설명 | 인증 |
|------|------|------|
| 카메라 | 실시간 YOLO 감지 오버레이 + 분류 결과 표시 | 게스트 |
| 상세 결과 | VLM 상세 분석 (배출 요령, 비용, 지역 정보) | 게스트 |
| 검색 히스토리 | 최근 분류 기록 목록 | 로그인 |
| 지역 설정 | 시/군/구 선택 → 공공데이터 API 연동 | 로그인 |
| 즐겨찾기 | 자주 보는 폐기물 분류 북마크 | 로그인 |
| 설정 | 알림, 지역, 계정 관리 | 로그인 |

### 데이터 흐름

```
카메라 프레임 → YOLO TFLite (로컬)
    → 1차 분류 결과 (예: "플라스틱", 신뢰도 95%)
    → [사용자 탭] 상세 보기 요청
    → JHipster API 호출 (분류 결과 + 크롭 이미지 전송)
    → VLM 상세 분석 응답
```

- **오프라인 동작**: YOLO 1차 분류는 네트워크 없이도 동작
- **캐시 전략**: 동일 분류 결과는 Redis 캐시에서 즉시 반환 (TTL 24h)

## 4. JHipster API 서버

### 기술 스택

- JHipster 8 (Spring Boot 3.x + JDL 기반 엔티티 생성)
- Java 21 + Gradle
- Spring Security + JWT (게스트는 익명 토큰)
- Spring Cloud OpenFeign (공공데이터 API 호출)

### 도메인 모델 (JDL)

```jdl
entity User {
  username(String)
  email(String)
  imageUrl(String)
  langKey(String)
}

entity WasteClassification {
  detectedClass(String) required
  confidence(Float) required
  imageUrl(String)
  detailResult(TextJson)
  classifiedAt(Instant) required
}

entity Region {
  sido(String)
  sigungu(String)
  emdName(String)
  emdCode(String)
}

entity DisposalGuide {
  wasteType(String) required
  disposalMethod(TextJson) required
  region(Region)
  source(String) // "PUBLIC_API" | "LLM_GENERATED" | "LLM_SUPPLEMENTED"
}

entity SearchHistory {
  query(String)
  resultSummary(TextJson)
  classifiedAt(Instant) required
  user(User)
}

entity FavoriteRegion {
  user(User)
  region(Region) required
}

entity NotificationSetting {
  user(User)
  enabled(Boolean)
  fcmToken(String)
}

entity WasteImage {
  originalUrl(String)
  thumbnailUrl(String)
  classification(WasteClassification)
}

entity Feedback {
  classification(WasteClassification)
  isAccurate(Boolean)
  correctedClass(String)
  comment(String)
  user(User)
}

relationship ManyToOne { WasteClassification(user) }
relationship ManyToOne { WasteImage(classification) }
relationship ManyToOne { SearchHistory(user) }
relationship OneToMany { Region(disposalGuide) }
relationship ManyToOne { FavoriteRegion(region) }
```

### API 엔드포인트

| Method | Path | 설명 | Auth |
|--------|------|------|------|
| POST | /api/v1/classify/detail | VLM 상세 분석 요청 | Guest |
| GET | /api/v1/disposal-guide | 폐기물별 배출 요령 조회 | Guest |
| GET | /api/v1/regions | 지역 목록 | Guest |
| GET | /api/v1/regions/{code}/schedule | 수거 일정 | Guest |
| POST | /api/v1/search-histories | 검색 기록 저장 | User |
| GET | /api/v1/search-histories | 내 검색 기록 | User |
| POST | /api/v1/feedbacks | 분류 피드백 | User |
| POST | /api/v1/auth/guest | 게스트 토큰 발급 | Public |
| POST | /api/v1/auth/social | 소셜 로그인 (Kakao/Naver/Google) | Public |

### VLM 서비스 연동

- Feign Client → VLM Service (gRPC)
- 응답 캐싱: 동일 분류+지역 조합은 Redis TTL 24h 캐시
- Vector DB: VLM 응답 임베딩 저장 → 유사 질문 시 캐시 hit

## 5. VLM 서비스 (Qwen3-VL-4B)

### 기술 스택

- Python FastAPI + gRPC 서버
- Qwen3-VL-4B + vLLM 런타임
- K8s Deployment (GPU 파드)

### K8s 배포

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: vlm-service
  namespace: waste-helper
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: vlm-service
        image: vlm-service:latest
        resources:
          requests:
            nvidia.com/gpu: 1
          limits:
            nvidia.com/gpu: 1
        env:
        - name: MODEL_NAME
          value: "Qwen/Qwen3-VL-4B"
        - name: VLLM_MAX_MODEL_LEN
          value: "4096"
        livenessProbe:
          httpGet:
            path: /health
            port: 8000
---
apiVersion: v1
kind: Service
metadata:
  name: vlm-service
  namespace: waste-helper
spec:
  ports:
  - port: 8000
    name: http
  - port: 50051
    name: grpc
  selector:
    app: vlm-service
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: vlm-hpa
  namespace: waste-helper
spec:
  minReplicas: 1
  maxReplicas: 3
  metrics:
  - type: Resource
    resource:
      name: nvidia.com/gpu
      target:
        type: Utilization
        averageUtilization: 70
```

### VLM 프롬프트 파이프라인

```
입력: 이미지 + YOLO 1차 분류 결과
  ↓
시스템 프롬프트 구성:
  - 역할: 한국 폐기물 분리배출 전문가
  - 컨텍스트: 사용자 지역({region}) 기준 배출 규정
  - 태스크: 분류 확인 + 배출 요령 + 비용 안내
  ↓
VLM 추론 (Qwen3-VL-4B)
  ↓
출력: JSON (분류, 배출법, 주의사항, 비용, 지역 특이사항)
```

### 모델 확장 경로

| 단계 | 모델 | GPU 요구 | 시점 |
|------|------|---------|------|
| MVP | Qwen3-VL-4B | 1x GPU (8GB VRAM) | 초기 런칭 |
| v2 | Qwen3-VL-8B | 1x GPU (16GB VRAM) | 사용자 피드백 기반 정확도 개선 |
| v3 | Qwen3-VL-30B | 2x GPU (A100 40GB) | 대규모 서비스 |

## 6. 인프라 (Proxmox K8s)

### 클러스터 구성 (3~5노드)

| 노드 | 역할 | vCPU | RAM | Storage | GPU |
|------|------|------|-----|---------|-----|
| master-1 | Control Plane | 4 | 8GB | 100GB SSD | - |
| worker-1 | API + DB | 8 | 32GB | 500GB SSD | - |
| worker-2 | VLM Service | 8 | 32GB | 200GB SSD | 1x RTX 3060 (12GB) |
| worker-3 *(옵션)* | API 복제 + Redis | 8 | 16GB | 200GB SSD | - |
| worker-4 *(옵션)* | VLM 복제 | 8 | 32GB | 200GB SSD | 1x GPU |

### 네트워크 (Proxmox SDN)

```
Proxmox Host
  ├── vmbr0 (외부 네트워크)
  │     └── pfSense/OPNsense VM (라우터 + 방화벽)
  │           ├── WAN: 공인 IP
  │           └── VLAN 10: 10.10.0.0/24 (K8s 클러스터)
  ├── vmbr1 (내부 스토리지 네트워크)
  │     └── NFS Server (Longhorn 백엔드)
  └── vmbr2 (관리 네트워크)
        └── Proxmox GUI, SSH 점프 호스트
```

### K8s 네임스페이스

```
waste-helper/
  ├── api-server        (JHipster, 2 replicas)
  ├── vlm-service        (Qwen3-VL-4B, 1~3 replicas + HPA)
  ├── postgres           (PostgreSQL 16, PVC 50Gi)
  ├── redis              (Redis 7, Sentinel 모드)
  ├── milvus             (Vector DB, etcd + MinIO 백엔드)
  ├── ingress-nginx      (Ingress Controller + Let's Encrypt)
  └── monitoring         (Prometheus + Grafana + Loki)
```

### GitOps (ArgoCD)

```
GitHub repo
  ├── /k8s/manifests/     # K8s 매니페스트
  ├── /k8s/helm/          # Helm 차트
  └── /k8s/argocd/        # ArgoCD Application 정의
       └── ArgoCD가 자동 감지 → 클러스터에 배포
```

### 백업 전략

- PostgreSQL: pg_dump 일일 + S3 호환 스토리지(MinIO) 업로드
- Vector DB: Milvus 백업 도구 주간 스냅샷
- K8s etcd: 자동 스냅샷 5분 간격

## 7. 데이터 레이어

### PostgreSQL — 영구 저장

```sql
users                    -- 사용자 계정 (게스트 익명 포함)
regions                  -- 시/도/군/구 코드 + 공공데이터 메타
disposal_guides          -- 폐기물 배출 요령 (공공데이터 + LLM 보완)
waste_classifications    -- 분류 기록 (1차 YOLO + 2차 VLM 결과)
search_histories         -- 검색 히스토리
favorite_regions         -- 즐겨찾는 지역
notification_settings    -- 알림 설정 (수거일, 비용 변동 등)
feedbacks                -- 분류 정확도 피드백 (모델 개선용)
```

### Redis — 캐시 + 세션

```
session:{token}          → 사용자 정보 (TTL: 7d)
classify:{hash}          → VLM 상세 분석 결과 (TTL: 24h)
guide:{region}:{type}    → 배출 요령 JSON (TTL: 7d)
ratelimit:{ip}           → 분당 요청 수 (분류 30/min, 상세 10/min)
```

### Milvus — Vector DB

```
Collection: waste_embeddings
  - id: UUID
  - embedding: float[1024]    -- VLM 출력 임베딩
  - metadata: {
      waste_type, confidence, region,
      vlm_response_summary, created_at
    }

용도:
  1. 유사 폐기물 분류 검색 → 캐시 히트율 향상
  2. 사용자 피드백 기반 임베딩 클러스터링 → 모델 개선 데이터
```

### 데이터 소스 연동

```
공공데이터포털 API
  ├── 환경부_생활폐기물배출정보  → disposal_guides
  ├── 지자체_분리배출기준       → disposal_guides (지역별)
  └── 대형폐기물수거신청       → disposal_guides (비용)

연동 방식:
  Spring Batch 스케줄러 → 주간 동기화
  → 신규/변경 데이터 감지
  → DisposalGuide upsert
  → source = "PUBLIC_API"
  → LLM으로 누락 필드 보완 시 source = "LLM_SUPPLEMENTED"
```

## 8. 보안

### 인증 흐름

```
앱 실행 → 게스트 토큰 발급 (POST /api/v1/auth/guest)
       → 익명 JWT (role: GUEST, deviceId 기반)
       → 분류 API 즉시 사용 가능
            │
            ▼ [사용자가 "히스토리 저장" 등 선택 시]
       소셜 로그인 (POST /api/v1/auth/social)
       → Kakao/Naver/Google OAuth2
       → 정식 JWT (role: USER, 기존 게스트 데이터 마이그레이션)
```

### JWT 구성

```
게스트 토큰: { sub: deviceId, role: GUEST, exp: 7d }
유저 토큰:  { sub: userId, role: USER, exp: 30d, refresh: 90d }
```

### 보안 조치

| 영역 | 조치 |
|------|------|
| API Gateway | Spring Cloud Gateway → Rate Limiting, IP 블랙리스트 |
| 이미지 업로드 | 최대 10MB, HEIC/AVIF 차단, EXIF 메타데이터 제거 |
| VLM 요청 | 게스트 30회/일, 유저 100회/일 제한 |
| 데이터 보호 | 이미지 분석 후 서버 임시 파일 즉시 삭제 (TTL 1h) |
| 네트워크 | K8s NetworkPolicy — VLM 파드는 API 서버에서만 접근 가능 |
| 시크릿 관리 | Vault/K8s Secrets → DB 비밀번호, OAuth client secret |
| 컨테이너 | Trivy 이미지 스캔, non-root 컨테이너 실행 |
| 감사(Audit) | 분류 API 호출 로그 → Loki 적재, 90일 보관 |

## 9. 결정사항 요약

| 항목 | 결정사항 |
|------|---------|
| 프로젝트명 | 내 손안의 AI 폐기물 처리 도우미 |
| 타겟 사용자 | 일반 시민, 분리배출 헬퍼 |
| 모바일 | React Native (iOS + Android), YOLO TFLite 온디바이스 |
| API 서버 | JHipster 8 (Spring Boot 3.x), Java 21 |
| VLM | Qwen3-VL-4B, vLLM, K8s 독립 마이크로서비스 (gRPC) |
| 인증 | 게스트 우선 + 선택적 소셜 로그인 (Kakao/Naver/Google) |
| DB | PostgreSQL + Redis + Milvus (Vector DB) |
| 데이터 | 공공데이터포털 API + LLM 하이브리드 보완 |
| 인프라 | Proxmox K8s 3~5노드, ArgoCD GitOps |
| 확장 | 4B→8B→30B, 1→3 replica, HPA 오토스케일링 |
