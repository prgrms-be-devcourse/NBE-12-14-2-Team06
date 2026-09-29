import { api } from '@/lib/api';
import type { ReviewDto } from './types';

/** 리뷰 작성 요청 본문. tags 는 ReviewTag ENUM 이름 배열(최대 5개, 선택). content 는 500자 이하, 선택. */
export type WriteReviewPayload = {
  rating: number;
  tags: string[];
  content: string;
};

/** 리뷰 작성(의뢰인 전용) — POST /api/v1/applications/{applicationId}/reviews */
export async function writeReview(applicationId: number, payload: WriteReviewPayload): Promise<ReviewDto> {
  return api<ReviewDto>(`/api/v1/applications/${applicationId}/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

/** 특정 동행인이 받은 리뷰 목록 조회(로그인 쿠키 필요) — GET /api/v1/users/{escortId}/reviews */
export function fetchUserReviews(escortId: number): Promise<ReviewDto[]> {
  return api<ReviewDto[]>(`/api/v1/users/${escortId}/reviews`);
}
