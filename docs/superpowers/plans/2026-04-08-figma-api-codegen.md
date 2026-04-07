# Figma API → Code Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Figma Page 단위 프레임 이미지를 추출하여 OpenUI API 경유 Vision LLM으로 React + NativeWind 컴포넌트 코드를 자동 생성하는 `figma-to-code.py` 스크립트 구현.

**Architecture:** Python 스크립트가 Figma REST API에서 Page 내 frame node 목록과 PNG 이미지를 순차 획득. 각 이미지를 base64 인코딩하여 OpenUI FastAPI(`/v1/chat/completions`)에 Vision LLM 요청 전송. SSE 스트리밍 응답에서 JSX 코드 블록을 추출해 `frontend/components/generated/`에 저장. `make figma-to-code`로 실행.

**Tech Stack:** Python 3.12, `requests`, Figma REST API, OpenUI FastAPI (OpenAI-compatible), SSE streaming

**Spec:** `docs/superpowers/specs/2026-04-08-figma-api-codegen-design.md`

---

## File Structure

| 파일 | 역할 | 상태 |
|------|------|------|
| `ui/scripts/figma-to-code.py` | 메인 스크립트 (CLI 진입점 + 전체 로직) | 신규 |
| `ui/tests/test_figma_to_code.py` | 단위 테스트 | 신규 |
| `frontend/components/generated/.gitkeep` | 자동 생성 디렉토리 보존 | 신규 |
| `Makefile` | `figma-to-code` 타겟 추가 | 수정 |
| `.env.example` | Figma/OpenUI 환경변수 추가 | 수정 |

---

### Task 1: 테스트 인프라 및 프레임명 변환 유틸리티

**Files:**
- Create: `ui/tests/test_figma_to_code.py`
- Create: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: 테스트 파일 생성 — to_pascal_case 테스트**

```python
# ui/tests/test_figma_to_code.py
"""figma-to-code.py 단위 테스트"""

import pytest
import sys
import os

# ui/scripts를 모듈 경로에 추가
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "scripts"))


def test_to_pascal_case_kebab():
    """kebab-case → PascalCase"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("home-header") == "HomeHeader"


def test_to_pascal_case_snake():
    """snake_case → PascalCase"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("waste_card_list") == "WasteCardList"


def test_to_pascal_case_spaces():
    """공백 → PascalCase"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("main screen") == "MainScreen"


def test_to_pascal_case_already_pascal():
    """이미 PascalCase인 경우"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("HomeHeader") == "HomeHeader"


def test_to_pascal_case_single_word():
    """단일 단어"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("button") == "Button"


def test_to_pascal_case_mixed_separators():
    """혼합 구분자 (kebab + space)"""
    from figma_to_code import to_pascal_case
    assert to_pascal_case("home-header item") == "HomeHeaderItem"
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: FAIL (`ModuleNotFoundError: No module named 'figma_to_code'`)

- [ ] **Step 3: 최소 구현 — to_pascal_case 함수**

```python
#!/usr/bin/env python3
"""Figma Page → React/NativeWind 컴포넌트 자동 생성 스크립트.

Usage:
    python3 ui/scripts/figma-to-code.py --page "Home"

Env:
    FIGMA_TOKEN      (필수) Figma Personal Access Token
    FIGMA_FILE_KEY   (필수) Figma 파일 키
    OPENUI_BASE      (선택) http://localhost:7878
    OPENUI_MODEL     (선택) glm-5.1
"""

import re


def to_pascal_case(name: str) -> str:
    """프레임명을 PascalCase로 변환.

    지원 구분자: 하이픈(-), 언더스코어(_), 공백( )
    예: home-header → HomeHeader, waste_card → WasteCard
    """
    words = re.split(r"[-_\s]+", name.strip())
    return "".join(w.capitalize() for w in words if w)
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 6 passed

- [ ] **Step 5: 커밋**

```bash
git add ui/tests/test_figma_to_code.py ui/scripts/figma-to-code.py
git commit -m "feat(ui): add figma-to-code script with to_pascal_case utility"
```

---

### Task 2: Figma API — 파일 구조 조회 및 Page 프레임 추출

**Files:**
- Modify: `ui/tests/test_figma_to_code.py`
- Modify: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: fetch_page_frames 테스트 추가**

