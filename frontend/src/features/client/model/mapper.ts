import type { EscortProfileDto } from '@/features/application';
import { REVIEW_TAG_ROWS, type ReviewDto } from '@/features/review';
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
    grade: profile.grade,
    tags,
    intro: profile.intro ? [profile.intro] : ['자기소개를 아직 작성하지 않았습니다.'],
  };
}
