# NBE-12-14-2-Team06
---
## 기술 스택

| 구분 | 사용 |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA, Hibernate |
| Auth | Spring Security, JWT (jjwt 0.13.0) |
| DB | MySQL 8.0 (Docker), H2 (test) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle |
---
## 세팅

- .env 파일 각자 생성 (변경 시 슬랙으로 공유)
- Docker Desktop (실행 중이어야 함)
---
## DB 띄우기 (개발할 때 띄워주세요.)

```bash
docker compose up -d
```
---
## .env 파일 수정시 컨테이너 재생성
```bash
docker compose down -v && docker compose up -d
```
---
## 브렌치 전략

```
main (배포/제출용)
  ↑ PR
dev (개발 통합 브렌치)
  ↑ PR
feat/... ← 각자 작업 브랜치
```
---
## 브렌치 이름

| 접두사 | 용도 | 예시 |
|---|---|---|
| `feat/` | 기능 추가 | `feat/jwt-login` |
| `fix/` | 버그 수정 | `fix/cookie-parsing` |
| `refactor/` | 리팩터링 | `refactor/rq-getactor` |
| `docs/` | 문서 | `docs/readme` |
| `chore/` | 설정, 빌드 | `chore/docker-compose` |
---
## 작업 흐름

```bash
# 1. 최신 dev 받기
git checkout dev
git pull origin dev

# 2. 브렌치 생성
git switch -c feat/post-entity

# 3. 작업 후 커밋
git add .
git commit -m "feat: Post 엔티티 작성"

# 4. push 전에 dev 최신화 후 병합 (충돌은 각자 최대한 해결해주세요)
git pull origin dev --no-rebase

# 4-1. 충돌 시
# 충돌 파일 확인
git status
# 파일 열어서 <<<<<<< ======= >>>>>>> 부분 정리 후
git add .
git commit

# 5. 내 브렌치 push
git push origin feat/post-entity
```
---
## 커밋 메시지

```
feat: 공고 등록 API 구현
fix: 쿠키에서 apiKey를 읽지 못하던 문제 수정
refactor: Rq.getActor 분기 정리
```
---
## 작업 중 dev가 바뀌었다면

```bash
git pull origin dev --no-rebase
# 충돌 나면 해결 후 커밋
```
---
## 프로필

| 프로필 | DB | ddl-auto | 용도 |
|---|---|---|---|
| `dev` | Docker MySQL (3307) | create | 로컬 개발 (기본) |
| `test` | H2 인메모리 | create-drop | 테스트 |


테스트 클래스에는 `@ActiveProfiles("test")`를 붙입니다.