```python
# ui/tests/test_figma_to_code.py 의 기존 import 아래에 추가

import json
from unittest.mock import patch, MagicMock


class TestFetchPageFrames:
    """Figma 파일 구조에서 Page 내 frame 노드 추출 테스트"""

    def _make_file_response(self, pages):
        """Figma GET /v1/files/{key} 응답 mock 생성"""
        return {
            "document": {
                "children": [
                    {
                        "name": page_name,
                        "type": "CANVAS",
                        "children": frames,
                    }
                    for page_name, frames in pages.items()
                ]
            }
        }

    def test_returns_frame_ids_and_names(self):
        """지정 Page의 frame node_id와 name 목록 반환"""
        from figma_to_code import fetch_page_frames

        response = self._make_file_response({
            "Home": [
                {"id": "1:2", "name": "home-header", "type": "FRAME"},
                {"id": "1:3", "name": "waste-card", "type": "FRAME"},
            ]
        })

        with patch("figma_to_code.requests.get") as mock_get:
            mock_get.return_value = MagicMock(
                status_code=200,
                json=lambda: response,
            )
            result = fetch_page_frames("dummy-token", "dummy-key", "Home")

        assert result == [
            {"id": "1:2", "name": "home-header"},
            {"id": "1:3", "name": "waste-card"},
        ]

    def test_skips_non_frame_nodes(self):
        """FRAME 타입이 아닌 노드는 제외"""
        from figma_to_code import fetch_page_frames

        response = self._make_file_response({
            "Home": [
                {"id": "1:2", "name": "header", "type": "FRAME"},
                {"id": "1:3", "name": "group1", "type": "GROUP"},
                {"id": "1:4", "name": "text1", "type": "TEXT"},
            ]
        })

        with patch("figma_to_code.requests.get") as mock_get:
            mock_get.return_value = MagicMock(
                status_code=200,
                json=lambda: response,
            )
            result = fetch_page_frames("dummy-token", "dummy-key", "Home")

        assert len(result) == 1
        assert result[0]["name"] == "header"

    def test_page_not_found_returns_empty(self):
        """지정 Page가 없으면 빈 목록 + stderr 경고"""
        from figma_to_code import fetch_page_frames

        response = self._make_file_response({"Other": []})

        with patch("figma_to_code.requests.get") as mock_get, \
             patch("sys.stderr"):
            mock_get.return_value = MagicMock(
                status_code=200,
                json=lambda: response,
            )
            result = fetch_page_frames("dummy-token", "dummy-key", "Missing")

        assert result == []

    def test_default_page_uses_first(self):
        """page_name=None이면 첫 번째 Page 사용"""
        from figma_to_code import fetch_page_frames

        response = self._make_file_response({
            "Home": [{"id": "1:1", "name": "hero", "type": "FRAME"}],
            "Detail": [],
        })

        with patch("figma_to_code.requests.get") as mock_get:
            mock_get.return_value = MagicMock(
                status_code=200,
                json=lambda: response,
            )
            result = fetch_page_frames("dummy-token", "dummy-key", None)

        assert result == [{"id": "1:1", "name": "hero"}]

    def test_api_403_raises_error(self):
        """403 응답 시 TOKEN 만료 에러 발생"""
        from figma_to_code import fetch_page_frames, FigmaAPIError

        with patch("figma_to_code.requests.get") as mock_get:
            mock_get.return_value = MagicMock(status_code=403)
            with pytest.raises(FigmaAPIError, match="TOKEN"):
                fetch_page_frames("bad-token", "key", "Home")

    def test_api_404_raises_error(self):
        """404 응답 시 FILE_KEY 에러 발생"""
        from figma_to_code import fetch_page_frames, FigmaAPIError

        with patch("figma_to_code.requests.get") as mock_get:
            mock_get.return_value = MagicMock(status_code=404)
            with pytest.raises(FigmaAPIError, match="FILE_KEY"):
                fetch_page_frames("token", "bad-key", "Home")
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py::TestFetchPageFrames -v`
Expected: FAIL (`ImportError: cannot import name 'fetch_page_frames'`)

- [ ] **Step 3: fetch_page_frames 구현**

`ui/scripts/figma-to-code.py` 기존 코드 아래에 추가:

