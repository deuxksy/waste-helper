# K8s Manifests — waste-helper

Kubernetes 배포 매니페스트.

## 구조

```
k8s/
├── manifests/            # K8s raw YAML
├── helm/                 # Helm Chart
└── argocd/               # ArgoCD Application CRD
```

## Namespace

모든 리소스는 `waste-helper` namespace에 배포.

## 배포 구성 요소

| Component | Manifest | replicas | Port |
|-----------|----------|----------|------|
| Frontend | `frontend-deployment.yaml` | 2 | 80 (nginx) |
| API Server | `api-server-deployment.yaml` | 1 | 8080 (Spring Boot) |
| VLM Service | `vlm-service-deployment.yaml` | 1 | 50051 (gRPC) + 8000 (HTTP) |
| PostgreSQL | `postgres-statefulset.yaml` | 1 (StatefulSet) | 5432 |
| Redis | `redis-deployment.yaml` | 1 | 6379 |

## 배포

```bash
# 전체 배포
make deploy

# 개별 배포
make deploy-api
make deploy-frontend
make deploy-vlm

# 전체 삭제
make teardown
```

## 외부 접근 (로컬 개발)

```bash
# Port-forward (0.0.0.0 바인딩으로 외부 기기 접근 가능)
kubectl port-forward -n waste-helper svc/frontend 33080:80 --address 0.0.0.0
kubectl port-forward -n waste-helper svc/api-server 32067:8080 --address 0.0.0.0
```

- Frontend: `http://<machine-ip>:33080`
- API Server: `http://<machine-ip>:32067`
