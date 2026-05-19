# FORHOS Backend

> 병원 방문 전 대기 현황을 확인하고, 진료 접수와 사용자 정보를 안전하게 관리하기 위한 병원 대기 관리 서비스 백엔드

---

## 프로젝트 소개

FORHOS Backend는 병원 대기 관리 서비스의 서버 애플리케이션입니다.

사용자는 회원가입과 로그인을 통해 서비스를 이용할 수 있고, 병원 목록을 조회한 뒤 진료 접수를 신청할 수 있습니다. 접수 후에는 자신의 접수 상태와 내 앞 대기 인원을 확인할 수 있도록 API를 제공합니다.

이 프로젝트는 프론트엔드에서 필요한 병원 목록, 회원 정보, 진료 접수, 대기 상태 데이터를 REST API로 제공하는 것을 목표로 설계했습니다.

---

## 기획 배경

병원에 직접 방문하기 전에는 현재 대기 인원이나 예상 대기 시간을 확인하기 어렵습니다.

특히 소규모 병원이나 지방 병원에서는 디지털 대기 시스템이 충분히 갖춰지지 않은 경우가 많아, 사용자가 병원에 도착한 뒤 긴 대기 시간을 겪는 문제가 발생합니다.

FORHOS Backend는 이러한 문제를 줄이기 위해 병원, 회원, 접수, 대기 상태 정보를 서버에서 관리하고 프론트엔드가 안정적으로 사용할 수 있는 API를 제공합니다.

---

## 해결하고자 한 문제

| 문제 | 백엔드 해결 방향 |
| --- | --- |
| 병원 방문 전 대기 현황 확인 어려움 | 병원별 오늘 접수 목록 조회 API 제공 |
| 진료 접수 데이터 관리 필요 | 회원과 병원을 연결한 접수 생성 API 제공 |
| 사용자 정보 반복 입력 | 회원 정보 조회 및 수정 API 제공 |
| 접수 상태 확인 필요 | 본인 접수 상태 및 앞 대기 인원 조회 API 제공 |
| 인증된 사용자만 접수 가능 | JWT 기반 인증 필터 적용 |
| 민감한 사용자 정보 보호 필요 | 비밀번호 BCrypt 암호화 및 본인 접수 검증 |

---

## 주요 기능

- 회원가입
- 로그인 및 JWT 발급
- 내 정보 조회
- 내 정보 수정
- 병원 목록 조회
- 병원 이름 기반 조회 로직
- 진료 접수 신청
- 병원별 오늘 접수 목록 조회
- 내 접수 상태 조회
- 내 앞 대기 인원 계산
- JWT 인증 필터 적용
- 전역 예외 응답 처리
- CORS 설정

---

## 기술 스택

| Category | Tech |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Web | Spring Web MVC |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL |
| Test Database | H2 |
| Security | Spring Security, JWT |
| Validation | Jakarta Validation |
| Build Tool | Gradle |
| Utility | Lombok |

---

## Backend Architecture

```text
src
├─ main
│  ├─ java
│  │  └─ com.jin.practice
│  │     ├─ common
│  │     ├─ config
│  │     ├─ Security
│  │     ├─ util
│  │     ├─ member
│  │     │  ├─ Controller
│  │     │  ├─ Repository
│  │     │  ├─ dto
│  │     │  ├─ entity
│  │     │  └─ service
│  │     ├─ hospital
│  │     │  ├─ Controller
│  │     │  ├─ Repository
│  │     │  ├─ dto
│  │     │  ├─ entity
│  │     │  └─ service
│  │     └─ reception
│  │        ├─ Controller
│  │        ├─ Repository
│  │        ├─ dto
│  │        ├─ entity
│  │        └─ service
│  └─ resources
└─ test
```