```python
import re
import sys
import requests  # 기존 import re 아래에 추가


class FigmaAPIError(Exception):
    """Figma API 호출 오류"""


def fetch_page_frames(token: str, file_key: str, page_name: str | None) -> list[dict]:
    """Figma 파일에서 지정 Page의 FRAME 노드 목록 반환.

    Args:
        token: Figma Personal Access Token
        file_key: Figma 파일 키
        page_name: Page 이름 (None이면 첫 번째 Page)

    Returns:
        [{"id": "node_id", "name": "frame_name"}, ...]

    Raises:
        FigmaAPIError: API 호출 실패 시
    """
    url = f"https://api.figma.com/v1/files/{file_key}"
    headers = {"X-Figma-Token": token}

    resp = requests.get(url, headers=headers)

    if resp.status_code == 403:
        raise FigmaAPIError("Figma TOKEN이 만료되었거나 권한이 없습니다.")
    if resp.status_code == 404:
        raise FigmaAPIError("FIGMA_FILE_KEY를 확인하세요. 파일을 찾을 수 없습니다.")
    resp.raise_for_status()

    data = resp.json()
    pages = data.get("document", {}).get("children", [])

    if not pages:
        raise FigmaAPIError("Figma 파일에 Page가 없습니다.")

    # page_name이 None이면 첫 번째 Page 사용
    target_page = None
    for page in pages:
        if page_name is None or page.get("name") == page_name:
            target_page = page
            break

    if target_page is None:
        print(f"경고: Page '{page_name}'을(를) 찾을 수 없습니다.", file=sys.stderr)
        return []

    frames = [
        {"id": node["id"], "name": node["name"]}
        for node in target_page.get("children", [])
        if node.get("type") == "FRAME"
    ]

    if not frames:
        print(f"경고: Page '{target_page['name']}'에 FRAME이 없습니다.", file=sys.stderr)

    return frames
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 12 passed (기존 6 + 신규 6)

- [ ] **Step 5: 커밋**

```bash
git add ui/tests/test_figma_to_code.py ui/scripts/figma-to-code.py
git commit -m "feat(ui): add fetch_page_frames with Figma API integration"
```

---

### Task 3: Figma API — 프레임 PNG 이미지 export

**Files:**
- Modify: `ui/tests/test_figma_to_code.py`
- Modify: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: export_frame_images 테스트 추가**

```python
# ui/tests/test_figma_to_code.py 에 추가

class TestExportFrameImages:
    """Figma 프레임 PNG export 테스트"""

    def test_downloads_and_returns_base64(self):
        """프레임 PNG를 다운로드하여 base64 문자열 반환"""
        from figma_to_code import export_frame_images
        import base64

        # Figma images API 응답 mock
        images_response = {
            "images": {
                "1:2": "https://figma.com/image1.png",
                "1:3": "https://figma.com/image2.png",
            }
        }
        # PNG 다운로드 응답 mock (최소 8바이트 PNG 시그니처)
        fake_png = b"\x89PNG\r\n\x1a\n" + b"\x00" * 100

        with patch("figma_to_code.requests.get") as mock_get:
            def side_effect(url, **kwargs):
                if "/v1/images/" in url:
                    return MagicMock(status_code=200, json=lambda: images_response)
                # 이미지 다운로드 URL
                return MagicMock(status_code=200, content=fake_png)

            mock_get.side_effect = side_effect
            result = export_frame_images(
                "token", "key", [{"id": "1:2", "name": "a"}, {"id": "1:3", "name": "b"}]
            )

        assert len(result) == 2
        assert result[0]["id"] == "1:2"
        assert result[0]["name"] == "a"
        assert result[0]["image_b64"] == base64.b64encode(fake_png).decode()
        assert result[1]["id"] == "1:3"

    def test_missing_image_skipped(self):
    """이미지 URL이 없는 프레임은 스킵"""
        from figma_to_code import export_frame_images

        images_response = {"images": {"1:2": "https://figma.com/image1.png"}}

        with patch("figma_to_code.requests.get") as mock_get:
            def side_effect(url, **kwargs):
                if "/v1/images/" in url:
                    return MagicMock(status_code=200, json=lambda: images_response)
                return MagicMock(status_code=200, content=b"\x89PNG\r\n\x1a\n")

            mock_get.side_effect = side_effect
            result = export_frame_images(
                "token", "key",
                [{"id": "1:2", "name": "a"}, {"id": "1:9", "name": "missing"}]
            )

        assert len(result) == 1
        assert result[0]["id"] == "1:2"

    def test_empty_frames_returns_empty(self):
        """프레임 목록이 비어있으면 빈 목록 반환"""
        from figma_to_code import export_frame_images

        with patch("figma_to_code.requests.get"):
            result = export_frame_images("token", "key", [])

        assert result == []
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py::TestExportFrameImages -v`
Expected: FAIL

- [ ] **Step 3: export_frame_images 구현**

`ui/scripts/figma-to-code.py`에 추가:

```python
import base64  # 파일 상단 import 영역에 추가


