# sort-mate 시스템 아키텍처

> Phase 1 기준 — 전체 시스템 아키텍처 다이어그램

---

## 1. 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph Client["📱 Client (React Native)"]
        Camera["📷 Camera<br/>YOLO TFLite 온디바이스"]
        App[" sort-mate App<br/>Zustand + React Navigation"]
    end

    subgraph K8s["☸️ Proxmox Kubernetes Cluster"]
        subgraph Ingress["🌐 Ingress"]
            Nginx["NGINX Ingress<br/>+ cert-manager (TLS)"]
        end

        subgraph API["🔧 API Server (JHipster)"]
            API1["api-server-0<br/>Spring Boot 3.x"]
            API2["api-server-1<br/>Spring Boot 3.x"]
            RateLimit["RateLimitFilter<br/>Bucket4j"]
            GrpcClient["gRPC Stub<br/>VLMInference"]
            Cache["Redis Cache<br/>TTL 24h"]
        end

        subgraph VLM["🤖 VLM Service"]
            VLM1["vlm-service<br/>FastAPI + gRPC"]
            vLLM["vLLM Runtime<br/>Qwen3-VL-4B"]
            HPA["HPA<br/>1~3 replicas"]
        end

        subgraph Data["💾 Data Layer"]
            PG["PostgreSQL 16<br/>StatefulSet + PVC 10Gi"]
            Redis["Redis 7<br/>Cache & Session"]
        end

        subgraph Monitor["📊 Observability"]
            Prom["Prometheus"]
            Grafana["Grafana"]
        end
    end

    subgraph External["🌐 External"]
        PublicAPI["공공데이터포털 API<br/>환경부 배출정보"]
    end

    Camera -->|"1차 분류<br/>(오프라인)"| App
    App -->|"POST /api/v1/classify/detail<br/>multipart/form-data"| Nginx
    Nginx --> API1 & API2
    API1 & API2 --> RateLimit
    RateLimit --> GrpcClient
    GrpcClient -->|"gRPC :50051<br/>AnalyzeWaste()"| VLM1
    VLM1 --> vLLM
    API1 & API2 <-->|"조회/저장"| PG
    API1 & API2 <-->|"캐시"| Redis
    API1 & API2 -.->|"OpenFeign<br/>Spring Batch (주간)"| PublicAPI
    Prom -.-> API1 & API2 & VLM1
    Grafana -.-> Prom
    HPA -.-> VLM1
```

---

## 2. 분류 요청 데이터 흐름

```mermaid
sequenceDiagram
    participant App as Mobile App
    participant YOLO as YOLO TFLite
    participant API as API Server
    participant Redis as Redis Cache
    participant VLM as VLM Service
    participant DB as PostgreSQL

    App->>YOLO: 카메라 프레임 입력
    YOLO-->>App: 1차 분류 (예: "플라스틱", 95%)
    Note over App: confidence ≥ 70% → 결과 표시

    App->>API: POST /api/v1/classify/detail<br/>(image + yoloResult + regionCode)
    API->>API: Rate Limit 체크 (Guest 30/일, User 100/일)
    API->>Redis: 캐시 조회 (classify:{hash})

    alt 캐시 HIT
        Redis-->>API: 캐시된 분석 결과
    else 캐시 MISS
        API->>VLM: gRPC AnalyzeWaste(image, yoloClass, region)
        VLM-->>API: AnalyzeResponse (분류, 배출법, 비용, 경고)
        API->>Redis: 결과 캐싱 (TTL 24h)
        API->>DB: 분류 기록 저장
    end

    API-->>App: 상세 분석 결과 JSON
```

---

## 3. K8s 네임스페이스 배포 구성

```mermaid
graph TB
    subgraph NS["Namespace: waste-helper"]
        subgraph Net["NetworkPolicy"]
            IngressCtrl["Ingress NGINX<br/>+ cert-manager"]
        end

        subgraph Apps["Applications"]
            APIDep["api-server Deployment<br/>replicas: 2"]
            APISvc["Service :8080"]
            VLMDep["vlm-service Deployment<br/>GPU: nvidia.com/gpu: 1"]
            VLMSvc["Service :50051 (gRPC)<br/>Service :8000 (http)"]
            HPA["HPA vlm-hpa<br/>min: 1, max: 3"]
        end

        subgraph Infra["Infrastructure"]
            PGSS["PostgreSQL StatefulSet<br/>PVC 10Gi"]
            PGSvc["Service :5432"]
            RedisDep["Redis Deployment"]
            RedisSvc["Service :6379"]
            Secret["Secret: postgres-secret<br/>(envsubst 관리)"]
        end

        subgraph Mon["Monitoring"]
            PromDep["Prometheus Deployment"]
            PromCfg["ConfigMap<br/>scrape targets"]
            GrafanaDep["Grafana Deployment"]
            GrafanaSvc["Service :3000"]
        end
    end

    IngressCtrl --> APISvc
    APISvc --> APIDep
    VLMSvc --> VLMDep
    HPA -.-> VLMDep
    APIDep --> PGSvc
    APIDep --> RedisSvc
    VLMDep -.->|"NetworkPolicy<br/>API Server만 접근 허용"| APIDep
    PGSvc --> PGSS
    RedisSvc --> RedisDep
    GrafanaDep -.-> PromDep
