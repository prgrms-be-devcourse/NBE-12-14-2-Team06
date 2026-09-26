'use client';

import Image from 'next/image';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { fetchMyEscortProfile, fetchMyReviews } from '../api';
import type { ReviewDto, ReviewTagName } from '../types';
import DateRangeFilter from './DateRangeFilter';
import EmptyState from './EmptyState';
import MyPageShell from './MyPageShell';
import StatBar from './StatBar';

const TAG = 'inline-flex h-[18px] items-center rounded-full px-[15px] text-[10px] leading-[15px] font-semibold whitespace-nowrap';

/** 백엔드 ReviewTag 이름 → 화면 문구·긍정 여부 */
const TAG_INFO: Record<ReviewTagName, { label: string; positive: boolean }> = {
  KIND: { label: '친절해요', positive: true },
  PUNCTUAL: { label: '시간을 잘 지켜요', positive: true },
  DETAILED_REPORT: { label: '보고서가 꼼꼼해요', positive: true },
  GOOD_COMMUNICATION: { label: '소통이 잘 돼요', positive: true },
  CAREFUL: { label: '어르신을 세심하게 챙겨요', positive: true },
  LATE: { label: '시간 약속이 아쉬워요', positive: false },
  POOR_COMMUNICATION: { label: '소통이 잘 안 됐어요', positive: false },
  UNKIND: { label: '응대가 아쉬웠어요', positive: false },
  INSUFFICIENT_REPORT: { label: '보고서 내용이 부족해요', positive: false },
};

/** "2026-09-22T10:00:00" → "2026.09.22" */
function formatDate(value: string): string {
  return value.slice(0, 10).replaceAll('-', '.');
}

/**
 * 마이페이지 — 받은 리뷰 (Figma 562:14068)
 *
 * GET /api/v1/users/{userId}/reviews 로 조회합니다. 내 userId 는 동행 매니저 프로필(GET /users/profile/escort)에서 얻습니다.
 * ⚠️ 리뷰 응답에 공고 제목·병원명이 없어서(공고 상세를 알 방법이 없음), 카드에서 그 부분은 뺐습니다.
 */
export default function MyReviewsPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const [fromInput, setFromInput] = useState('');
  const [toInput, setToInput] = useState('');
  const [range, setRange] = useState({ from: '', to: '' });

  const [state, setState] = useState<{ reviews?: ReviewDto[]; error?: string }>();

  useEffect(() => {
    let ignore = false;
    fetchMyEscortProfile()
      .then((profile) => fetchMyReviews(profile.userId))
      .then((reviews) => !ignore && setState({ reviews }))
      .catch((error: Error) => !ignore && setState({ error: error.message }));
    return () => {
      ignore = true;
    };
  }, []);

  if (authLoading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  if (!state?.reviews) {
    return (
      <MyPageShell>
        <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
          <SectionHeading title="받은 리뷰" description="동행을 마친 공고에 대한 리뷰를 확인할 수 있습니다." className="mb-0" />
          <p className="text-base font-semibold text-brand-muted">{state?.error ? `불러오지 못했습니다. (${state.error})` : '불러오는 중입니다.'}</p>
        </div>
      </MyPageShell>
    );
  }

  const allReviews = state.reviews;
  const reviews = allReviews.filter(
    (review) => (!range.from || formatDate(review.createdAt).replaceAll('.', '-') >= range.from) && (!range.to || formatDate(review.createdAt).replaceAll('.', '-') <= range.to),
  );
  const average = allReviews.length ? Math.round((allReviews.reduce((sum, review) => sum + review.rating, 0) / allReviews.length) * 10) / 10 : 0;

  return (
    <MyPageShell>
      <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
        <SectionHeading title="받은 리뷰" description="동행을 마친 공고에 대한 리뷰를 확인할 수 있습니다." className="mb-0" />

        <StatBar
          items={[
            { icon: '/icons/mypage/stat-review.svg', label: '리뷰', value: `${allReviews.length}개` },
            { icon: '/icons/mypage/stat-rating.svg', label: '리뷰 평점', value: `${average}점` },
          ]}
        />

        <div className="w-full max-w-[910px]">
          <DateRangeFilter
              from={fromInput}
              to={toInput}
              onFromChange={setFromInput}
              onToChange={setToInput}
              onSearch={() => setRange({ from: fromInput, to: toInput })}
          />
        </div>

        {reviews.length > 0 ? (
          <ul className="grid w-full max-w-[910px] gap-4 sm:grid-cols-2">
            {reviews.map((review) => {
              const positives = review.tags.filter((tag) => TAG_INFO[tag].positive);
              const negatives = review.tags.filter((tag) => !TAG_INFO[tag].positive);
              return (
                <li key={review.id}>
                  <article className="flex min-h-[271px] flex-col items-center justify-center gap-2.5 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
                    <p className="text-xs font-medium text-brand-muted">{formatDate(review.createdAt)}</p>
                    {review.rating >= 3 ? (
                      <Image src={`/icons/mypage/stars-${review.rating}.svg`} alt={`별점 ${review.rating}점`} width={158} height={27} />
                    ) : (
                      <p className="text-lg font-bold text-brand">{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)} ({review.rating}점)</p>
                    )}
                    {review.tags.length > 0 && (
                      <ul aria-label="리뷰 태그" className="flex flex-wrap justify-center gap-1.5">
                        {positives.map((tag) => (
                          <li key={tag} className={`${TAG} bg-[#6796db] text-white`}>{TAG_INFO[tag].label}</li>
                        ))}
                        {negatives.map((tag) => (
                          <li key={tag} className={`${TAG} bg-[#e6e8ec] text-brand`}>{TAG_INFO[tag].label}</li>
                        ))}
                      </ul>
                    )}
                    <p className="flex min-h-[61px] w-full items-center justify-center rounded-[25px] bg-[#e6e8ec] px-5 py-2.5 text-center text-xs leading-4 text-brand">
                      {review.content || '작성된 의견이 없습니다.'}
                    </p>
                  </article>
                </li>
              );
            })}
          </ul>
        ) : (
            <div className="w-full max-w-[910px]">
              <EmptyState
                  title="받은 리뷰가 없습니다."
                  description="동행을 마치면 의뢰인이 남긴 리뷰가 이곳에 표시됩니다."
              />
            </div>
        )}
      </div>
    </MyPageShell>
  );
}
