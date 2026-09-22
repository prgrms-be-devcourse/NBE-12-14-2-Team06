import { api } from '@/lib/api';
import type { UserProfileDto } from './types';

/** 내 정보 조회(로그인 쿠키 필요) — GET /api/v1/users/profile */
export function fetchProfile(): Promise<UserProfileDto> {
  return api<UserProfileDto>('/api/v1/users/profile');
}
