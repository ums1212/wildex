# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Don't Build

Don't Build

## Architecture

**단일 Activity + Jetpack Compose**, MVI 패턴, DI 프레임워크 없음(수동 주입).

### MVI Contract

각 feature는 3개의 파일로 구성된다 (`*Mvi.kt`):
```kotlin
data class FeatureUiState(...)
sealed interface FeatureIntent { ... }   // 사용자 액션
sealed interface FeatureUiEvent { ... }  // 1회성 이벤트(navigation, snackbar)
```
- ViewModel: `StateFlow<UiState>` + `Channel<UiEvent>(BUFFERED)`
- 인텐트 진입점: `fun onIntent(intent: FeatureIntent)`

### Navigation

```
WildexRootNav
 └─ SplashScreen → TitleScreen(로그인) → MainMenuScreen(탭 쉘)
                                             └─ WildexMainNav (중첩 NavHost)
                                                  ├─ Journal: BirdListScreen → BirdInfoScreen
                                                  ├─ Capture: CaptureScreen → CaptureResultScreen
                                                  ├─ Records: RecordsScreen → RecordDetailScreen
                                                  └─ Settings: SettingsScreen
```
- 타입 안전 라우트: `@Serializable` data object/class 사용
- Journal 탭은 SaveState 비활성화(탭 복귀 시 카테고리 화면으로 리셋)

### Data Layer

| 저장소 | 역할 |
|---|---|
| `BirdRepository` | 캐시 우선 전략(메모리 → DataStore → API) |
| `CaptureRecordRepository` | Room DAO 래퍼 (촬영 기록) |
| `ThemePreferencesRepository` | DataStore (다크모드, BGM 활성화 플래그) |

- **API 클라이언트:** Retrofit + OkHttp. `X-Wildex-Api-Key` 헤더 자동 주입
- **AI 인식:** `BirdImageAnalyzer` 인터페이스 → `GeminiBirdImageAnalyzer` 구현 (Firebase Gemini vision)
- **Room DB:** `capture_record` 테이블, `/schemas` 폴더에 버전 스키마 내보내기

### 의존성 주입

DI 프레임워크 없음. `Application` context를 ViewModel에서 직접 사용하여 Repository 생성. 싱글턴 패턴:
- `WildexDatabase.getInstance(context)`
- `WildexApiClient` (object)
- `SupabaseClient` (object)

### CompositionLocal Providers

`MainActivity.setContent` 에서 주입:
- `LocalThemePreferencesRepository`
- `LocalBgmManager`

---

## Design System

`DESIGN.md` 에 전체 스펙 있음. 핵심 규칙:

- **라이트 테마:** 카트리지/명세서 미학, 브랜드 레드 Primary
- **다크 테마("Night Mission"):**
  - 배경 `#121414`, CTA 네온 레드 `#FF544B`
  - **모든 모서리 0px (Sharp corners 필수)**
  - 구조적 보더 2px–4px (하드웨어 회로 느낌)
  - 타이포그래피: Space Grotesk
- `WildexShapes.kt`: `RoundedCornerShape(0.dp)` — 둥근 모서리 사용 금지

### 버튼 눌림 효과

클릭 가능한 **모든** 버튼·카드에 카트리지 눌림 효과를 적용해야 한다:
```kotlin
val interactionSource = remember { MutableInteractionSource() }
val isPressed by interactionSource.collectIsPressedAsState()
val shadowOffset = if (isPressed) 0.dp else 4.dp   // pressed → shadow 제거
val contentInset = if (isPressed) 4.dp else 0.dp    // pressed → 아래로 이동
```

---

## Key Docs

- `DESIGN.md` — 디자인 시스템 전체 스펙
- `doc/wildex_api_spec.md` — REST API 명세
- `doc/todo.md` — 예정 기능 목록
