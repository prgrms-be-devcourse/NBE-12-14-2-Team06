import { api, apiDelete, apiPost } from '@/lib/api';
import type { CurrentUser, LoginFormValues, UserLoginResponse } from './types';

/**
 * 로그인한 내 정보를 가져옵니다.
 * 로그인이 안 돼 있으면 백엔드가 401 + statusCode "401-1"(토큰 없음) / "401-2"(만료) / "401-3"(유효하지 않음) 으로 응답합니다.
 */
export async function fetchMyProfile() {
  return api<CurrentUser>('/api/v1/users/profile');
}

/**
 * 로그인 — POST /api/v1/auth/login (로그인 없이 호출합니다)
 * 성공하면 백엔드가 access·refresh 토큰 쿠키를 내려주므로, 이 시점부터 로그인 상태입니다.
 *
 * 아이디가 없거나 비밀번호가 틀리면 401 "아이디 또는 비밀번호가 올바르지 않습니다." 로 응답합니다.
 * (두 경우를 구분하지 않는 건 계정 존재 여부를 흘리지 않기 위해서입니다.)
 *
 * rememberMe 는 백엔드에 받는 항목이 없어서 보내지 않습니다. 쿠키 유효기간은 서버 설정을 따릅니다.
 */
export function login(values: LoginFormValues): Promise<UserLoginResponse> {
  return apiPost<UserLoginResponse>('/api/v1/auth/login', {
    username: values.username.trim(),
    password: values.password,
  });
}

/** 로그아웃 — DELETE /api/v1/auth/logout (쿠키 필요). 백엔드가 refresh 토큰을 폐기하고 쿠키를 지웁니다. */
export function logout(): Promise<void> {
  return apiDelete<void>('/api/v1/auth/logout');
}