def export_frame_images(
    token: str, file_key: str, frames: list[dict]
) -> list[dict]:
    """Figma 프레임을 PNG로 export하여 base64 인코딩.

    Args:
        token: Figma Personal Access Token
        file_key: Figma 파일 키
        frames: fetch_page_frames 반환값

    Returns:
        [{"id": ..., "name": ..., "image_b64": "..."}, ...]
    """
    if not frames:
        return []

    ids = ",".join(f["id"] for f in frames)
    url = f"https://api.figma.com/v1/images/{file_key}"
    headers = {"X-Figma-Token": token}
    params = {"ids": ids, "format": "png", "scale": "2"}

    resp = requests.get(url, headers=headers, params=params)
    if resp.status_code == 403:
        raise FigmaAPIError("Figma TOKEN이 만료되었거나 권한이 없습니다.")
    resp.raise_for_status()

    image_urls = resp.json().get("images", {})

    results = []
    for frame in frames:
        image_url = image_urls.get(frame["id"])
        if not image_url:
            print(f"경고: 프레임 '{frame['name']}' 이미지 export 실패, 스킵", file=sys.stderr)
            continue

        img_resp = requests.get(image_url)
        img_resp.raise_for_status()

        results.append({
            "id": frame["id"],
            "name": frame["name"],
            "image_b64": base64.b64encode(img_resp.content).decode(),
        })

    return results
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 15 passed (기존 12 + 신규 3)

- [ ] **Step 5: 커밋**

```bash
git add ui/tests/test_figma_to_code.py ui/scripts/figma-to-code.py
git commit -m "feat(ui): add export_frame_images for PNG export"
```

---

### Task 4: OpenUI API — 요청 빌드 및 SSE 스트리밍 파싱

**Files:**
- Modify: `ui/tests/test_figma_to_code.py`
- Modify: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: generate_component_code 테스트 추가**

```python
# ui/tests/test_figma_to_code.py 에 추가

class TestGenerateComponentCode:
    """OpenUI API 호출 및 JSX 추출 테스트"""

    def _make_sse_response(self, chunks: list[str]) -> MagicMock:
        """SSE 스트리밍 응답 mock 생성"""
        content_lines = []
        for chunk in chunks:
            import json as _json
            data = _json.dumps({
                "choices": [{"delta": {"content": chunk}}]
            })
            content_lines.append(f"data: {data}")
        content_lines.append("data: [DONE]")
        content_lines.append("")

        mock_resp = MagicMock()
        mock_resp.status_code = 200
        mock_resp.iter_lines.return_value = [
            line.encode() for line in content_lines
        ]
        mock_resp.__enter__ = MagicMock(return_value=mock_resp)
        mock_resp.__exit__ = MagicMock(return_value=False)
        return mock_resp

    def test_extracts_jsx_from_sse_stream(self):
        """SSE 스트리밍에서 JSX 코드 블록 추출"""
        from figma_to_code import generate_component_code

        sse_chunks = [
            "Here is the component:\n\n",
            "```jsx\n",
            'import { View, Text } from "react-native";\n',
            "\nexport default function Card() {\n",
            '  return <View><Text>Hello</Text></View>;\n',
            "}\n",
            "```\n",
        ]

        with patch("figma_to_code.requests.post") as mock_post:
            mock_post.return_value = self._make_sse_response(sse_chunks)
            result = generate_component_code(
                "http://localhost:7878", "glm-5.1", "dummy-b64", "TestCard"
            )

        assert 'import { View, Text } from "react-native"' in result
        assert "export default function Card()" in result
        assert "```" not in result

    def test_no_jsx_block_returns_none(self):
        """JSX 코드 블록이 없으면 None 반환"""
        from figma_to_code import generate_component_code

        sse_chunks = ["I cannot generate code from this image."]

        with patch("figma_to_code.requests.post") as mock_post:
            mock_post.return_value = self._make_sse_response(sse_chunks)
            result = generate_component_code(
                "http://localhost:7878", "glm-5.1", "dummy-b64", "TestCard"
            )

        assert result is None

    def test_openui_connection_error_returns_none(self):
        """OpenUI 연결 실패 시 None 반환 + stderr 경고"""
        from figma_to_code import generate_component_code

        with patch("figma_to_code.requests.post") as mock_post, \
             patch("sys.stderr"):
            mock_post.side_effect = requests.ConnectionError("refused")
            result = generate_component_code(
                "http://localhost:7878", "glm-5.1", "dummy-b64", "TestCard"
            )

        assert result is None

    def test_openui_timeout_returns_none(self):
        """OpenUI 타임아웃 시 None 반환"""
        from figma_to_code import generate_component_code

        with patch("figma_to_code.requests.post") as mock_post, \
             patch("sys.stderr"):
            mock_post.side_effect = requests.Timeout("timed out")
            result = generate_component_code(
                "http://localhost:7878", "glm-5.1", "dummy-b64", "TestCard"
            )

        assert result is None
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py::TestGenerateComponentCode -v`
Expected: FAIL

- [ ] **Step 3: generate_component_code 구현**

`ui/scripts/figma-to-code.py`에 추가:

```python
SYSTEM_PROMPT = """You are a React + NativeWind (Tailwind CSS for React Native) component generator.

Given a Figma frame screenshot, generate a single React component that:
1. Uses NativeWind (className prop) for styling — no inline styles
2. Is TypeScript-compatible (proper prop types via JSDoc)
3. Supports common variants (size, variant, disabled) where applicable
4. Uses only standard React Native components (View, Text, TouchableOpacity, Image, ScrollView)

Output ONLY the JSX code inside a single ```jsx code block. No explanation."""

