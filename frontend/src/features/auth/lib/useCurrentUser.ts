'use client';

import { useAuth, type CurrentUserState } from './AuthProvider';

export type { CurrentUserState };

/**
 * 로그인한 사용자를 돌려줍니다.
 * AuthProvider 가 앱을 열 때 한 번 불러온 결과를 나눠 쓰므로, 화면마다 요청이 더 나가지 않습니다.
 */
export function useCurrentUser(): CurrentUserState {
  return useAuth();
}
