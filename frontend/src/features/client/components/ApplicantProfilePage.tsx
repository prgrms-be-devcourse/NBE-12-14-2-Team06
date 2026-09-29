'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { fetchEscortProfile, type EscortProfileDto } from '@/features/application';
import { useRequireAuth } from '@/features/auth';
import { fetchUserReviews, reviewTagInfo, type ReviewDto } from '@/features/review';
import { cn } from '@/lib/cn';
import ManagerProfile from './ManagerProfile';

/** 리뷰 태그 칩. 긍정은 파랑, 부정은 회색 — 마이페이지 "받은 리뷰"와 같은 규칙입니다. */
const TAG = 'inline-flex h-[18px] items-center rounded-full px-[15px] text-[10px] leading-[15px] font-semibold whitespace-nowrap';

/** "2026-09-22T10:00:00" → "2026.09.22" */
function formatDate(value: string): string {
  return value.slice(0, 10).replaceAll('-', '.');
}

/**
 * 지원자 프로필 더보기 — 지원자 확인 화면의 "프로필 더보기" 보조 링크로 들어옵니다.
 *
 * - 프로필: GET /api/v1/applications/{applicationId}/escort-profile (지원자 확인 화면과 같은 API)
 * - 받은 리뷰: GET /api/v1/users/{escortId}/reviews (로그인만 하면 누구나 조회 가능)
 */
export default function ApplicantProfilePage() {
  const { loading: authLoading, user } = useRequireAuth('CLIENT');
  const params = useParams<{ postId: string; applicationId: string }>();
  const postId = Number(params.postId);
  const applicationId = Number(params.applicationId);

  const [state, setState] = useState<{ applicationId: number; profile?: EscortProfileDto; reviews?: ReviewDto[]; error?: string }>();

  useEffect(() => {
    let ignore = false;
    fetchEscortProfile(applicationId)
      .then(async (profile) => {
        const reviews = await fetchUserReviews(profile.escortId).catch(() => []);
        if (!ignore) setState({ applicationId, profile, reviews });
      })
      .catch((error: Error) => !ignore && setState({ applicationId, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [applicationId]);

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

  const profile = state?.applicationId === applicationId ? state.profile : undefined;
  const reviews = state?.applicationId === applicationId ? state.reviews : undefined;
  const loadError = state?.applicationId === applicationId ? state.error : undefined;

  const backHref = `/client/posts/${postId}/applicants`;

  if (!profile || !reviews) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">{loadError ? `불러오지 못했습니다. (${loadError})` : '불러오는 중입니다.'}</p>
          <Link href={backHref} className="mx-auto mt-8 flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand">
            지원자 확인으로
          </Link>
        </section>
      </AppShell>
    );
  }

  const manager = {
    name: profile.name,
    rating: profile.rating,
    completedCount: profile.completedCount,
    grade: profile.grade,
    intro: profile.intro ? [profile.intro] : ['자기소개를 아직 작성하지 않았습니다.'],
    ratingCount: profile.ratingCount,
    noShowCount: profile.noShowCount,
    verified: profile.verified,
    age: profile.age,
    gender: profile.gender,
    region: profile.region,
  };

  return (
    <AppShell>
      <section className="bg-white px-4 py-[50px]">
        <div className="mx-auto flex w-full max-w-[910px] flex-col gap-[30px]">
          <SectionHeading title="지원자 프로필" description="이 동행 매니저의 프로필과 받은 리뷰를 확인할 수 있어요." className="-mb-1.5" />

          <section className="rounded-[30px] border border-line-soft bg-white p-8 shadow-card">
            <ManagerProfile manager={manager} size="lg" />
          </section>

          <p className="px-2 text-base leading-6 font-semibold text-brand">받은 리뷰 {reviews.length}개</p>

          {reviews.length > 0 ? (
            <ul className="grid gap-4 sm:grid-cols-2">
              {reviews.map((review) => (
                <li key={review.id}>
                  <article className="flex min-h-[220px] flex-col items-center justify-center gap-2.5 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
                    <p className="text-xs font-medium text-brand-muted">{formatDate(review.createdAt)}</p>
                    <p className="text-lg font-bold text-brand">
                      {'★'.repeat(review.rating)}
                      {'☆'.repeat(5 - review.rating)} ({review.rating}점)
                    </p>
                    {review.tags.length > 0 && (
                      <ul aria-label="리뷰 태그" className="flex flex-wrap justify-center gap-1.5">
                        {review.tags.map((tag) => {
                          const { label, positive } = reviewTagInfo(tag);
                          return (
                            <li key={tag} className={cn(TAG, positive ? 'bg-[#6796db] text-white' : 'bg-[#e6e8ec] text-brand')}>
                              {label}
                            </li>
                          );
                        })}
                      </ul>
                    )}
                    <p className="flex min-h-[61px] w-full items-center justify-center rounded-[25px] bg-[#e6e8ec] px-5 py-2.5 text-center text-xs leading-4 text-brand">
                      {review.content || '작성된 의견이 없습니다.'}
                    </p>
                  </article>
                </li>
              ))}
            </ul>
          ) : (
            <p className="py-10 text-center text-base font-semibold text-brand-muted">아직 받은 리뷰가 없습니다.</p>
          )}

          <Link href={backHref} className="mx-auto flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand">
            지원자 확인으로
          </Link>
        </div>
      </section>
    </AppShell>
  );
}
