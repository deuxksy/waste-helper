# waste-helper Frontend Design Spec

> Phase 1 — Expo Router + NativeWind 기반 Camera-first 미니멀 UI

---

## 1. 개요

### 목표

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
│   ├── useClassify.ts          # 분류 상태관리 (YOLO → API)
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
│   └── test-images/            # YOLO 테스트용
│       ├── pet_bottle.jpg
│       ├── can.jpg
│       └── food_waste.jpg
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

### Request Flow

```
Camera 캡영
    ↓
[온디바이스] YOLO TFLite 추론
    ├─ confidence ≥ 70% → 클래스명 표시
    └─ confidence < 70% → "인식 불가" 안내
    ↓
[API Server] POST /api/v1/classify/detail
    ↓ multipart/form-data
    {
      image: JPEG (압축, max 1MB)
      yoloClass: "플라스틱" | null
      yoloConfidence: 0.95
      regionCode: "11000"
    }
    ↓
[Response] 200 OK
    {
      "category": "RECYCLABLE",
      "wasteType": "PET 플라스틱 병",
      "confidence": 0.92,
      "disposalMethod": {
        "steps": ["...", "...", "..."],
        "warnings": ["뚜껑 별도 분리"],
        "cost": null
      },
      "source": "VLM_REALTIME"
    }
```

### TypeScript Types

```typescript
// types/classify.ts

export interface ClassifyRequest {
  image: Blob;
  yoloClass?: string;
  yoloConfidence?: number;
  regionCode?: string;
}

export interface ClassifyResponse {
  category: WasteCategory;
  wasteType: string;
  confidence: number;
  disposalMethod: DisposalMethod;
  source: ResultSource;
}

export type WasteCategory =
  | 'RECYCLABLE'
  | 'FOOD_WASTE'
  | 'GENERAL'
  | 'HAZARDOUS'
  | 'OVERSIZED';

export type ResultSource =
  | 'PUBLIC_API'
  | 'VECTOR_CACHE'
  | 'VLM_REALTIME';

export interface DisposalMethod {
  steps: string[];
  warnings: string[];
  cost?: string;
}

export interface YOLOResult {
  className: string;
  confidence: number;
  boundingBox: [number, number, number, number];
}
```

---

## 5. Web vs Mobile 기능 차이

| 기능 | Mobile | Web |
|---|---|---|
| 카메라 | expo-camera (Native) | WebRTC getUserMedia |
| YOLO TFLite | tflite-react-native | 서버 API로 폴백 |
| 푸시 알림 (Phase 2) | expo-notifications | Web Push API |
| 네비게이션 | Stack + Tab | 동일 (Expo Router Web) |

---

## 6. 에러 처리

| 시나리오 | 처리 |
|---|---|
| 네트워크 오류 | "오프라인 모드" 안내, YOLO 결과만 표시 |
| API 타임아웃 (10s) | 재시도 1회 후 "서버 혼잡" 안내 |
| YOLO 미인식 | "다시 촬영" 안내, VLM 직접 요청 |
| 카메라 권한 거부 | 설정 이동 CTA |
| TFLite 모델 로드 실패 | 서버 전용 모드로 폴백 |

---

## 7. 핵심 Dependencies

| Package | Version | 용도 |
|---|---|---|
| expo | ~53 | SDK core |
| expo-router | ~4 | File-based routing |
| expo-camera | ~16 | 카메라 제어 |
| nativewind | ~4 | Tailwind for RN |
| tailwindcss | ~3 | NativeWind peer |
| tflite-react-native | - | 온디바이스 YOLO 추론 |
| expo-image-manipulator | ~13 | 이미지 리사이즈/압축 |
| expo-constants | ~17 | 환경변수 관리 |

---

## 8. 테스트 전략

| 레벨 | 도구 | 대상 |
|---|---|---|
| Unit | Vitest + React Testing Library | hooks, services, utils |
| Component | Vitest + RNTL | UI 컴포넌트 |
| Integration | MSW (API mocking) | API client + flow |
| E2E | Detox (mobile) / Playwright (web) | 전체 화면 flow |
| YOLO | 테스트 이미지 셋 (50장) | 분류 정확도 ≥ 85% |