OPENUI_TIMEOUT = 30


def generate_component_code(
    openui_base: str,
    model: str,
    image_b64: str,
    frame_name: str,
) -> str | None:
    """OpenUI API에 이미지를 전송하여 JSX 코드 생성.

    Args:
        openui_base: OpenUI 베이스 URL
        model: LLM 모델명
        image_b64: base64 인코딩 PNG 이미지
        frame_name: 프레임명 (프롬프트에 활용)

    Returns:
        JSX 코드 문자열, 실패 시 None
    """
    url = f"{openui_base}/v1/chat/completions"
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {
                "role": "user",
                "content": [
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/png;base64,{image_b64}"
                        },
                    },
                    {
                        "type": "text",
                        "text": (
                            f"Generate a React NativeWind component from this "
                            f"Figma frame named '{frame_name}'. "
                            f"Use the frame name as the component name."
                        ),
                    },
                ],
            },
        ],
        "stream": True,
    }

    try:
        resp = requests.post(
            url,
            json=payload,
            stream=True,
            timeout=OPENUI_TIMEOUT,
            headers={"Content-Type": "application/json"},
        )
        resp.raise_for_status()
    except (requests.ConnectionError, requests.Timeout) as e:
        print(
            f"경고: OpenUI 연결 실패 ({e}). "
            f"`make openui-up`으로 컨테이너를 시작하세요.",
            file=sys.stderr,
        )
        return None
    except requests.HTTPError as e:
        print(f"경고: OpenUI API 오류 ({e})", file=sys.stderr)
        return None

    # SSE 스트리밍 응답 수집
    full_text = ""
    for line in resp.iter_lines():
        if not line:
            continue
        line = line.decode("utf-8")
        if not line.startswith("data: "):
            continue
        data = line[6:]
        if data == "[DONE]":
            break
        try:
            import json
            chunk = json.loads(data)
            content = chunk.get("choices", [{}])[0].get("delta", {}).get("content", "")
            full_text += content
        except (json.JSONDecodeError, IndexError, KeyError):
            continue

    # JSX 코드 블록 추출
    match = re.search(r"```(?:jsx|tsx|javascript|typescript)\s*\n(.*?)```", full_text, re.DOTALL)
    if match:
        return match.group(1).strip()

    # 코드 블록 없음
    print(f"경고: '{frame_name}'에서 JSX 코드 블록을 찾을 수 없습니다", file=sys.stderr)
    return None
```

파일 상단에 `import json` 추가 (기존 `import re` 아래).

- [ ] **Step 4: 테스트 실행 — 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 19 passed (기존 15 + 신규 4)

- [ ] **Step 5: 커밋**

```bash
git add ui/tests/test_figma_to_code.py ui/scripts/figma-to-code.py
git commit -m "feat(ui): add generate_component_code with SSE streaming parser"
```

---

### Task 5: 파일 저장 및 원본 응답 백업

**Files:**
- Modify: `ui/tests/test_figma_to_code.py`
- Modify: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: save_component 테스트 추가**

```python
# ui/tests/test_figma_to_code.py 에 추가
import tempfile
import os


class TestSaveComponent:
    """컴포넌트 파일 저장 테스트"""

    def test_saves_jsx_file(self):
        """PascalCase.jsx 파일로 저장"""
        from figma_to_code import save_component

        with tempfile.TemporaryDirectory() as tmpdir:
            path = save_component(tmpdir, "HomeHeader", 'export default function HomeHeader() {}')
            assert path is not None
            assert os.path.exists(path)
            assert path.endswith("HomeHeader.jsx")
            with open(path) as f:
                assert "HomeHeader" in f.read()

    def test_skips_existing_file(self):
        """기존 파일 충돌 시 스킵"""
        from figma_to_code import save_component

        with tempfile.TemporaryDirectory() as tmpdir:
            # 먼저 파일 생성
            existing = os.path.join(tmpdir, "HomeHeader.jsx")
            with open(existing, "w") as f:
                f.write("old content")

            path = save_component(tmpdir, "HomeHeader", "new content")
            assert path is None

            # 기존 내용 유지 확인
            with open(existing) as f:
                assert f.read() == "old content"

    def test_saves_raw_on_parse_failure(self):
        """코드 파싱 실패 시 .raw.txt로 원본 저장"""
        from figma_to_code import save_raw_response

        with tempfile.TemporaryDirectory() as tmpdir:
            path = save_raw_response(tmpdir, "BrokenFrame", "raw text without jsx blocks")
            assert os.path.exists(path)
            assert path.endswith("BrokenFrame.raw.txt")
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py::TestSaveComponent -v`
Expected: FAIL

- [ ] **Step 3: save_component 및 save_raw_response 구현**

`ui/scripts/figma-to-code.py`에 추가:

```python
import os  # 파일 상단 import 영역에 추가