| 폴더 | 역할 |
| --- | --- |
| `common` | 공통 예외 응답과 전역 예외 처리 |
| `config` | Security, CORS 등 애플리케이션 설정 |
| `Security` | JWT 인증 필터 |
| `util` | JWT 생성 및 검증 유틸 |
| `member` | 회원가입, 로그인, 내 정보 관리 |
| `hospital` | 병원 정보 조회 |
| `reception` | 진료 접수, 대기열, 접수 상태 관리 |

---

## API 명세

### Member

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/api/members/register` | 회원가입 | No |
| `POST` | `/api/members/login` | 로그인 및 JWT 발급 | No |
| `GET` | `/api/members/myinfo` | 내 정보 조회 | Yes |
| `PATCH` | `/api/members/myinfo` | 내 정보 수정 | Yes |

### Hospital

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/hospital` | 병원 목록 조회 | No |

### Reception

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/api/reception` | 진료 접수 생성 | Yes |
| `GET` | `/api/reception/hospital/{hospitalId}/today` | 병원별 오늘 접수 목록 조회 | Yes |
| `GET` | `/api/reception/hospital/{receptionId}/status` | 내 접수 상태 및 앞 대기 인원 조회 | Yes |

---

## Swagger API 문서

Springdoc OpenAPI를 적용해 백엔드 API를 Swagger UI에서 확인할 수 있도록 구성했습니다.

애플리케이션 실행 후 아래 주소에서 API 목록, 요청/응답 구조, 인증 필요 여부를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

![FORHOS Backend Swagger UI](docs/swagger-ui.png)

---

## 핵심 구현 포인트

### JWT 기반 인증

로그인 성공 시 `JwtProvider`에서 Access Token과 Refresh Token을 생성합니다.

이후 보호된 API 요청은 `JwtAuthenticationFilter`를 통해 토큰을 검증하고, 인증 객체를 SecurityContext에 저장합니다.

```java
new UsernamePasswordAuthenticationToken(principal, "", authorities)
```

이를 통해 컨트롤러에서는 `Authentication` 객체에서 현재 로그인한 사용자의 이메일을 가져와 본인 데이터를 조회합니다.

---

### 비밀번호 암호화

회원가입 시 사용자의 비밀번호는 평문으로 저장하지 않고 `BCryptPasswordEncoder`를 사용해 암호화합니다.

로그인 시에는 입력된 비밀번호와 저장된 암호화 비밀번호를 `matches()`로 비교합니다.

---

### DTO 기반 응답 분리

엔티티를 그대로 반환하지 않고 DTO로 변환하여 응답합니다.

이를 통해 API 응답 구조를 명확하게 유지하고, 엔티티 내부 구조가 외부로 직접 노출되지 않도록 했습니다.

사용한 DTO 예시는 다음과 같습니다.

- `RegisterDto`
- `LoginDto`
- `JwtDto`
- `MyInfoDto`
- `HospitalDto`
- `ReceptionCreateDto`
- `ReceptionDto`
- `ReceptionStatusDto`

---

### 진료 접수 생성

진료 접수는 로그인한 회원과 선택한 병원을 기준으로 생성됩니다.

접수 생성 시 해당 병원의 오늘 접수 목록을 조회하고, 가장 큰 대기 번호에 1을 더해 다음 대기 번호를 부여합니다.

```java
int nextQueueNumber = todayQueue.stream()
        .mapToInt(Reception::getQueueNumber)
        .max()
        .orElse(0) + 1;
```

---

### 내 앞 대기 인원 계산

사용자가 자신의 접수 상태를 조회하면, 서버는 접수 ID를 기준으로 접수 정보를 찾고 로그인한 사용자의 접수가 맞는지 검증합니다.

그 다음 같은 병원의 오늘 접수 목록 중 `WAITING` 상태이면서 내 대기 번호보다 앞선 접수만 계산합니다.

```java
waitingCount = receptionRepository.findByHospital_IdAndQueueDate(
                reception.getHospital().getId(),
                today
        )
        .stream()
        .filter(item -> item.getQueueStatus() == QueueStatus.WAITING)
        .filter(item -> item.getQueueNumber() < reception.getQueueNumber())
        .toList()
        .size();