```

---

## 4. 인증 흐름

```mermaid
sequenceDiagram
    participant App as Mobile App
    participant API as API Server
    participant JWT as JWT Provider
    participant Redis as Redis Session
    participant OAuth as Social OAuth<br/>(Kakao/Naver/Google)

    rect rgb(240, 248, 255)
        Note over App,Redis: Guest Flow (인증 없이 분류 가능)
        App->>API: POST /api/v1/auth/guest<br/>{deviceId, fingerprint}
        API->>API: deviceId 검증 (UUID v4)
        API->>JWT: 익명 JWT 발급 {role: GUEST, exp: 7d}
        JWT-->>API: guest token
        API->>Redis: session:{token} TTL 7d
        API-->>App: Guest JWT
        App->>API: POST /api/v1/classify/detail<br/>Authorization: Bearer {guestJWT}
        API-->>App: 분류 결과 (정상)
    end

    rect rgb(255, 248, 240)
        Note over App,Redis: Social Login (히스토리/즐겨찾기 필요 시)
        App->>API: POST /api/v1/auth/social<br/>{provider, code, PKCE}
        API->>OAuth: Token + Profile 조회
        OAuth-->>API: 사용자 정보
        API->>JWT: 정식 JWT 발급 {role: USER, exp: 30d}
        API->>DB: 게스트 데이터 마이그레이션<br/>(waste_classifications, search_histories)
        API->>Redis: session:{token} TTL 30d
        API-->>App: User JWT
    end
```

---

## 5. 배출 요령 조회 우선순위

```mermaid
flowchart TD
    Req["분류 결과 + 지역 코드"] --> PG{PostgreSQL<br/>공공데이터 조회}

    PG -->|"HIT"| Return1["PUBLIC_API<br/>공공데이터 그대로 반환"]
    PG -->|"MISS"| Vec{Vector DB<br/>유사 검색 (threshold 0.85)}

    Vec -->|"HIT"| Return2["VECTOR_CACHE<br/>캐시된 LLM 응답 반환"]
    Vec -->|"MISS"| VLM["VLM 실시간 추론<br/>Qwen3-VL-4B"]

    VLM --> SaveVec["임베딩 → Vector DB 저장"]
    VLM --> Return3["VLM_REALTIME<br/>VLM 응답 반환"]
    VLM --> SavePG["공공데이터 누락 필드<br/>LLM 보완 → DB 저장"]

    style Return1 fill:#4CAF50,color:#fff
    style Return2 fill:#FF9800,color:#fff
    style Return3 fill:#F44336,color:#fff
```

---

## 6. Proxmox 클러스터 노드 구성

```mermaid
graph TB
    subgraph Proxmox["Proxmox Host"]
        subgraph Net["Network"]
            vmbr0["vmbr0 — 외부<br/>WAN: 공인 IP"]
            vmbr1["vmbr1 — 스토리지<br/>NFS (Longhorn)"]
            vmbr2["vmbr2 — 관리<br/>Proxmox GUI, SSH"]
        end

        subgraph Router["pfSense/OPNsense VM"]
            WAN["WAN"]
            VLAN10["VLAN 10: 10.10.0.0/24<br/>K8s Cluster"]
        end

        subgraph Nodes["K8s Nodes"]
            M1["master-1<br/>Control Plane<br/>4vCPU / 8GB / 100GB"]
            W1["worker-1<br/>API + DB<br/>8vCPU / 32GB / 500GB"]
            W2["worker-2<br/>VLM Service<br/>8vCPU / 32GB / 200GB<br/>RTX 3060 12GB"]
            W3["worker-3 (opt)<br/>API 복제 + Redis<br/>8vCPU / 16GB / 200GB"]
        end

        NFS["NFS Server<br/>(Longhorn Backend)"]
    end

    vmbr0 --> Router
    Router --> VLAN10
    VLAN10 --> M1 & W1 & W2 & W3
    vmbr1 --> NFS
    W2 ---|"GPU Passthrough"| GPU["🖥️ RTX 3060"]
```

---

## 7. GitOps 배포 흐름

```mermaid
graph LR
    Dev["Developer<br/>git push"] --> GitHub["GitHub Repo<br/>deuxksy/sort-mate"]

    subchart ArgoCD["ArgoCD"]
        Sync["Auto Sync"]
        Diff["Drift Detection"]
    end

    GitHub -->|"Webhook / Poll"| Sync
    Sync -->|"Apply Manifests"| K8s["K8s Cluster"]

    subchart Repo["/k8s Directory"]
        Manifests["/manifests/<br/>K8s raw YAML"]
        Helm["/helm/<br/>Helm Chart"]
        App["/argocd/<br/>Application CRD"]
    end

    GitHub --- Repo
    App --> Sync
    Diff -->|"Git Diff"| GitHub
```
