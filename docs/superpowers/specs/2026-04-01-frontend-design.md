# waste-helper Frontend Design Spec

> Phase 1 — Expo Router + NativeWind 기반 Camera-first 미니멀 UI

---

## 1. 개요

аксим### 목표

폐기물 촬영 → 온디바이스 YOLO 1차 분류 → API Server VLM 상세 분석 → 배출 방법 안내까지 한 화면 flow로 제공.

### 범위

| 항목 | Phase 1 포함 | Phase 2+ |
|---|---|---|
| 카메라 촬영 + 분류 | ✅ | - |
| 온디바이스 YOLO TFLite | ✅ | - |
| VLM 상세 분석 | ✅ | - |
| 배출 방법 가이드 | ✅ | - |
| 인증 (Guest + Social) | - | ✅ |
| 히스토리 / 통계 | - | ✅ |
| 배출 정보 조회 | - | ✅ |
| 푸시 알림 | - | ✅ |

### 기술 결정

| 항목 | 결정 | 이유 |
|---|---|---|
| Platform | React Native Web + Mobile (단일 코드베이스) | 코드 공유, 빠른 개발 |
| Framework | Expo SDK 53 + Expo Router v4 | File-based routing, OTA, EAS Build |
| Styling | NativeWind v4 (Tailwind) | Web/RN 공통, 빠른 프로토타이핑 |
| State | React Context + useState | Phase 1 단순 flow, 과도한 추상화 방지 |
| Camera | expo-camera | Expo 생태계 통합 |
| 온디바이스 AI | tflite-react-native | YOLO TFLite 추론 |
| API 통신 | fetch (REST multipart/form-data) | gRPC-Web RN 미지원, REST가 안정적 |
| Design 톤 | Eco Green (#22c55e) | 환경 친화적, 깔끔한 인상 |

---

## 2. 프로젝트 구조

```
frontend/
├── app/                        # Expo Router (file-based routing)
│   ├── _layout.tsx             # Root layout (Provider wrapping)
│   ├── index.tsx               # 홈 → 카메라 화면
│   ├── classify/
│   │   ├── _layout.tsx         # 분류 flow layout
│   │   └── result.tsx          # 분류 결과 상세
│   └── +not-found.tsx          # 404
│
├── components/
│   ├── camera/
│   │   ├── CameraView.tsx      # expo-camera 래퍼
│   │   └── CaptureButton.tsx   # 촬영 버튼
│   ├── classify/
│   │   ├── ResultCard.tsx      # 분류 결과 카드
│   │   ├── DisposalGuide.tsx   # 배출 방법 안내
│   │   └── ConfidenceBadge.tsx # 신뢰도 표시
│   └── ui/
│       ├── Button.tsx
│       ├── LoadingOverlay.tsx
│       └── ErrorMessage.tsx
│
├── services/
│   ├── api.ts                  # API Server REST client
│   ├── yolo.ts                 # TFLite inference wrapper
│   └── image.ts                # 이미지 전처리 (resize, compress)
│
├── hooks/
│   ├── useCamera.ts            # 카메라 권한 + 제어
│   ├── useClassify.ts            # 분류 상태관리 (YOLO → API)
│   └── usePermission.ts        # 권한 체크
│
├── constants/
│   ├── theme.ts                # Eco Green 색상/타이포
│   └── config.ts               # API URL, 모델 경로 등
│
├── types/
│   └── classify.ts             # API 응답 타입
│
├── assets/
│   └── models/
│       └── waste_v4.tflite     # YOLO TFLite 모델
│
├── __tests__/
│   ├── hooks/
│   │   └── useClassify.test.ts
│   ├── services/
│   │   ├── api.test.ts
│   │   └── yolo.test.ts
│   └── components/
│       └── ResultCard.test.tsx
│
├── __fixtures__/
│   └── test-images/            # YOLO 테스트용 (.gitignore 등록 권장)
│
├── app.json
├── tsconfig.json
├── vitest.config.ts
└── package.json
```

---

## 3. UI 화면 설계

### Design System

```
색상:
  Primary:    #22c55e (Green 500)
  Primary-D:  #16a34a (Green 600)
  Primary-DD: #166534 (Green 800)
  Background: #f0faf0 (Eco light)
  Surface:    #ffffff
  Warning:    #f59e0b (Amber 500)
  Error:      #ef4444 (Red 500)
  Text:       #1a1a1a
  Text-Sub:   #6b7280 (Gray 500)

타이포그래피:
  Title:    24px / Bold / Green-800
  Subtitle: 16px / SemiBold / Green-700
  Body:     14px / Regular / Gray-800
  Caption:  12px / Regular / Gray-500
```

### 화면 Flow

```
[홈 - 카메라] → 촬영 → [1차 YOLO 결과 + VLM 로딩] → [상세 결과] → 다시 촬영
```

### Screen 1: 홈 (카메라)

- 전체 화면 카메라 뷰파인더
- 중앙 점선 프레임 (폐기물 위치 가이드)
- 상단: 앱 로고 + Guest 뱃지
- 하단: 촬영 버튼 (원형, Green)
- YOLO 실시간 오버레이 (좌측 상단)

### Screen 2: 1차 분류 (YOLO)

- 촬영 이미지 미리보기
- YOLO 결과 카드: 분류명 + 신뢰도(%)
- VLM 요청 로딩 스피너

### Screen 3: 상세 결과 (VLM)

- 폐기물 분류명 + 카테고리 뱃지
- 배출 방법 Step-by-step 가이드
- 주의사항 (Amber 경고 박스)
- "다시 촬영하기" CTA

---

## 4. API 연동

> Backend API 스펙 기준: `ClassificationResource.java`, `ClassifyDetailResponse.java`

### Request

```
POST /api/v1/classify/detail  Content-Type: multipart/form-data

  image: JPEG (압축, max 1MB)            // MultipartFile (required)
  detectedClass: "플라스틱"              // String (required, @NotBlank)
  confidence: 0.95                      // Float (required, @NotNull, 0.0~1.0)
  regionCode: "11000"                  // String (optional)
```

### Response

```
200 OK — ClassifyDetailResponse

{
  "detectedClass": "플라스틱",
  "confirmedClass": "PET 플라스틱 병",
  "confidence": 0.92,
  "disposalMethod": {
    "method": "분리수거",
    "notes": ["내용물 비우기", "라벨 제거"],
    "items": [
      { "label": "본체", "action": "플라스틱 분리수거함" },
      { "label": "뚜껑", "action": "일반 쓰레기" }
    ]
  },
  "costInfo": {
    "type": "FREE",
    "amount": 0,
    "currency": "KRW",
    "collectionSchedule": "월/수/금 오전 6~9시",
    "notes": "지자체별 상이"
  },
  "warnings": ["뚜껑 별도 분리"],
  "regionSpecific": "서울시 기준",
  "source": "VLM_REALTIME",
  "cached": false
}
```

### TypeScript Types

> Backend Java Record와 1:11 매핑. 필드명/타입은 서버 스펙 기준.

```typescript
// types/classify.ts

// --- Request ---

export interface ClassifyRequest {
  image: Blob;
  detectedClass: string;        // @NotBlank, YOLO 분류명
  confidence: number;           // 0.0 ~ 1.0
  regionCode?: string;          // optional
}

// --- Response ---

export interface ClassifyDetailResponse {
  detectedClass: string;
  confirmedClass: string;
  confidence: number;
  disposalMethod: DisposalMethodResponse;
  costInfo: CostInfoResponse;
  warnings: string[];
  regionSpecific: string;
  source: string;               // "PUBLIC_API" | "VECTOR_CACHE" | "VLM_REALTIME"
  cached: boolean;
}

export interface DisposalMethodResponse {
  method: string;               // "분리수거", "음식물 폐기물", "일반 쓰레기" 등
  notes: string[];              // 배출 시 주의사항
  items: DisposalItemResponse[]; // 부품별 배출 방법
}

export interface DisposalItemResponse {
  label: string;                // "본체", "뚜껑", "라벨" 등
  action: string;               // 해당 부품의 배출 방법
}

export interface CostInfoResponse {
  type: string;                 // "FREE", "PAID", "SUBSCRIPTION" 등
  amount: number;
  currency: string;             // "KRW"
  collectionSchedule: string;   // "월/수/금 오전 6~9시"
  notes: string;
}

// --- YOLO (온디바이스) ---

export interface YOLOResult {
  className: string;
  confidence: number;
  boundingBox?: [number, number, number, number];
}
```

---

## 5. Web vs Mobile 기능 차이

| 기능 | Mobile | Web |
|---|---|---|
| 카메라 | expo-camera (Native) | WebRTC getUserMedia |
| YOLO TFLite | tflite-react-native | 서버 API로 폴백 (이미지 전체 전송) |
| 푸시 알림 (Phase 2) | expo-notifications | Web Push API |
| 네비게이션 | Stack + Tab | 동일 (Expo Router Web) |

### Web 폴백 전략

Web 환경에서 TFLite를 사용할 수 없으믢 다음과 같이 폴백:

```
Web: camera(getUserMedia) → 이미지 캡처 → POST /api/v1/classify/detail
  (detectedClass=null, confidence=0 으로 전체 분석을 서버에 의존)
Mobile: camera → YOLO TFLite 1차 → API 서버 상세 분석
```

---

## 6. 에러 처리

| 시나리오 | 처리 |
|---|---|
| 네트워크 오류 | "오프라인 모드" 안내, YOLO 결과만 표시 (Mobile only) |
| API 타임아웃 (10s) | 재시도 1회 후 "서버 혼잡" 안내 |
| YOLO 미인식 (confidence < 70%) | "다시 촬영" 안내, VLM 직접 요청 (detectedClass=null) confidence=0) |
| 카메라 권한 거부 | 설정 이동 CTA (`Linking.openSettings()` on Expo) |
| TFLite 모델 로드 실패 | 서버 전용 모드로 폴백 (detectedClass=null) confidence=0 전송) |
| API 400/500 | "일시 오류" 안내, 재시도 없함 |

