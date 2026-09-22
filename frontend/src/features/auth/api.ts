import { api } from '@/lib/api';
import type { CurrentUser } from './types';

/**
 * 로그인한 내 정보를 가져옵니다.
 * 로그인이 안 돼 있으면 백엔드가 401 + statusCode "401-1"(토큰 없음) / "401-2"(만료) / "401-3"(유효하지 않음) 으로 응답합니다.
 */
export async function fetchMyProfile() {
  return api<CurrentUser>('/api/v1/users/profile');
}
