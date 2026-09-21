/**
 * 로그인 기능이 붙기 전까지 화면을 확인하기 위한 가짜 로그인 사용자입니다.
 * TODO: 인증(JWT 쿠키)이 연결되면 GET /api/v1/users/profile 결과로 교체하세요.
 *       (role 값이 CLIENT 이면 의뢰인 화면, ESCORT 이면 동행 매니저 화면으로 나눕니다.)
 */
export const MOCK_USER = { name: '나알바' };

/** 관리자 페이지 확인용 가짜 관리자입니다. */
export const MOCK_ADMIN = { name: '관리자' };

/** 의뢰인 화면(/client/**)에서 쓰는 가짜 로그인 사용자. 이름을 누르면 의뢰인 마이페이지로 이동합니다. */
export const MOCK_CLIENT = { name: '김가지', href: '/client' };
