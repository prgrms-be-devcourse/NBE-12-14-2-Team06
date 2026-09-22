import { apiPost } from '@/lib/api';
import { toClientProfileRequest, toEscortProfileRequest, toSignUpRequest } from './model/mapper';
import type { SignupFormValues, SignupRole, UserSignUpResponse } from './types';

/**
 * 회원가입 — POST /api/v1/users (로그인 없이 호출합니다)
 * 성공하면 백엔드가 access·refresh 토큰 쿠키를 함께 내려주므로, 이 시점부터 로그인 상태입니다.
 *
 * 실패 시 ApiError 의 statusCode 로 원인을 알 수 있습니다.
 * 409-1 아이디 중복 · 409-2 이메일 중복 · 409-3 전화번호 중복
 */
export function signUp(values: SignupFormValues, role: SignupRole): Promise<UserSignUpResponse> {
  return apiPost<UserSignUpResponse>('/api/v1/users', toSignUpRequest(values, role));
}

/** 의뢰인 프로필 생성 — POST /api/v1/users/profile/client (가입으로 받은 CLIENT 쿠키 필요) */
export function createClientProfile(values: SignupFormValues): Promise<unknown> {
  return apiPost('/api/v1/users/profile/client', toClientProfileRequest(values));
}

/** 동행 매니저 프로필 생성 — POST /api/v1/users/profile/escort (가입으로 받은 ESCORT 쿠키 필요) */
export function createEscortProfile(values: SignupFormValues): Promise<unknown> {
  return apiPost('/api/v1/users/profile/escort', toEscortProfileRequest(values));
}

/**
 * 가입 + 역할별 프로필 생성을 한 번에 처리합니다.
 * 가입이 끝나야 프로필 API 에 쓸 로그인 쿠키가 생기므로 순서대로 호출합니다.
 */
export async function signUpWithProfile(values: SignupFormValues, role: SignupRole): Promise<void> {
  await signUp(values, role);

  if (role === 'CLIENT') {
    await createClientProfile(values);
  } else {
    await createEscortProfile(values);
  }
}
