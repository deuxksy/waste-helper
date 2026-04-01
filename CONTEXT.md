1 Figma
- Figma Variables 설정 (Primary Color, Semantic Color, Spacing, Radius 등)
- Style Dictionary 등을 활용해 Variables를 `tokensjson`으로 추출
- 추출된 JSON을 Tailwind Config 및 Appsmith 테마에 자동 주입하는 스크립트 작성

2 OpenUI
- Figma 시안을 OpenUI 에 입력
- React 와 React Native 컴포넌트 분리 생성
- 생성된 코드를 프로젝트 구조에 배치

3 Storybook
- Storybook 환경 구축 및 단계별 컴포넌트 등록
- Figma 디자인과 실제 구현 코드 검수
- 다양한 상태(Loading, Error, Empty)에 대한 시각적 테스트 케이스 작성

4 개발
- 4-1 API (JHipster):
  - JDL을 엔티티 설계
  - Swagger API 명세 확정 및 DTO, Service Layer 구현
- 4-2 관리자 (Appsmith):
  - PostgreSQL DB 연결
  - 수거 신청 목록 조회, AI 분석 결과 승인, 통계 대시보드 위젯 배치
- 4-3 프론트엔드 (RactieNative):
  - Storybook 컴포넌트 조립 및 페이지 라우팅 구현
  - JHipster API 연동 및 상태 관리 설정

5 인프라
- Proxmox 에서 GPU Passthrough 설정
- K8s 에 GPU 자원 할당 및 vLLM 배포
- Qwen-VL 로드 및 API 엔드포인트

6 최적화
- 모바일 위한 양자화 및 실시간 탐지 속도 최적화
- Qwen 프롬프트 엔지니어링을 통해 분석 결과의 JSON 출력 정확도 향상
- YOLO→Qwen 간의 데이터 전달 지연 시간 단축

7 배포,모니터링
- 통합 테스트, CI/CD
- 하이웍스 알림 장애 발생, API 오류, AI 추론 실패 시 알림
- 데이터 기반의 AI 피드백 루프(재학습 데이터 수집) 구축