```

이 방식으로 프론트엔드는 별도 계산 없이 백엔드가 내려주는 `waitingCount` 값을 그대로 표시할 수 있습니다.

---

### 본인 접수 검증

접수 상태 조회 API는 접수 ID만 알면 누구나 조회할 수 있는 구조가 되면 위험합니다.

따라서 서비스 계층에서 현재 로그인한 사용자와 접수의 회원이 같은지 확인하고, 다를 경우 `403 FORBIDDEN`을 반환합니다.

```java
if (reception.getMember().getId() != member.getId()) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
}
```

---

### 접수 상태 관리

접수 상태는 enum으로 관리합니다.

```java
public enum QueueStatus {
    WAITING,
    CALLED,
    COMPLETED,
    CANCELED
}
```

`Reception` 엔티티는 상태 변경 메서드를 가지고 있어, 호출, 완료, 취소 같은 상태 변경 로직을 엔티티 내부에서 관리할 수 있도록 구성했습니다.

---

## 데이터 모델

### Member

회원 정보를 관리하는 엔티티입니다.

- 이메일
- 비밀번호
- 이름
- 나이
- 전화번호
- 성별
- 지역
- 추가 정보

### Hospital

병원 정보를 관리하는 엔티티입니다.

- 병원명
- 주소
- 전화번호
- 운영 상태
- 대기 인원
- 예상 대기 시간
- 평점 정보

### Reception

진료 접수와 대기열 정보를 함께 관리하는 엔티티입니다.

- 회원
- 병원
- 환자명
- 방문 유형
- 증상
- 대기 번호
- 접수 상태
- 접수 날짜
- 접수 시간
- 호출 시간
- 완료 시간
- 취소 시간

---

## 프론트엔드 연동

프론트엔드는 Axios 기반 `apiClient`를 사용해 `/api` prefix로 백엔드와 통신합니다.

Vite 개발 서버에서는 다음과 같이 프록시를 설정해 백엔드 서버로 요청을 전달합니다.

```ts
server: {
  proxy: {
    "/api": {
      target: "http://localhost:8080",
      changeOrigin: true,
    },
  },
}
```

접수 완료 후 프론트엔드는 `receptionId`를 저장하고, 대기 현황 화면에서 다음 API를 호출합니다.

```text
GET /api/reception/hospital/{receptionId}/status
```

응답 예시는 다음과 같습니다.

```json
{
  "receptionId": 1,
  "hospitalId": 2,
  "hospitalName": "병원명",
  "status": "WAITING",
  "queueNumber": 4,
  "waitingCount": 2
}
```

---

## 실행 방법

### 1. 데이터베이스 준비

MySQL에서 `forhos` 데이터베이스를 사용합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/forhos
spring.datasource.username=ssafy
spring.datasource.password=ssafy
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew.bat bootRun
```

### 3. 테스트 실행

```bash
./gradlew test
```

Windows 환경:

```bash
gradlew.bat test
```

---

## 확장 예정 기능

현재 백엔드는 핵심 접수 흐름과 대기 상태 조회를 중심으로 구현되어 있습니다.

서비스 기획 기준으로 다음 기능은 이후 확장 대상으로 둘 수 있습니다.

- 복용 중인 약 정보 등록
- 기존 질병 정보 등록
- 증상 키워드 기반 진료과 분류
- No-show 관리 정책
- 병원 운영자용 접수 호출/완료/취소 API
- 병원별 예상 대기 시간 자동 계산
- Refresh Token 재발급 API

---

## AI 활용 과정

이번 프로젝트에서는 프론트엔드와 백엔드의 연결 흐름을 정리하고, 미완성된 접수 상태 조회 기능을 구현하는 과정에서 AI를 보조 도구로 활용했습니다.