def save_component(output_dir: str, name: str, code: str) -> str | None:
    """생성된 JSX 코드를 파일로 저장.

    Args:
        output_dir: 저장 디렉토리
        name: PascalCase 컴포넌트명
        code: JSX 코드

    Returns:
        저장된 파일 경로, 충돌 시 None
    """
    filename = f"{name}.jsx"
    filepath = os.path.join(output_dir, filename)

    if os.path.exists(filepath):
        print(f"경고: '{filename}' 이미 존재, 스킵", file=sys.stderr)
        return None

    os.makedirs(output_dir, exist_ok=True)
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(code)

    print(f"  ✓ {filename}")
    return filepath


def save_raw_response(output_dir: str, name: str, raw_text: str) -> str:
    """파싱 실패한 원본 응답을 .raw.txt로 저장.

    Args:
        output_dir: 저장 디렉토리
        name: 프레임명
        raw_text: 원본 텍스트

    Returns:
        저장된 파일 경로
    """
    filepath = os.path.join(output_dir, f"{name}.raw.txt")
    os.makedirs(output_dir, exist_ok=True)
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(raw_text)

    print(f"  ⚠ {name}.raw.txt (코드 파싱 실패, 원본 저장)", file=sys.stderr)
    return filepath
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 22 passed (기존 19 + 신규 3)

- [ ] **Step 5: 커밋**

```bash
git add ui/tests/test_figma_to_code.py ui/scripts/figma-to-code.py
git commit -m "feat(ui): add save_component and save_raw_response"
```

---

### Task 6: CLI 진입점 및 메인 오케스트레이션

**Files:**
- Modify: `ui/scripts/figma-to-code.py`

- [ ] **Step 1: main() 함수 및 argparse 구현**

`ui/scripts/figma-to-code.py` 하단에 추가:

```python
import argparse  # 파일 상단 import 영역에 추가


def main():
    parser = argparse.ArgumentParser(
        description="Figma Page → React/NativeWind 컴포넌트 자동 생성"
    )
    parser.add_argument(
        "--page",
        default=None,
        help="대상 Figma Page 이름 (기본값: 첫 번째 Page)",
    )
    parser.add_argument(
        "--output",
        default=None,
        help="출력 디렉토리 (기본값: frontend/components/generated/)",
    )
    args = parser.parse_args()

    # 환경변수 로드
    token = os.environ.get("FIGMA_TOKEN")
    file_key = os.environ.get("FIGMA_FILE_KEY")
    openui_base = os.environ.get("OPENUI_BASE", "http://localhost:7878")
    model = os.environ.get("OPENUI_MODEL", "glm-5.1")

    if not token:
        print("ERROR: FIGMA_TOKEN 환경변수가 필요합니다.", file=sys.stderr)
        sys.exit(1)
    if not file_key:
        print("ERROR: FIGMA_FILE_KEY 환경변수가 필요합니다.", file=sys.stderr)
        sys.exit(1)

    # 출력 디렉토리 (프로젝트 루트 기준 상대경로)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", ".."))
    output_dir = args.output or os.path.join(
        project_root, "frontend", "components", "generated"
    )

    page_display = args.page or "(첫 번째 Page)"
    print(f"Figma → 코드 생성 시작")
    print(f"  Page: {page_display}")
    print(f"  Model: {model}")
    print(f"  Output: {output_dir}")
    print()

    # 1. Page 프레임 목록 조회
    try:
        frames = fetch_page_frames(token, file_key, args.page)
    except FigmaAPIError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)

    if not frames:
        print("처리할 프레임이 없습니다.")
        sys.exit(0)

    print(f"프레임 {len(frames)}개 발견:")
    for f in frames:
        print(f"  - {f['name']} ({f['id']})")
    print()

    # 2. 프레임 PNG export
    try:
        images = export_frame_images(token, file_key, frames)
    except FigmaAPIError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)

    if not images:
        print("ERROR: PNG export 실패", file=sys.stderr)
        sys.exit(1)

    print(f"PNG export 완료: {len(images)}개\n")

    # 3. 각 프레임 → LLM → 코드 생성 → 저장
    saved = 0
    skipped = 0
    failed = 0

    for img in images:
        component_name = to_pascal_case(img["name"])
        print(f"생성 중: {component_name} ...")

        code = generate_component_code(openui_base, model, img["image_b64"], img["name"])

        if code is None:
            # 원본 응답이 없으면 (연결 실패 등) 스킵
            failed += 1
            continue

        result = save_component(output_dir, component_name, code)
        if result:
            saved += 1
        else:
            skipped += 1

    # 결과 요약
    print(f"\n완료: {saved}개 생성, {skipped}개 스킵, {failed}개 실패")

    if failed > 0 and saved == 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: 스크립트 실행 테스트 — 도움말 확인**

Run: `cd /Users/crong/git/waste-helper && python3 ui/scripts/figma-to-code.py --help`
Expected: usage 메시지에 `--page`, `--output` 옵션 표시

- [ ] **Step 3: 스크립트 실행 테스트 — 환경변수 누락**

Run: `cd /Users/crong/git/waste-helper && python3 ui/scripts/figma-to-code.py 2>&1; echo "exit: $?"`
Expected: `ERROR: FIGMA_TOKEN 환경변수가 필요합니다.` + exit code 1

- [ ] **Step 4: 기존 테스트 전체 통과 확인**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 22 passed

- [ ] **Step 5: 커밋**

```bash
git add ui/scripts/figma-to-code.py
git commit -m "feat(ui): add CLI entry point with main orchestration"
```

---

### Task 7: Makefile 타겟 및 .env.example 업데이트

**Files:**
- Modify: `Makefile`
- Modify: `.env.example`
- Create: `frontend/components/generated/.gitkeep`

- [ ] **Step 1: Makefile에 figma-to-code 타겟 추가**

Makefile의 `UI Pipeline` 섹션(약 197행)에 타겟을 추가. 기존 `.PHONY` 라인과 `ui-pipeline` 타겟 사이에 삽입:

기존 `.PHONY` 라인 변경:
```makefile
.PHONY: token-sync token-build storybook storybook-build \
        appsmith-up appsmith-down openui-up openui-down \
        figma-to-code \
        ui-pipeline
