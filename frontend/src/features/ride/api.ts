import { api } from '@/lib/api';
import type { RideDto } from './types';

/** 공고의 이동 정보 조회(로그인 쿠키 필요) — GET /api/v1/rides/posts/{postId} */
export function fetchRidesByPost(postId: number): Promise<RideDto[]> {
  return api<RideDto[]>(`/api/v1/rides/posts/${postId}`);
}
