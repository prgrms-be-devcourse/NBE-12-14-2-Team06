@AGENTS.md

## API 호출 규칙
- 컴포넌트에서 fetch 를 직접 쓰지 않는다.
- src/lib/api.ts 의 api<T>(path, init) 를 쓴다.
  RsData 껍질을 벗겨 data 만 돌려주고, 실패하면 msg 를 담은 Error 를 던진다.
- 실패 에러는 Error 를 상속한 ApiError 클래스이고, msg 외에 statusCode("401-1", "404-2" 등)도 들고 있다.
  상태코드로 분기할 때 쓴다. 타입 별칭으로 바꾸지 않는다 — error instanceof ApiError 로
  좁히는 곳이 있어서 값(클래스)이어야 한다.
  본보기: src/features/auth/lib/AuthProvider.tsx (401 을 "로그인 필요" 상태로 구분)
- 도메인별 호출 함수는 src/features/<도메인>/api.ts 에 모은다.
  본보기: src/features/post/api.ts
- 서버 DTO 타입과 화면용 타입은 types.ts 에 나누어 두고,
  변환이 필요하면 model/mapper.ts 를 만든다.
- 경로는 상대 경로로 쓴다. next.config.ts 의 rewrites 가 8080 으로 넘긴다.
- 기존 화면은 model/ 의 모의 데이터를 쓰고 있다. 실제 API 로 교체할 때
  화면 구조(JSX)와 className 은 그대로 두고 데이터 출처만 바꾼다.
