# Wildex 조류도감 API 명세서

본 문서는 안드로이드 앱 클라이언트에서 Wildex 서비스의 조류도감 데이터를 가져오기 위한 REST API 연동 규격을 안내합니다. 

해당 API는 공공데이터포털(산림청 국립수목원)의 원본 XML 응답을 모바일 환경에 적합한 JSON 형태로 파싱하여 제공합니다.

---

## 공통 사항 (보안 인증)

모든 API 호출 시, 유효하지 않은 사용자의 무분별한 호출을 방지하기 위해 반드시 HTTP 요청 헤더에 아래의 커스텀 보안 키를 포함해야 합니다.

- **Header Name**: `X-Wildex-Api-Key`
- **Header Value**: `(사전에 공유된 모바일 앱용 인증 키)`
  - *예시: `asdf...`*

인증이 누락되거나 틀린 경우 HTTP Status `403 Forbidden` 에러를 반환합니다.

API 서버 주소
'https://comon.dev'
---

## 1. 조류도감 목록 검색 API

조류도감의 기본 목록을 페이지네이션(Pagination) 형태로 검색하여 가져옵니다.

- **Endpoint**: `GET /api/wildex/bird-list/`
- **Authentication Required**: Yes

### 요청 파라미터 (Query Parameters)

| 필드명 | 타입 | 필수 여부 | 기본값 | 설명 |
|---|---|---|---|---|
| `numOfRows` | Number | 선택 | 10 | 한 페이지에 출력할 결과 수 |
| `pageNo` | Number | 선택 | 1 | 조회할 페이지 번호 |

**요청 예시:**
```http
GET /api/wildex/bird-list/?numOfRows=10&pageNo=1
X-Wildex-Api-Key: your_secure_mobile_key
```

### 성공 응답 예시 (HTTP 200)

```json
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL SERVICE."
    },
    "body": {
      "items": {
        "item": [
          {
            "anmlGnrlNm": "가마우지",
            "anmlScnm": "Phalacrocorax capillatus (Temminck & Schlegel, 1850)",
            "anmlSpecsId": "A000001305",
            "clsscSstemSctinKrlngNm": "가마우지과",
            "cprtCtnt": "본 데이터에 대한 모든 저작권리는 국립수목원 또는 원저작자에게 있습니다."
          },
          ...
        ]
      },
      "numOfRows": "10",
      "pageNo": "1",
      "totalCount": "240"
    }
  }
}
```

---

## 2. 조류도감 상세정보 조회 API

목록에서 얻은 특정 조류의 종 고유 식별자(`anmlSpecsId`)를 이용해 상세 정보(생태 특징, 이미지 URL 등)를 조회합니다.

- **Endpoint**: `GET /api/wildex/bird-info/`
- **Authentication Required**: Yes

### 요청 파라미터 (Query Parameters)

| 필드명 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| `q1` | String | **필수** | 종별 고유 식별 ID (`anmlSpecsId` 값, 예: `A000001305`) |

**요청 예시:**
```http
GET /api/wildex/bird-info/?q1=A000001305
X-Wildex-Api-Key: your_secure_mobile_key
```

### 성공 응답 예시 (HTTP 200)

```json
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL SERVICE."
    },
    "body": {
      "item": {
        "anmlClsKorNm": "조강",
        "anmlFmlyKorNm": "나무발발이과",
        "anmlGnrlNm": "나무발발이",
        "anmlOrdKorNm": "참새목",
        "anmlScnm": "Certhia familiaris Linnaeus, 1758",
        "anmlSpecsId": "A000001148",
        "eclgDpftrCont": "아고산대의 침엽수림에서 서식한다...",
        "gnrlSpftrCont": "겨울 깃은 암컷과 수컷 모두...",
        "imgUrl": "http://www.nature.go.kr/fileUpload/animals/basic/KNAM-BI-0000555_001.jpg",
        "cprtCtnt": "본 데이터에 대한 모든 저작권리는 국립수목원 또는 원저작자에게 있습니다."
      }
    }
  }
}
```

### 에러 응답 예시

**필수 파라미터 누락 (HTTP 400)**
```json
{
  "error": "Missing parameter 'q1' (anmlSpecsId)"
}
```

**잘못된 보안 키 혹은 헤더 누락 (HTTP 403)**
```json
{
  "detail": "Invalid or missing X-Wildex-Api-Key header."
}
```

**공공데이터포털 연동 실패 (HTTP 502 / 500)**
```json
{
  "error": "Failed to fetch data from API",
  "details": "..."
}
```
