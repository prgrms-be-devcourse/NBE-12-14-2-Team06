import { api, apiDelete, apiPost, apiPut, type SpringPage } from '@/lib/api';
import { daysFromNow, toDateTimeParam } from './lib/date';
import { toPostDetail, toPostSummary } from './model/mapper';
import type { PostDetail, PostDto, PostFilters, PostSummary, PostWriteRequest } from './types';

/** 목록 화면이 쓰는 결과 (공고 카드 + 페이지 정보) */
export type PostPage = {
  posts: PostSummary[];
  totalPages: number;
  totalElements: number;
};

/** 기간 필터 → 동행 시작일 범위 (dateFrom ~ dateTo). "오늘"=오늘, "이번 주"=6일 뒤까지, "이번 달"=30일 뒤까지 */
function periodParams(period: PostFilters['period']): Record<string, string> {
  if (period === 'all') return {};
  const days = period === 'today' ? 0 : period === 'week' ? 6 : 30;
  const to = daysFromNow(days);
  to.setHours(23, 59, 59, 0);
  return { dateFrom: toDateTimeParam(daysFromNow(0)), dateTo: toDateTimeParam(to) };
}

/**
 * 공고 목록 조회 — GET /api/v1/posts
 * page 는 0부터 시작하고, 검색·필터·정렬은 서버가 처리합니다.
 */
export async function fetchPosts(filters: PostFilters, page: number, size: number): Promise<PostPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: filters.sort,
    openOnly: String(filters.openOnly),
  });
  if (filters.keyword) params.set('keyword', filters.keyword);
  if (filters.region !== 'all') params.set('region', filters.region);
  if (filters.pay !== 'all') params.set('minPay', filters.pay);
  Object.entries(periodParams(filters.period)).forEach(([key, value]) => params.set(key, value));

  const data = await api<SpringPage<PostDto>>(`/api/v1/posts?${params.toString()}`);
  return {
    posts: data.content.map(toPostSummary),
    totalPages: data.totalPages,
    totalElements: data.totalElements,
  };
}

/** 공고 상세 조회 — GET /api/v1/posts/{postId} (로그인 없이도 됩니다) */
export async function fetchPost(postId: number): Promise<PostDetail> {
  const dto = await api<PostDto>(`/api/v1/posts/${postId}`);
  return toPostDetail(dto);
}

/** 공고 상세를 백엔드 응답 그대로 조회합니다. 수정 폼에 값을 채울 때처럼 위도·경도 원본이 필요할 때 씁니다. */
export function fetchPostRaw(postId: number): Promise<PostDto> {
  return api<PostDto>(`/api/v1/posts/${postId}`);
}

/** 공고 등록 — POST /api/v1/posts (의뢰인 로그인 쿠키 필요). 등록하면 결제(Payment)도 함께 만들어집니다. */
export function createPost(request: PostWriteRequest): Promise<{ id: number; title: string; postStatus: string; createdAt: string }> {
  return apiPost(`/api/v1/posts`, request);
}

/** 공고 수정 — PUT /api/v1/posts/{postId} (작성자 본인만, 모집 시작 전까지만 가능) */
export function updatePost(postId: number, request: PostWriteRequest): Promise<void> {
  return apiPut<void>(`/api/v1/posts/${postId}`, request);
}

/**
 * 공고 삭제 — DELETE /api/v1/posts/{postId}
 * 작성자(의뢰인) 본인만 지울 수 있고, 로그인 쿠키가 있어야 합니다.
 */
export async function deletePost(postId: number): Promise<void> {
  await apiDelete<void>(`/api/v1/posts/${postId}`);
}
