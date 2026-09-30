<div align=center>

![Header](readme-images/header.png)

<div align=left>

# 📌목차

1. [📄프로젝트 설명](#-프로젝트-설명)
2. [📚기술 스택📚](#-기술-스택-)
3. [👥팀원](#-팀원)
4. [🏗️ERD](#erd)
5. [🔧아키텍처](#아키텍처)
    - [⚙시스템 아키텍처](#시스템-아키텍처)
    - [📂패키지 구조](#패키지-구조)
    - [🌊상태 플로우](#상태-플로우)
6. [🚀트러블 슈팅](#-트러블-슈팅)

</div>

# 📄 프로젝트 설명

### 병원 동행 서비스

---

#### 거동이 불편한 어르신들의 병원 이동부터 진료, 귀가까지 동행 + 고소득꿀알바 타임임박 하실 분 구함

### 문제 제기

---
<div align=left>
<details>
    <summary style="font-weight: bold">점점 늘어나는 고령자 인구</summary>

![elderlypopulation](/readme-images/elderly_population.png)

- 고령자가 해마다 약 5%씩 증가
- 출처: 통계청 - 주요 인구 지표

</details>

<details>
    <summary style="font-weight: bold">독거 노인의 비율</summary>

![singleelderlyratio](/readme-images/single_elderly_ratio.png)

- 독거 노인의 비율이 해마다 약 0.5%씩 증가
- 고령자의 수와 독거 노인의 비율이 **함께 증가**하며, 사회적 위험을 기하급수적으로 **증폭** 초래
- 출처: 국가데이터처, 「장래가구추계 2022」, 「장래인구추계 2022」 2024

</details>

<details>
    <summary style="font-weight: bold">방문 진료</summary>

![carevisit](/readme-images/care_visit.png)

- 전국 의원(37,731개) 중 방문 진료가 가능한 의원(7,027개)의 비율은 **18.6%**
- 방문 진료 병원은 보건복지부 장관이 **지정한 기관**만 가능하며,
  병원이 **신청**을 하고 보건복지부 장관이 승인하는 **번거로운 과정**이 필요
- 출처: 건강보험심사평가원, 통계청 - 시도별 표시과목별 의원 현황

</details>

<details>
    <summary style="font-weight: bold;">거동 불편 노인의 낮은 방문 진료 이용률</summary>

- 2019년 ~ 2024년 상반기 방문 진료 서비스 이용 환자(한의원 제외) - 23,274명
- 이용자의 92.5%는 65살 이상 노인
  이 중 79.4%는 75살 이상
- 국내 거동이 불편한 환자 추정치인 약 270,800명의 8.4%에 불과함
  (2022년 보건행정학회지 수록 논문)
- 출처: [한겨레 - 초고령사회인데…입법조사처 “거동 불편한 노인에 방문 진료 빈약”
  ](https://www.hani.co.kr/arti/society/health/1175302.html)

</details>

</div>

---

# 📚 기술 스택 📚

### Front End

![nextjs](https://img.shields.io/badge/Nextjs-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![html](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=HTML5&logoColor=white)
![css](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=CSS3&logoColor=white)
![typescript](https://img.shields.io/badge/typescript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![react](https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=white)

### Back End

![springboot](https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![springsecurity](https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![junit5](https://img.shields.io/badge/junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![log4j](https://img.shields.io/badge/log4j-F16728?style=for-the-badge&logo=log4j&logoColor=white)
![jwt](https://img.shields.io/badge/JSON_Web_Tokens-85EA2D?style=for-the-badge&logo=JSON-Web-Tokens&logoColor=white)
![swagger](https://img.shields.io/badge/swagger-34E27A?style=for-the-badge&logo=swagger&logoColor=white)
![mysql](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white)
![docker](https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![gradle](https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### Third-Party

![claude](https://img.shields.io/badge/claude-D97757?style=for-the-badge&logo=claude&logoColor=white)
![tosspayment](https://img.shields.io/badge/tosspayment-007acc?style=for-the-badge&logo=tosspayment&logoColor=white)

### Infra

![railway](https://img.shields.io/badge/railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)
![vercel](https://img.shields.io/badge/vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

### Tools

![zoom](https://img.shields.io/badge/zoom-0B5CFF?style=for-the-badge&logo=zoom&logoColor=white)
![slack](https://img.shields.io/badge/slack-4a154b?style=for-the-badge&logo=slack&4a154b=white)
![notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&4a154b=white)  
![github](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=GitHub&181717=white)
![github_actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=GitHub-Actions&logoColor=white)
![figma](https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![canva](https://img.shields.io/badge/canva-0D83CD?style=for-the-badge&logo=canva&logoColor=white)
![intellijidea](https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white)

# 👥 팀원

|                [최훈희](https://github.com/hunhee99)<br>(BE, 팀장)                 |                 [장윤찬](https://github.com/globaltoper)<br>(BE)                  |                 [김신영](https://github.com/kimssin1991)<br>(BE)                 |                   [나희원](https://github.com/lion1230)<br>(BE)                   |                   [박현호](https://github.com/PHH1123)<br>(BE)                    |
|:-----------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|
| <img src='https://avatars.githubusercontent.com/u/67158609?v=4' width='100'/> | <img src='https://avatars.githubusercontent.com/u/276445634?v=4' width='100'/> | <img src='https://avatars.githubusercontent.com/u/82699095?v=4' width='100'/> | <img src='https://avatars.githubusercontent.com/u/301536526?v=4' width='100'/> | <img src='https://avatars.githubusercontent.com/u/193578436?v=4' width='100'/> |
|    <p align="left">- 회원 도메인<br/>- 인증·인가<br/>- 교육 영상 시청 검증<br/>- ERD 설계</p>    |  <p align="left">- 진료 보고서 도메인<br>- 리뷰 도메인<br/>- AI 요약 연동<br/>- 전체 통합 테스트</p>   |      <p align="left">- 공고 도메인<br/>- 카카오맵 연동<br/>- 스케줄러<br/>- 로깅·모니터링</p>      |         <p align="left">- 지원 도메인<br>- 동행 진행 상태<br/>- UI·UX·와이어프레임</p>          |     <p align="left">- 결제, 정산 도메인<br/>- 이동수단 도메인<br/>- 정산 스케줄러<br/>- 서기</p>     |

# ERD

![erd](/readme-images/erd.png)

# 아키텍처

## 시스템 아키텍처

![system-architecture](/readme-images/system_architecture.png)

## 상태 플로우

![status-flow](/readme-images/status_flow.png)

## 패키지 구조

<div align=left>

<details>
    <summary>구조 보기</summary>

    ```
    ├── domain
    │   ├── application
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── enums
    │   │   ├── repository
    │   │   └── service
    │   ├── auth
    │   │   ├── controller
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── education
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── payment
    │   │   ├── client
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── penalty
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── post
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   ├── scheduler
    │   │   └── service
    │   ├── report
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   ├── service
    │   │   └── summarizer
    │   ├── review
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── ride
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   └── service
    │   ├── settlement
    │   │   ├── client
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── repository
    │   │   ├── scheduler
    │   │   └── service
    │   └── user
    │       ├── controller
    │       ├── dto
    │       │   ├── admin
    │       │   ├── login
    │       │   ├── profile
    │       │   ├── signup
    │       │   │   └── common
    │       │   └── user
    │       ├── entity
    │       ├── enums
    │       ├── repository
    │       └── service
    └── global
        ├── config
        ├── entity
        ├── exception
        ├── initData
        ├── response
        ├── restclient
        ├── rq
        ├── scheduler
        └── security
    ```

</details>

</div>

# 🚀 트러블 슈팅

<div align=left>

<details>
    <summary style="font-weight: bold">정산 신뢰성 vs 편의성 트레이드오프 해결</summary>

### 현재 상황

- 의뢰인이 공고를 올릴때 공고에 작성한 예상 결제 금액을 선결제 → 동행 완료 후 EscortProgressLog의 progress가 ARRIVED_HOME이 된 시각 - DEPARTED가 된 시각으로 최종 결제
  금액을 산정한다.

### 문제점

- 우리 서비스의 차별점으로 동행인, 의뢰인 모두 돈에 대한 신뢰는 생각할 필요 없게 하려고 했으나, 의뢰인이 언제 최종 금액을 재결제하든지 결제금액은 동행완료 시에 확정이라 동행인의 서비스에 대한 신뢰가 떨어질 수
  있다.
- 의뢰인이 최종 결제를 늦게 하더라도 아무 패널티가 가지 않는 것이 문제

### 해결 방법

- 최종 결제 금액의 시간 기준을 귀가 완료 시각이 아닌 최종 결제 시간으로 변경: (최종 결제 시각 - 출발 시각) * 시급
    - 예시 - 전동킥보드를 빌리고 최종 결제를 안하고 집에가면 사용 금액이 계속 늘어나는 예시가 있음
    - 장점
        - 의뢰인의 입장에서 최종결제를 미루면 결제금액이 늘어나기 때문에 빠른 최종 결제 유도 가능
        - 동행인은 상대방이 최종결제를 미룬다고 불만이 생기지 않음
    - 단점
        - 의뢰인이 정말로 어쩔 수 없는 상황에서 결제를 못했다고 해도 추가적인 금액이 붙음
        - 실제 서비스 이용 시간에 대한 금액과는 차이가 발생할 수 있음
- 현재 계산식 유지: (귀가완료 시각 - 출발 시각) * 시급
    - 장점
        - 실제 서비스 이용 시간에 대해서만 결제가 진행
        - 웹 결제에 익숙하지 않을 수 있는 의뢰인의 편의를 봐줄 수 있음
    - 단점
        - 어쩔 수 없이 늦은 결제가 아닌 늦은 결제에 대해서 패널티를 줄 수 없음
        - 동행인의 서비스에 대한 신뢰가 떨어질 수 있음

동행인의 신뢰, 의뢰인의 편의 사이의 트레이드오프 관계에 대해서 명확하게 밝히고 회의를 진행하였으며, 우리 서비스의 우선 타깃은 의뢰인이기에 의뢰인의 편의를 봐줄 수 있는 현재 계산식 유지로 결정되었다. 추가적으로
우리 서비스는 공고를 올릴때에 선결제를 진행하기에 최소한 이 부분에 대해서는 동행인도 보장받을 수 있다.

</details>



<details>
    <summary style="font-weight: bold">초기 데이터 생성을 통한 로컬 관리자 권한 테스트 환경 개선</summary>

### 현재 상황

- 회원가입(`POST /api/v1/users`) API에 `role: "ADMIN"`을 보내 관리자 계정을 만들려고 시도
- `UserService.signUp()`이 다른 검증(아이디 중복 등)보다 먼저 `role == ADMIN`이면 예외를 던짐
- 개발용 초기 데이터(`BaseInitData`)에도 원래 의뢰인 3명뿐, 관리자 계정은 없었음
- `AdminUserController`에도 회원을 ADMIN으로 승격시키는 API는 없고 조회·수정·탈퇴만 있음

### 문제점

- 관리자 계정을 만들 수 있는 방법이 프로젝트 전체에 **하나도 없음** (회원가입도 막혀 있고, 승격 API도 없고, 시드 데이터도 없음)
- 그래서 관리자 권한이 필요한 기능(`SecurityConfig`의 `hasRole("ADMIN")`, 관리자 전용 API)을 테스트하려면 매번 로컬 DB에 직접 들어가 SQL로 `role`을 바꿔야 함
- `ddl-auto: create`라 서버를 재시작할 때마다 DB가 초기화되어, 이렇게 만든 관리자 계정도 매번 사라짐 → 매 재시작마다 반복해야 함

### 해결 방법

- 관리자를 만드는 정식 API를 새로 여는 건 보안상 범위 밖이라 하지 않고, **개발용 초기 데이터에 관리자 계정 1명을 심어서 서버가 뜰 때 자동으로 생기게** 함
- 그 전까지는 급한 대로 이렇게 우회함
    1. 일반 계정(`CLIENT` 등)으로 회원가입
    2. 로컬 dev DB에서 직접 `UPDATE`로 `role`을 `ADMIN`으로 변경
    3. **반드시 재로그인** — JWT에는 발급 시점의 `role`이 그대로 박혀 있어서, DB만 바꾸고 기존 토큰을 계속 쓰면 여전히 예전 `role`로 인식됨

### 현재 코드 - 컨트롤러 - 회원가입에서 ADMIN 차단

---

```java
// UserService.signUp
public User signUp(UserSignUpRequest request) {
    // Admin으로 가입 불가
    if (request.role() == Role.ADMIN) {
        throw new BusinessException("400-3", "잘못된 요청입니다.");
    }
    ...
}
```

### 현재 코드 - 임시 우회 — SQL로 직접 승격 후 재로그인

---

```bash
# 1. 승격
docker exec escort-mysql mysql -uescort -pescort6 escort \
  -e "UPDATE users SET role='ADMIN' WHERE username='admin01';"

# 2. 재로그인 (새 토큰에 role=ADMIN 이 담겨야 함)
curl -s -c cookies.txt -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin01","password":"password1!"}'
```

### 변경 코드 - BaseInitData에 관리자 계정 자동 생성 추가

---

```java
// initPosts() 안, 기존 의뢰인 3명 생성 뒤에 추가
createEscortWithProfile("escort01","최동행","010-2000-0001","서울");

createEscortWithProfile("escort02","정동행","010-2000-0002","부산");

createEscortWithProfile("escort03","한동행","010-2000-0003","경기");

createAdmin("admin01","관리자","010-3000-0001","서울");

// 회원가입 API 로는 ADMIN 을 만들 수 없어서(UserService.signUp 참고),
// 로그인 확인용 관리자 계정이 필요할 때마다 DB 를 직접 만졌는데, 이제 dev 서버를 띄우면 자동으로 생김
private User createAdmin(String username, String name, String phoneNum, String region) {
    User admin = User.builder()
            .username(username)
            .password(passwordEncoder.encode("password1!"))
            .email(username + "@example.com")
            .name(name)
            .role(Role.ADMIN)
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(1985, 1, 1))
            .phoneNum(phoneNum)
            .region(region)
            .build();
    return userRepository.save(admin);
}
```

</details>



<details>
    <summary style="font-weight: bold">지원 취소 이력 보존과 재지원 처리를 위한 상태 기반 중복 지원 방안</summary>

### 현재 상황

- 동행매니저의 지원 취소는 지원 상태에 따라 다르게 처리
    - `PENDING` 상태의 지원 취소 → `CANCELED`
    - `ACCEPTED` 상태에서 취소 → `NO_SHOW` 처리 및 노쇼 횟수 증가

    ```
    동행매니저 지원 취소
            │
            ├── PENDING
            │      ↓
            │   CANCELED
            │
            └── ACCEPTED
                   ↓
                NO_SHOW
                   ↓
            acceptedPostId = null
                   ↓
             noShowCount + 1
                   ↓
             모집 기간 확인
              ┌────┴────┐
           마감 전      마감 후
              ↓           ↓
            OPEN       CANCELED
           재모집       공고 취소
    ```

- 이 중 `CANCELED` 처리된 지원은 이력 보존을 위해 Application 데이터를 삭제하지 않고 DB에 유지
- `CANCELED` 처리 후 해당 공고가 아직 지원 가능한 경우 동일 공고에 재지원할 수 있어야 함
- 기존에는 `Post + Escort`의 Application 존재 여부만으로 중복 지원을 판단

### 문제점

- **동행매니저: 지원 취소 후 동일 공고에 재지원할 수 없는 문제**
    - 지원 취소 후에도 기존 Application이 `CANCELED` 상태로 DB에 남아 있기 때문에 기존 중복 지원 검사에서는 이미 지원한 공고로 판단

    ```
    공고 지원
       ↓
    Application(PENDING)
       ↓
    지원 취소
       ↓
    Application(CANCELED) ← DB에는 그대로 존재
       ↓
    동일 공고 재지원
       ↓
    existsByPostAndEscort() == true
       ↓
    "이미 지원한 공고입니다."
    ```

- **의뢰인: 취소된 지원이 지원자 목록에 노출되는 문제**
    - 기존 의뢰인의 지원자 목록 조회는 `postId`만 기준으로 Application을 조회했기 때문에 취소된 지원까지 지원자 목록에 노출
        - 이후 재지원이 가능해지면서 기존 `CANCELED` 지원과 새로운 `PENDING` 지원이 동시에 존재할 수 있어 동일 지원자가 중복 노출되는 문제 발생

    ```
    같은 Post / 같은 Escort
    
    기존 Application → CANCELED
    새 Application   → PENDING
            ↓
    postId 기준 조회
            ↓
    두 Application 모두 반환
            ↓
    동일 지원자 중복 노출
    ```

### 해결 방법

- 중복 지원 판단 시 Application의 상태까지 고려하도록 변경
    - `CANCELED` 이력만 존재하는 경우에는 재지원을 허용하고, 그 외의 유효한 Application이 존재하는 경우에는 중복 지원을 차단

    ```java
    existsByPostAndEscortAndStatusNot(
        post,
        escort,
        ApplicationStatus.CANCELED
    )
    ```

- 지원자 목록 조회에서도 동일하게 `CANCELED` 상태를 제외
    - 취소 이력은 DB에 보존하면서도 의뢰인에게는 현재 유효한 지원만 노출

    ```SQL
    WHERE a.post.id = :postId
    AND a.status <> ApplicationStatus.CANCELED
    ```

## 결론

- `CANCELED` 상태의 지원 이력은 DB에 보존하면서도 동일 공고에 재지원할 수 있도록 개선하고, 의뢰인의 지원자 목록에서는 현재 유효한 지원만 조회되도록 상태 기준을 일관되게 적용

</details>



<details>
    <summary style="font-weight: bold">트랜잭션 경계 분리(퍼사드 패턴)</summary>

### 현재 상황

- 결제 컨트롤러에서 서비스의 결제 요청
- 결제 메서드는 외부 API 요청
- 컨트롤러는 서비스에서 발생하는 모든 예외를 취소 처리

### 문제점

- 외부 API 요청 전에 예외가 발생해도 취소 처리가 됨
- 컨트롤러에 비즈니스 로직이 껴있음
    - 외부 API 요청 실패 시 실패 메서드 호출 (try-catch)
- 중요한 데이터인 결제 정보의 로그를 남기지 않음

### 해결 방법

- 컨트롤러는 하나의 결제 승인 메서드를 호출
- 서비스는 아래의 메서드를 구현
    1. 외부 API 호출 전 검증
    2. 외부 API 호출
    3. DB 반영
    4. 3에서 실패 시 결제를 취소하는 외부 API 호출 및 로그 작성
- 내부 메서드는 프록시를 생성하지 않고 바로 호출을 하기 때문에 트랜잭션 어노테이션이 적용되지 않는다.그러므로 직접 트랜잭션을 적용하거나 별도의 빈으로 등록해서 사용해야하는데, 여기서는 별도의 빈으로 분리해서
  사용했다.
    - PaymentService
    - PaymentPersistenceService
    - TossPaymentService

### 현재 코드 - 컨트롤러

---

```java

@PostMapping("/{paymentId}/confirm")
public RsData<PaymentConfirmResponse> requestConfirm(@AuthenticationPrincipal SecurityUser actor,
                                                     @RequestBody PaymentConfirmRequest request,
                                                     @PathVariable Long paymentId,
                                                     HttpSession session) {
    Long userId = actor.getId();

    // 결제 정보 검증
    verifyAmount(session, new SaveAmountRequest(request.orderId(), request.amount()));

    // 결제 승인 요청 로직, 실패 시 예외(400-11) 발생
    try {
        paymentService.confirm(request, paymentId, userId);
    } catch (InvalidException e) {
        throw e;
    } catch (Exception e) {
        // 기타 DB 저장 하다 예외 발생하는 경우 -> 결제 취소
        cancelPayment(actor, paymentId, new PaymentCancelRequest("서버 에러 발생"));
        throw new InternalServerErrorException(10, "결제 승인 도중 서버 에러가 발생했습니다.");
    }

    return new RsData<>("200-10", "결제 승인에 성공했습니다.",
            new PaymentConfirmResponse(request));
}
```

### 현재 코드 - 서비스

---

```java

@Transactional
public Payment confirm(PaymentConfirmRequest request, Long paymentId, Long userId) {

    Payment payment = this.findById(userId, paymentId);

    payment.statusUpdate(PaymentStatus.IN_PROGRESS);

    String tossPaymentKey = request.paymentKey();
    String tossOrderId = request.orderId();
    String amount = request.amount();

    // 요청 DTO를 JSON으로 변환
    String requestBody = objectMapper.createObjectNode()
            .put("paymentKey", tossPaymentKey)
            .put("orderId", tossOrderId)
            .put("amount", amount)
            .toPrettyString();

    ResponseEntity<TossConfirmResponse> response = tossRestClient.post()
            .uri("/v1/payments/confirm")
            .body(requestBody)
            .retrieve()
            .toEntity(TossConfirmResponse.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
        throw new InvalidException(11, "결제 승인에 실패했습니다.");
    }

    TossConfirmResponse body = response.getBody();
    if (body != null) {
        // 승인 시 상태 변경, 더티 체킹으로 자동 변경
        payment.ApprovePayment(tossOrderId, tossPaymentKey, body.method());
    } else {
        payment.ApprovePayment(tossOrderId, tossPaymentKey, null);
    }

    return payment;
}
```

### 변경 코드 - 컨트롤러

---

```java

@PostMapping("/{paymentId}/confirm")
public RsData<PaymentConfirmResponse> requestConfirm(@AuthenticationPrincipal SecurityUser actor,
                                                     @RequestBody PaymentConfirmRequest request,
                                                     @PathVariable Long paymentId,
                                                     HttpSession session) {
    Long userId = actor.getId();
    String amount = (String) session.getAttribute("amount");

    paymentService.confirm(request, paymentId, userId, amount);

    return new RsData<>("200-10", "결제 승인에 성공했습니다.",
            new PaymentConfirmResponse(request));
}
```

### 변경 코드 - 서비스

---

#### PaymentService

```java
public Payment confirm(PaymentConfirmRequest request, Long paymentId, Long userId, String sessionAmount) {

    Payment payment = this.findById(userId, paymentId);
    String tossPaymentKey = request.paymentKey();
    String tossOrderId = request.orderId();
    String amount = request.amount();
    // 1. 검증 로직
    verifyAmount(sessionAmount, new SaveAmountRequest(null, amount));
    payment.statusUpdate(PaymentStatus.IN_PROGRESS);
    // 2. 외부 API 호출
    ResponseEntity<TossConfirmResponse> response =
            tossPaymentClient.callApiConfirm(tossPaymentKey, tossOrderId, amount);
    // 3. DB 반영
    try {
        paymentPersistenceService.paymentSaveDb(response, paymentId, tossPaymentKey, tossOrderId);
    } catch (NotFoundException e) {
        cancel(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
        log.error("결제 승인 실패", e);
    } catch (RuntimeException ex) {
        cancel(userId, paymentId, new PaymentCancelRequest("서버 에러 발생"));
        log.error("결제 승인 실패", ex);
        throw new InternalServerErrorException(10, "결제 승인 도중 서버 에러가 발생했습니다.");
    }

    log.info("결제 승인 성공, %s".formatted(response));

    return payment;
}
```

#### PaymentPersistenceService

```java

@Transactional
public void paymentSaveDb(ResponseEntity<TossConfirmResponse> response, Long paymentId, String tossPaymentKey, String tossOrderId) {
    TossConfirmResponse body = response.getBody();
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException(10, "결제 정보를 찾을 수 없습니다."));
    if (body != null) {
        // 승인 시 상태 변경, 더티 체킹으로 자동 변경
        payment.ApprovePayment(tossOrderId, tossPaymentKey, body.method());
    } else {
        payment.ApprovePayment(tossOrderId, tossPaymentKey, null);
    }
}
```

#### TossPaymentService

```java
public ResponseEntity<TossConfirmResponse> callApiConfirm(String tossPaymentKey, String tossOrderId, String amount) {

    String requestBody = objectMapper.createObjectNode()
            .put("paymentKey", tossPaymentKey)
            .put("orderId", tossOrderId)
            .put("amount", amount)
            .toPrettyString();

    ResponseEntity<TossConfirmResponse> response;
    try {
        response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(requestBody)
                .retrieve()
                .toEntity(TossConfirmResponse.class);
    } catch (RuntimeException e) {
        throw new InternalServerErrorException(13, e.getMessage());
    }

    if (!response.getStatusCode().is2xxSuccessful()) {
        throw new InvalidException(11, "결제 승인에 실패했습니다.");
    }

    log.info("토스 결제 승인 요청 성공 tossPaymentKey: %s | tossOrderId: %s | amount: %s".formatted(tossPaymentKey, tossOrderId, amount));

    return response;
}
```

</details>



<details>
    <summary style="font-weight: bold">OSIV off 환경의 LazyInitializationException</summary>

### 현재 상황

- 동행인 프로필에서 그 동행인이 받은 리뷰 목록을 조회하는 API (`GET /api/v1/users/{userId}/reviews`)
- 서비스는 `Review` 엔티티 목록을 반환하고, 컨트롤러에서 `ReviewDto` 로 변환
- `Review` 는 태그(`Set<ReviewTag>`)를 별도 테이블에 갖고 있어 지연 로딩 대상
- 프로젝트 설정은 `spring.jpa.open-in-view: false`

### 문제점

- 리뷰 목록 조회 시 500 발생 (`LazyInitializationException`)
- `open-in-view: false` 이므로 서비스의 트랜잭션이 끝나는 순간 영속성 세션이 닫힌다. 그 뒤에 태그를 읽으면 지연 로딩이 불가능하다.
- 컨트롤러에서 DTO 로 변환하긴 했지만, 태그 컬렉션을 **그대로 참조만 넘겼기 때문에** 실제로 읽는 시점은 JSON 직렬화 단계였다. 이미 세션이 닫힌 뒤다.
- 예외가 직렬화 단계에서 터져 스택 트레이스가 Jackson 쪽에 찍혀, 처음에는 DTO 매핑 문제로 오해했다.
- 테스트 클래스에 `@Transactional` 이 붙어 있어 테스트가 끝날 때까지 세션이 유지된다. 그래서 기존 테스트로는 재현되지 않았다. 테스트는 전부 통과하는데 실제 요청만 실패하는 상태였다.

테스트 (@Transactional)  → 트랜잭션 유지 → 지연 로딩 성공 → 통과
실제 요청 → 트랜잭션 종료 → 지연 로딩 실패 → 500

### 해결 방법

- DTO 변환을 컨트롤러가 아니라 서비스의 트랜잭션 안에서 수행한다.
    - `findAllByEscortId` 의 반환 타입을 `List<Review>` → `List<ReviewDto>` 로 변경
- `ReviewDto` 에서 `Set.copyOf` 로 태그를 복사해 엔티티 컬렉션과 분리한다.
    - 변환 위치만 옮기고 컬렉션 참조를 그대로 넘기면, 직렬화 시점에 같은 문제가 다시 발생한다. 끊어내는 작업이 반드시 필요하다.
- `open-in-view: true` 로 되돌리는 선택은 하지 않았다.
    - 뷰 렌더링이 끝날 때까지 DB 커넥션을 붙잡고 있어 트래픽이 몰릴 때 커넥션 풀이 마를 위험이 있다. 끈 것이 맞고, 끈 상태에 맞게 코드를 쓰는 방향으로 해결했다.

### 재발 방지

- `@Transactional` 없이 MockMvc 로 실제 HTTP 요청을 보내는 통합 테스트를 추가하고, 마지막 단계에 리뷰 목록 조회를 포함시켰다.
- 같은 유형의 버그(트랜잭션 종료 이후에 터지는 문제)가 다시 들어오면 이 테스트가 잡는다.

### 현재 코드 - 컨트롤러

```java

@GetMapping("/{userId}/reviews")
public RsData<List<ReviewDto>> list(@PathVariable Long userId) {

    // 서비스는 엔티티를 반환하고, 변환은 트랜잭션 밖에서 일어난다
    List<ReviewDto> reviews = reviewService.findAllByEscortId(userId)
            .stream()
            .map(ReviewDto::new)
            .toList();

    return new RsData<>("200-1", "리뷰 목록 조회 성공", reviews);
}
```

---

### 현재 코드 - 서비스

```java
// 특정 동행인이 받은 리뷰 목록 조회 (클래스의 readOnly 적용)
public List<Review> findAllByEscortId(Long escortId) {

    // 리뷰가 0건인 것은 정상이므로, 회원 존재 여부만 확인
    if (!userRepository.existsById(escortId)) {
        throw new NotFoundException(2, "존재하지 않는 회원입니다.");
    }

    return reviewRepository.findAllByEscortIdWithApplication(escortId);
}
```

### 현재 코드 - DTO

```java
public ReviewDto(Review review) {
    this(
            review.getId(),
            review.getApplication().getId(),
            review.getRating(),
            review.getTags(),          // 지연 로딩 컬렉션을 그대로 참조
            review.getContent(),
            review.getCreatedAt()
    );
}
```

---

### 변경 코드 - 컨트롤러

---

```java

@GetMapping("/{userId}/reviews")
public RsData<List<ReviewDto>> list(@PathVariable Long userId) {

    return new RsData<>("200-1", "리뷰 목록 조회 성공", reviewService.findAllByEscortId(userId));
}
```

### 변경 코드 - 서비스

```java
/**
 * 특정 동행인이 받은 리뷰 목록 조회 (클래스의 readOnly 적용)
 * <p>
 * DTO 변환을 컨트롤러가 아니라 여기서 한다.
 * 엔티티를 그대로 반환하면 트랜잭션이 끝난 뒤 JSON 으로 바꿀 때 태그를 읽게 되어
 * LazyInitializationException 이 발생한다. (open-in-view: false 라 트랜잭션 종료와 함께 세션이 닫힌다)
 */
public List<ReviewDto> findAllByEscortId(Long escortId) {

    // 리뷰가 0건인 것은 정상이므로, 회원 존재 여부만 확인
    if (!userRepository.existsById(escortId)) {
        throw new NotFoundException(2, "존재하지 않는 회원입니다.");
    }

    return reviewRepository.findAllByEscortIdWithApplication(escortId)
            .stream()
            .map(ReviewDto::new)
            .toList();
}
```

### 변경 코드 - DTO

```java
public ReviewDto(Review review) {
    this(
            review.getId(),
            review.getApplication().getId(),
            review.getRating(),
            // 엔티티의 컬렉션을 그대로 들고 나가면 지연 로딩 껍데기를 붙잡게 된다.
            // 트랜잭션이 끝난 뒤 JSON 으로 바꿀 때 세션이 없어 터지므로, 여기서 복사해 끊어낸다.
            Set.copyOf(review.getTags()),
            review.getContent(),
            review.getCreatedAt()
    );
}
```

</details>



<details>
    <summary style="font-weight: bold">정산 모듈 동시성 제어</summary>

### 현재 상황

- 자동 정산 스케줄링은 평일 오전 10시에 진행
- 정산 요청 기능이 존재

### 문제점

- 오전 10시에 정산 요청 기능을 사용하면 이중 지급이 되는 상황

### 동시성 해결을 위한 다양한 방법

1. 낙관적 락
    - 보통 락 중에서 동시에 발생할 확률이 적은 경우 낙관적 락을 사용한다.
    - 하지만 현재 로직은 외부 API를 호출하고 더티 체킹이 아닌 UPDATE 쿼리를 직접 날린다.
    - 낙관적 락을 적용하면 로직을 모두 실행하고 다른 트랜잭션에 의해 값이 바뀌었으면 롤백을 하는데, 여기서 외부 API 호출은 롤백에 해당되지 않으므로 이 방법은 안될 것 같다.
    - 또한, UPDATE 쿼리를 직접 날리므로 Versioning을 할 수 없으므로 안될 것 같다.
2. 비관적 락
    - 비관적 락은 다른 트랜잭션에서 비관적 락을 걸며 데이터를 조회하면 애초에 조회 자체가 불가능하기 때문에 가능할 것 같다.
    - 하지만 지금 비관적 락을 걸어야 하는 위치가 애매하다.
    - 조회에서 비관적 락을 걸 경우 조회가 끝나면 락이 풀린다.
    - 조회 + 외부 API 호출 + 변경을 하나의 트랜잭션으로 묶어 비관적 락을 걸면 결국 API 호출도 하나의 트랜잭션에 묶이게 되는거고, 이전 트러블 슈팅이었던 외부 API와 트랜잭션을 분리한 이유가
      없어지므로 이 방법은 아닌 것 같다.
3. 로직 분리
    - 제일 먼저 정산 중 상태로 바꾼다.
    - 바꿨다면 외부 API를 호출한다.
    - 바꾸지 못했다면 외부 API를 호출하지 않는다.
    - 바꿨지만 중간에 프로세스가 중단되는 경우 정산 중 상태로 남는데, 관리자가 로그를 확인하고 직접 처리하도록 한다.
        - 이 경우에는 실제 정산이 됐는지 안됐는지 관리자가 직접 로그를 확인해봐야 한다.

### 해결 방법

- 동시성을 막기 위한 방법으로 위의 세 방법 중 로직 분리를 사용한다.
- 동시에 요청을 보내는 테스트 케이스 작성
- SettlementStatus ENUM 요소 추가 - PROCESSING
- 정산 중 상태로 바꾸는 Repository 메서드 생성
    - SettlementRepository.updateProcessing(Long settlementId)
- 서비스 로직을 변경
    - 기존
        - 정산 데이터 조회
        - 외부 API 호출
        - 정산 데이터 상태 '완료'로 변경
    - 변경
        - 정산 데이터의 상태가 '정산 전' 이거나 '실패'인 경우에만 정산 데이터 상태를 '정산 중'으로 변경
        - 앞에서 변경된 행이 0건이면 예외 던지고 1건이면 아래 로직을 수행(현재 서비스의 정산 API는 한 건의 정산마다 1번의 외부 API를 호출하도록 되어있습니다)
        - 외부 API 호출
        - 정산 데이터 상태 '완료'로 변경
- 정산 스케줄링 서비스는 개별 적으로 조회 쿼리를 날리지 않고 1회 쿼리로 정산 데이터들을 조회 후개별 정산 진행할 때마다 정산 상태를 '정산 중'으로 변경합니다.외부 API 호출하는 데에 시간이 얼마나 걸릴 지
  모르므로 정산 데이터를 묶어두지 않기 위함입니다.

### 현재 코드 - 서비스

---

#### SettlementService

```java
// 단일 정산 요청
public void request(Long userId, Long settlementId) {

    AccountDto accountDto = settlementPersistenceService.findAccountDto(userId, settlementId);

    String name = accountDto.name();
    String account = accountDto.accountNumber();

    try {
        SettlementClientResponse response = settlementApi(accountDto.payoutAmount(), name, account);
        // 정산 완료 상태 변경
        if (response.res_cnt() >= 1) {
            settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.COMPLETED);
        } else {
            throw new RuntimeException();
        }
    } catch (RuntimeException e) {
        // 금융 결제원 API 요청 에러
        settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.FAILED);
        log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", settlementId, e);
        throw new InternalServerErrorException(30, "정산에 실패했습니다.");
    }
}

// 정산 스케줄링
public int[] settlementProcess() {
    List<AccountDto> accountDtoList = settlementPersistenceService.findAllByStatusAndDate();
    int successCount = 0;
    int failedCount = 0;

    for (AccountDto accountDto : accountDtoList) {
        try {
            SettlementClientResponse response =
                    settlementApi(accountDto.payoutAmount(), accountDto.name(), accountDto.accountNumber());
            // 정산 성공
            if (response.res_cnt() >= 1) {
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.COMPLETED);
                successCount++;
                log.info("정산 성공 - settlementId: {}", accountDto.id());
            } else {
                // 정산 성공 0건
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                failedCount++;
                log.error("정산 실패 - 금융 결제원 API 요청 성공 0건, settlementId: {}", accountDto.id());
            }
        } catch (RuntimeException e) {
            // 금융 결제원 API 요청 에러
            settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
            log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", accountDto.id(), e);
        }
    }

    return new int[]{successCount + failedCount, successCount, failedCount};
}

private SettlementClientResponse settlementApi(int amount, String name, String account) {
    SettlementClientResponse response =
            (SettlementClientResponse) settlementClient.settlementRequest(new SettlementClientRequest(account, name, amount));
    return response;
}
```

#### SettlementPersistenceService

```java

@Transactional
public int updateSettlement(Long settlementId, SettlementStatus status) {
    return settlementRepository.updateStatus(settlementId, status);
}

public AccountDto findAccountDto(Long userId, Long settlementId) {
    // 정산 데이터 조회
    return settlementRepository.findByIdAndState(userId, settlementId)
            .orElseThrow(() -> new NotFoundException(30, "찾으시는 정산 데이터가 없습니다."));
}
```

### 현재 코드 - 레파지토리

---

```java

@Query("select s.id, ep.accountNumber, e.name, s.payoutAmount " +
        "from Settlement s " +
        "join s.escort e " +
        "join EscortProfile ep on ep.userId=e.id " +
        "where s.id=:settlementId and e.id=:userId " +
        "and (s.settlementStatus='PENDING' or s.settlementStatus='FAILED' or s.settlementStatus='PROCESSING')")
Optional<AccountDto> findByIdAndState(@Param("userId") Long userId,
                                      @Param("settlementId") Long settlementId);

// clearAutomatically는 1차 캐시를 비워줌 -> 테스트에서 검증할 때 status 반영이 안되서 추가
@Modifying(clearAutomatically = true)
@Query("update Settlement s set s.settlementStatus=:status where s.id=:id")
int updateStatus(@Param("id") Long id, @Param("status") SettlementStatus status);
```

### 변경 코드 - 서비스

---

#### SettlementService

```java
// 단일 정산 요청
public void request(Long userId, Long settlementId) {

    AccountDto accountDto = settlementPersistenceService.findAccountDto(userId, settlementId);

    String name = accountDto.name();
    String account = accountDto.accountNumber();

    // 정산 중 상태 변경
    int row = settlementPersistenceService.processingSettlement(settlementId);

    // 변경 된 행이 0개라면 정산 중이거나 정산이 완료 된 경우
    if (row == 0) {
        throw new InvalidException(30, "이미 정산 중이거나 정산이 완료되었습니다.");
    }

    try {
        SettlementClientResponse response = settlementApi(accountDto.payoutAmount(), name, account);
        // 정산 완료 상태 변경
        if (response.res_cnt() >= 1) {
            settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.COMPLETED);
            log.info("정산 성공 - settlementId: {}", accountDto.id());
        } else {
            throw new RuntimeException();
        }
    } catch (RuntimeException e) {
        // 금융 결제원 API 요청 에러
        settlementPersistenceService.updateSettlement(settlementId, SettlementStatus.FAILED);
        log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", settlementId, e);
        throw new InternalServerErrorException(30, "정산에 실패했습니다.");
    }
}

// 정산 스케줄링
public int[] settlementProcess() {
    List<AccountDto> accountDtoList = settlementPersistenceService.findAllByStatusAndDate();
    int successCount = 0;
    int failedCount = 0;

    for (AccountDto accountDto : accountDtoList) {
        // 정산 중 상태 변경
        int row = settlementPersistenceService.processingSettlement(accountDto.id());

        // 변경 된 행이 0개라면 정산 중이거나 정산이 완료 된 경우
        if (row == 0) {
            continue;
        }

        try {
            SettlementClientResponse response =
                    settlementApi(accountDto.payoutAmount(), accountDto.name(), accountDto.accountNumber());
            // 정산 성공
            if (response.res_cnt() >= 1) {
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.COMPLETED);
                successCount++;
                log.info("정산 성공 - settlementId: {}", accountDto.id());
            } else {
                // 정산 성공 0건
                settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
                failedCount++;
                log.error("정산 실패 - 금융 결제원 API 요청 성공 0건, settlementId: {}", accountDto.id());
            }
        } catch (RuntimeException e) {
            // 금융 결제원 API 요청 에러
            settlementPersistenceService.updateSettlement(accountDto.id(), SettlementStatus.FAILED);
            log.error("정산 실패 - 금융 결제원 API 요청 실패, settlementId: {}", accountDto.id(), e);
        }
    }

    return new int[]{successCount + failedCount, successCount, failedCount};
}

private SettlementClientResponse settlementApi(int amount, String name, String account) {
    SettlementClientResponse response =
            (SettlementClientResponse) settlementClient.settlementRequest(new SettlementClientRequest(account, name, amount));
    return response;
}
```

#### SettlementPersistenceService

```java

@Transactional
public int updateSettlement(Long settlementId, SettlementStatus status) {
    return settlementRepository.updateStatus(settlementId, status);
}

@Transactional
public int processingSettlement(Long settlementId) {
    return settlementRepository.updateProcessing(settlementId);
}
```

### 변경 코드 - 레파지토리

---

```java

@Query("select s.id, ep.accountNumber, e.name, s.payoutAmount " +
        "from Settlement s " +
        "join s.escort e " +
        "join EscortProfile ep on ep.userId=e.id " +
        "where s.id=:settlementId and e.id=:userId " +
        "and (s.settlementStatus='PENDING' or s.settlementStatus='FAILED' or s.settlementStatus='PROCESSING')")
Optional<AccountDto> findByIdAndState(@Param("userId") Long userId,
                                      @Param("settlementId") Long settlementId);

// clearAutomatically는 1차 캐시를 비워줌 -> 테스트에서 검증할 때 status 반영이 안되서 추가
@Modifying(clearAutomatically = true)
@Query("update Settlement s set s.settlementStatus=:status where s.id=:id")
int updateStatus(@Param("id") Long id, @Param("status") SettlementStatus status);

@Modifying
@Query("update Settlement s " +
        "set s.settlementStatus=SettlementStatus.PROCESSING " +
        "where s.id=:settlementId " +
        "and (s.settlementStatus=SettlementStatus.PENDING or s.settlementStatus=SettlementStatus.FAILED)")
int updateProcessing(@Param("settlementId") Long settlementId);
```

### 테스트 케이스

---

```java

@SpringBootTest
@ActiveProfiles("test")
class SettlementConcurrencyTest {

    private static final int PAYOUT_AMOUNT = 60_000;
    private static final int SETTLEMENT_AMOUNT = (int) (PAYOUT_AMOUNT * 0.9);

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private NoShowPenaltyRepository noShowPenaltyRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EscortProfileRepository escortProfileRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @MockitoBean
    private SettlementClient settlementClient;

    private User client;
    private User escort;

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @BeforeEach
    void setUp() {
        noShowPenaltyRepository.deleteAll();
        settlementRepository.deleteAll();

        String tag = String.valueOf(System.nanoTime());
        String phoneTag = tag.substring(tag.length() - 7);

        client = userRepository.save(User.builder()
                .username("client-" + tag)
                .password("password1!")
                .email("client-" + tag + "@test.com")
                .name("의뢰인이름")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1970, 1, 1))
                .phoneNum("010" + phoneTag + "0")
                .region("서울")
                .build());

        escort = userRepository.save(User.builder()
                .username("escort-" + tag)
                .password("password1!")
                .email("escort-" + tag + "@test.com")
                .name("동행매니저이름")
                .role(Role.ESCORT)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNum("010" + phoneTag + "1")
                .region("서울")
                .build());

        EscortProfile profile = new EscortProfile(escort, "자기소개", "오픈은행", "동행매니저이름", "000-1234567-000");
        EscortProfile savedProfile = escortProfileRepository.save(profile);
        transactionTemplate.executeWithoutResult(transactionStatus ->
                escortProfileRepository.verify(savedProfile.getUserId(), LocalDateTime.now()));
    }

    @Test
    @DisplayName("[SettlementService] 동시성 스케줄러와 정산 요청")
    void concurrency1() throws Exception {

        Post post = savePost("정산 경합 공고", PostStatus.COMPLETED);
        Application application = saveApplication(post, ApplicationStatus.ACCEPTED, post.getId());
        Settlement settlement = saveSettlement(application, 0);

        when(settlementClient.settlementRequest(any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(500);
                    return new SettlementClientResponse("000-1234567-000", "동행매니저이름", SETTLEMENT_AMOUNT);
                });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        executor.submit(() -> {
            start.await();
            settlementService.request(escort.getId(), settlement.getId());
            return null;
        });
        executor.submit(() -> {
            start.await();
            settlementService.settlementProcess();
            return null;
        });
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Mockito.verify(settlementClient, times(1)).settlementRequest(any());
    }

    private Post savePost(String title, PostStatus status) {
        return postRepository.save(Post.builder()
                .client(client)
                .title(title)
                .content("병원 동행 테스트")
                .region("서울")
                .hospitalName("서울아산병원")
                .hospitalAddress("서울특별시 송파구 올림픽로43길 88")
                .hospitalLat(BigDecimal.valueOf(37.52643))
                .hospitalLng(BigDecimal.valueOf(127.1096))
                .pickupAddress("서울특별시 중구 세종대로 지하2")
                .pickupLat(BigDecimal.valueOf(37.555800))
                .pickupLng(BigDecimal.valueOf(126.972000))
                .hourlyPay(15_000)
                .recruitStartAt(LocalDateTime.now().minusDays(5))
                .recruitEndAt(LocalDateTime.now().minusDays(4))
                .escortStartAt(LocalDateTime.now().minusDays(2))
                .escortEndAt(LocalDateTime.now().minusDays(2).plusHours(4))
                .postStatus(status)
                .build());
    }

    private Application saveApplication(Post post, ApplicationStatus status, Long acceptedPostId) {
        return applicationRepository.save(Application.builder()
                .post(post)
                .escort(escort)
                .status(status)
                .acceptedPostId(acceptedPostId)
                .build());
    }

    private Settlement saveSettlement(Application application, int penaltyAmount) {
        return settlementRepository.save(Settlement.builder()
                .payoutAmount(SETTLEMENT_AMOUNT - penaltyAmount)
                .platformFee(PAYOUT_AMOUNT - SETTLEMENT_AMOUNT)
                .penaltyAmount(penaltyAmount)
                .settledDate(LocalDate.now())
                .application(application)
                .escort(escort)
                .build());
    }
}
```

</details>

</div>

</div>