---

## 7. 핵심 Dependencies

| Package | Version | 용도 | 비고 |
|---|---|---|---|
| expo | ~53 | SDK core | - |
| expo-router | ~4 | File-based routing | - |
| expo-camera | ~16 | 카메라 제어 | - |
| nativewind | ~4 | Tailwind for RN | Expo 53 호환성 검증 필요 |
| tailwindcss | ~3 | NativeWind peer | - |
| tflite-react-native | latest | 온디바이스 YOLO 추론 | Expo New Arch 호환성 검증 필요, |
| expo-image-manipulator | ~13 | 이미지 리사이즈/압축 | - |
| expo-constants | ~17 | 환경변수 관리 | - |

> **호환성 검증 필수**: `tflite-react-native`과 `NativeWind v4`의 Expo SDK 53 / New Architecture 지원 여부는
> 초기 설정 시 호환성 테스트가 필요. 미지원 시 대안 패키지 검토 필요.

---

## 8. 테스트 전략

| 레벨 | 도구 | 대상 |
|---|---|---|
| Unit | Vitest + React Testing Library | hooks, services, utils |
| Component | Vitest + RNTL | UI 컴포넌트 |
| Integration | MSW (API mocking) | API client + flow |
| YOLO | 테스트 이미지 셋 (50장, CI 외부 관리) | 분류 정확도 ≥ 85% |

> Phase 1에서는 Unit/Component 테스트에 집중. E2E(Detox/Playwright)은 Phase 2+에서 도입.