단순히 코드를 생성하는 방식이 아니라, 현재 프로젝트 구조와 사용 중인 기술 스택을 기준으로 어떤 API 계약이 자연스러운지 검토하고, 프론트엔드에서 필요한 응답 구조에 맞춰 백엔드 DTO와 서비스 로직을 정리했습니다.

### 사용한 프롬프트

```text
너가 알아서 깔끔하게 만들어주고 미완성부분 내가 쓰는 기술 스택을 기준으로.
그리고 클린 코드를 꼭 지켜주고 폴더구조는 FSD 구조로 만들어주고
너가 생각해서 비어있는 이미지는 해당 이미지에 어울리는 더미 이미지로 넣어줘.

위에 말을 기준으로 모든 부분 완성 시켜.
```

### 백엔드에 적용한 기준

| 요청 내용 | 백엔드 적용 방향 |
| --- | --- |
| 깔끔하게 만들어줘 | Controller, Service, Repository, DTO 역할 분리 |
| 미완성 부분 완성 | 내 접수 상태 및 앞 대기 인원 조회 API 구현 |
| 사용하는 기술 스택 기준 | Spring Boot, JPA, Security, JWT 구조 유지 |
| 클린 코드 준수 | DTO 응답 분리, 서비스 계층에서 비즈니스 로직 처리 |
| 프론트와 연결 | 프론트에서 바로 사용할 수 있는 API 응답 구조 설계 |

### 느낀 점

AI를 활용하면서 중요한 것은 단순히 기능을 요청하는 것이 아니라, 현재 프로젝트의 구조와 의도를 함께 전달하는 것이라는 점을 느꼈습니다.

특히 백엔드에서는 프론트엔드가 어떤 데이터를 필요로 하는지에 따라 DTO와 API 응답 구조가 달라지기 때문에, 화면 흐름과 API 계약을 함께 생각하는 과정이 중요했습니다.

또한 AI가 제안한 구조를 그대로 사용하는 것이 아니라, 현재 코드의 Repository 메서드, 인증 방식, 도메인 구조에 맞게 조정하면서 프로젝트에 맞는 형태로 적용하는 과정이 필요했습니다.

---

## 프로젝트를 통해 배운 점

이번 프로젝트를 통해 단순히 API를 만드는 것뿐만 아니라, 프론트엔드 화면 흐름과 백엔드 데이터 흐름을 함께 설계하는 경험을 할 수 있었습니다.

JWT 인증을 적용하면서 로그인한 사용자를 기준으로 데이터를 조회하는 방식과, 본인의 접수만 조회할 수 있도록 검증하는 보안 흐름을 이해할 수 있었습니다.

또한 JPA를 사용해 회원, 병원, 접수 엔티티 간의 관계를 구성하고, DTO를 통해 필요한 데이터만 응답하는 방식의 중요성을 배웠습니다.

앞으로는 접수 상태 변경 API, 병원 운영자 기능, 증상 기반 진료과 추천, No-show 정책 같은 기능을 추가하면서 서비스의 완성도를 높여갈 수 있습니다.

---

## 담당 역할

- 백엔드 API 설계
- 회원가입 및 로그인 API 구현
- JWT 발급 및 인증 필터 구성
- 회원 정보 조회 및 수정 API 구현
- 병원 목록 조회 API 구현
- 진료 접수 생성 API 구현
- 병원별 오늘 접수 목록 조회 API 구현
- 내 접수 상태 및 앞 대기 인원 조회 API 구현
- JPA 엔티티 및 Repository 구성
- DTO 기반 요청/응답 구조 설계
- 프론트엔드와 API 연동 구조 정리

---

## 한 줄 요약

> FORHOS Backend는 병원 방문 전 접수와 대기 상태를 관리하고, JWT 인증을 기반으로 사용자별 접수 정보를 안전하게 제공하는 병원 대기 관리 서비스 서버입니다.