```

`ui-pipeline` 타겟 앞에 추가:
```makefile
figma-to-code: ## Figma Page → React/RN 컴포넌트 자동 생성
	@test -n "$$FIGMA_TOKEN" || (echo "ERROR: FIGMA_TOKEN 환경변수가 필요합니다" && exit 1)
	python3 ui/scripts/figma-to-code.py --page "$(PAGE)"
```

- [ ] **Step 2: .env.example에 Figma/OpenUI 환경변수 추가**

`.env.example` 파일 말미에 추가:

```
# --- Figma API ---
FIGMA_TOKEN=your-figma-personal-access-token
FIGMA_FILE_KEY=your-figma-file-key

# --- OpenUI ---
OPENUI_BASE=http://localhost:7878
OPENUI_MODEL=glm-5.1
```

- [ ] **Step 3: generated 디렉토리 .gitkeep 생성**

```bash
mkdir -p /Users/crong/git/waste-helper/frontend/components/generated
touch /Users/crong/git/waste-helper/frontend/components/generated/.gitkeep
```

- [ ] **Step 4: make help로 타겟 표시 확인**

Run: `cd /Users/crong/git/waste-helper && make help | grep figma-to-code`
Expected: `figma-to-code        Figma Page → React/RN 컴포넌트 자동 생성`

- [ ] **Step 5: 커밋**

```bash
git add Makefile .env.example frontend/components/generated/.gitkeep
git commit -m "feat(ui): add figma-to-code Makefile target and env config"
```

---

### Task 8: 통합 테스트 (Mocked E2E)

**Files:**
- Modify: `ui/tests/test_figma_to_code.py`

- [ ] **Step 1: 통합 테스트 추가**

```python
# ui/tests/test_figma_to_code.py 에 추가

