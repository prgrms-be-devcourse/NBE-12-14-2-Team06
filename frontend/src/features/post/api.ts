import { api, type SpringPage } from '@/lib/api';
import { daysFromNow, toDateTimeParam } from './lib/date';
import { toPostSummary } from './model/mapper';
import type { PostDto, PostFilters, PostSummary } from './types';

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
