import { REGIONS } from '@/lib/regions';
import type { PostFilters, PostSummary } from '../types';

export const DEFAULT_FILTERS: PostFilters = {
  keyword: '',
  region: 'all',
  period: 'all',
  pay: 'all',
  sort: 'latest',
  openOnly: true,
};

type Option = { value: string; label: string };

export const REGION_OPTIONS: Option[] = [
  { value: 'all', label: '전체 지역' },
  ...REGIONS.map((region) => ({ value: region, label: region })),
];

export const PERIOD_OPTIONS: Option[] = [
  { value: 'all', label: '전체 기간' },
  { value: 'today', label: '오늘' },
  { value: 'week', label: '이번 주' },
  { value: 'month', label: '이번 달' },
];

export const PAY_OPTIONS: Option[] = [
  { value: 'all', label: '전체 시급' },
  { value: '10000', label: '10,000원 이상' },
  { value: '12000', label: '12,000원 이상' },
  { value: '14000', label: '14,000원 이상' },
];

export const SORT_OPTIONS: Option[] = [
  { value: 'latest', label: '최신 순' },
  { value: 'payHigh', label: '시급 높은 순' },
  { value: 'payLow', label: '시급 낮은 순' },
];

/** 검색·필터·정렬을 적용합니다. (백엔드 목록 API 의 keyword · region · 날짜 · 시급 · 정렬과 같은 조건) */
export function filterPosts(posts: PostSummary[], filters: PostFilters): PostSummary[] {
  const keyword = filters.keyword.trim();

  const filtered = posts.filter((post) => {
    if (keyword && ![post.title, post.hospitalName, post.region, post.district].some((t) => t.includes(keyword))) {
      return false;
    }
    if (filters.region !== 'all' && post.region !== filters.region) return false;
    if (filters.period === 'today' && post.startsInDays !== 0) return false;
    if (filters.period === 'week' && post.startsInDays > 6) return false;
    if (filters.period === 'month' && post.startsInDays > 30) return false;
    if (filters.pay !== 'all' && post.hourlyPay < Number(filters.pay)) return false;
    return true;
  });

  if (filters.sort === 'payHigh') return [...filtered].sort((a, b) => b.hourlyPay - a.hourlyPay);
  if (filters.sort === 'payLow') return [...filtered].sort((a, b) => a.hourlyPay - b.hourlyPay);
  return filtered; // 최신 순 = 목록 순서 그대로
}

export function optionLabel(options: Option[], value: string): string {
  return options.find((option) => option.value === value)?.label ?? value;
}