class TestIntegrationE2E:
    """전체 파이프라인 Mock 통합 테스트"""

    def test_full_pipeline_with_mocks(self, tmp_path):
        """Figma → OpenUI → 파일 저장 전체 흐름 (mocked)"""
        import base64
        from figma_to_code import (
            fetch_page_frames,
            export_frame_images,
            generate_component_code,
            to_pascal_case,
            save_component,
        )

        fake_png = b"\x89PNG\r\n\x1a\n" + b"\x00" * 100

        # 1. 프레임 조회
        file_resp = {
            "document": {
                "children": [{
                    "name": "Home",
                    "type": "CANVAS",
                    "children": [
                        {"id": "1:2", "name": "hero-banner", "type": "FRAME"},
                        {"id": "1:3", "name": "waste-list", "type": "FRAME"},
                    ],
                }]
            }
        }

        # 2. 이미지 export
        images_resp = {
            "images": {
                "1:2": "https://figma.example.com/hero.png",
                "1:3": "https://figma.example.com/list.png",
            }
        }

        # 3. OpenUI SSE 응답
        jsx_code = 'import { View, Text } from "react-native";\nexport default function HeroBanner() { return <View />; }'

        def mock_get(url, **kwargs):
            if "/v1/files/" in url:
                return MagicMock(status_code=200, json=lambda: file_resp)
            if "/v1/images/" in url:
                return MagicMock(status_code=200, json=lambda: images_resp)
            return MagicMock(status_code=200, content=fake_png)

        sse_chunks = [
            f"```jsx\n{jsx_code}\n```",
        ]

        def make_sse():
            import json as _json
            lines = []
            for chunk in sse_chunks:
                data = _json.dumps({"choices": [{"delta": {"content": chunk}}]})
                lines.append(f"data: {data}".encode())
            lines.append(b"data: [DONE]")
            lines.append(b"")
            mock = MagicMock()
            mock.status_code = 200
            mock.iter_lines.return_value = lines
            mock.__enter__ = MagicMock(return_value=mock)
            mock.__exit__ = MagicMock(return_value=False)
            return mock

        output_dir = str(tmp_path / "generated")
        saved_files = []

        with patch("figma_to_code.requests.get", side_effect=mock_get), \
             patch("figma_to_code.requests.post", return_value=make_sse()):

            frames = fetch_page_frames("token", "key", "Home")
            assert len(frames) == 2

            images = export_frame_images("token", "key", frames)
            assert len(images) == 2

            for img in images:
                name = to_pascal_case(img["name"])
                code = generate_component_code(
                    "http://localhost:7878", "glm-5.1", img["image_b64"], img["name"]
                )
                assert code is not None
                path = save_component(output_dir, name, code)
                assert path is not None
                saved_files.append(path)

        assert len(saved_files) == 2
        assert os.path.exists(os.path.join(output_dir, "HeroBanner.jsx"))
        assert os.path.exists(os.path.join(output_dir, "WasteList.jsx"))
```

- [ ] **Step 2: 전체 테스트 실행**

Run: `cd /Users/crong/git/waste-helper && python3 -m pytest ui/tests/test_figma_to_code.py -v`
Expected: 23 passed (기존 22 + 신규 1)

- [ ] **Step 3: 커밋**

```bash
git add ui/tests/test_figma_to_code.py
git commit -m "test(ui): add E2E integration test for figma-to-code pipeline"
```

---

## Self-Review

### 1. Spec Coverage

| Spec 요구사항 | Task | 비고 |
|------|------|------|
| Figma REST API 파일 구조 조회 | Task 2 | `fetch_page_frames` |
| 프레임 PNG export (scale=2) | Task 3 | `export_frame_images` |
| OpenUI `/v1/chat/completions` 호출 | Task 4 | `generate_component_code` |
| SSE 스트리밍 파싱 | Task 4 | `iter_lines` + JSON 파싱 |
| Vision LLM 요청 (base64 이미지) | Task 4 | `image_url` content type |
| JSX 코드 블록 추출 | Task 4 | regex ````jsx...```` 추출 |
| PascalCase 네이밍 | Task 1 | `to_pascal_case` |
| 파일 충돌 시 스킵 | Task 5 | `save_component` |
| 코드 파싱 실패 → .raw.txt 저장 | Task 5 | `save_raw_response` |
| CLI --page 인자 | Task 6 | argparse |
| 환경변수 (FIGMA_TOKEN, FILE_KEY, OPENUI_BASE, OPENUI_MODEL) | Task 6 | `os.environ.get` |
| Makefile 타겟 | Task 7 | `figma-to-code` |
| 시스템 프롬프트 (NativeWind) | Task 4 | `SYSTEM_PROMPT` 상수 |
| 에러 처리 (403, 404, 연결실패, 타임아웃) | Task 2, 4 | `FigmaAPIError`, None 반환 |
| 결과 요약 출력 | Task 6 | `main()` 말미 |
| frontend/components/generated/ 디렉토리 | Task 7 | `.gitkeep` |
| .env.example 업데이트 | Task 7 | Figma/OpenUI 항목 |

### 2. Placeholder Scan

- TBD, TODO, "implement later" 등 없음 — 모든 코드가 완전히 작성됨

### 3. Type Consistency

- `fetch_page_frames` → `list[dict]` with `{"id": str, "name": str}` → `export_frame_images` 동일 key 입력
- `export_frame_images` → `list[dict]` with `{"id", "name", "image_b64"}` → `generate_component_code`에서 `image_b64` 사용
- `to_pascal_case` → `str` → `save_component`에서 `name`으로 사용
- 모든 함수 시그니처 일관성 확인 완료
