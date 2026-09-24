import type { EscortProfileDto } from '@/features/application';
import { REVIEW_TAG_ROWS, type ReviewDto } from '@/features/review';
import type { RideDto } from '@/features/ride';
import type { Manager } from '../types';

const TAG_LABELS = new Map(REVIEW_TAG_ROWS.flat().map((tag) => [tag.value, tag.label]));

/** 리뷰들의 tags(ENUM 이름)를 모아 많이 받은 순으로 상위 N개의 한글 라벨만 뽑습니다. */
export function topReviewTagLabels(reviews: ReviewDto[], limit = 3): string[] {
  const counts = new Map<string, number>();
  for (const review of reviews) {
    for (const tag of review.tags) {
      counts.set(tag, (counts.get(tag) ?? 0) + 1);
    }
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([value]) => TAG_LABELS.get(value) ?? value);
}

/**
 * GET .../escort-profile 응답 → 동행 매니저 카드용 Manager.
 * 지역은 응답에 없어 비워 둡니다. 태그는 이 프로필과 별개로 리뷰에서 뽑아 채웁니다(topReviewTagLabels).
 */
export function toManager(profile: EscortProfileDto, tags: string[]): Manager {
  return {
    name: profile.name,
    rating: profile.rating,
    completedCount: profile.completedCount,
    tags,
    intro: profile.intro ? [profile.intro] : ['자기소개를 아직 작성하지 않았습니다.'],
  };
}

/** 공고 작성 폼의 이동수단 선택지(model/postForm.ts)와 같은 문구를 씁니다. */
const RIDE_SELECT_LABELS: Record<string, string> = { WALK: '도보', BUS: '대중교통', TAXI: '택시', OWN_CAR: '자가용' };

/** 갈 때(TO_HOSPITAL)·올 때(TO_HOME) 이동수단을 한 줄로 합칩니다. 아직 선택 전이면 "미정"으로 표시합니다. */
export function formatTransport(rides: RideDto[]): string {
  const label = (direction: 'TO_HOSPITAL' | 'TO_HOME') => {
    const ride = rides.find((item) => item.direction === direction);
    return ride?.selected ? (RIDE_SELECT_LABELS[ride.selected] ?? ride.selected) : '미정';
  };
  return `갈 때 ${label('TO_HOSPITAL')} · 올 때 ${label('TO_HOME')}`;
}
