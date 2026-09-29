/** 백엔드 리뷰 응답 (ReviewWriteResponse) — 작성 API 가 돌려주는 모양 그대로. */
export type ReviewDto = {
  id: number;
  applicationId: number;
  rating: number;
  tags: string[];
  content: string | null;
  createdAt: string;
};
