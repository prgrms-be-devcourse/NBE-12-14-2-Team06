'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import {
  acceptApplication,
  fetchApplicants,
  fetchEscortProfile,
  rejectApplication,
  type Applicant,
} from '@/features/application';
import { useRequireAuth } from '@/features/auth';
import { fetchPost, postStatusLabel, StatusLabel, type PostDetail } from '@/features/post';
import { cn } from '@/lib/cn';
import ManagerProfile from './ManagerProfile';

const BUTTON = 'flex h-[35px] flex-1 items-center justify-center rounded-[17px] text-[13px] leading-3 font-semibold transition-colors disabled:cursor-not-allowed';

/**
 * 지원자 확인 — Figma 의뢰인_지원자 목록 381:3563
 *
 * - 공고 요약: GET /api/v1/posts/{postId}
 * - 지원자 목록: GET /api/v1/applications/posts/{postId}
 * - 지원자 프로필(별점 등): GET /api/v1/applications/{applicationId}/escort-profile (목록 뒤에 한 명씩 추가로 불러옵니다)
 * - 승인/거절: PATCH .../accept · .../reject
 * ⚠️ 지원자 프로필에 지역·태그 정보가 없어 화면에서 뺐습니다.
 */
export default function ApplicantsPage() {
  const { loading: authLoading, user } = useRequireAuth('CLIENT');
  const params = useParams<{ postId: string }>();
  const postId = Number(params.postId);

  // result.postId 로 "지금 postId 의 결과인지" 판단합니다. (아직 안 왔으면 loading)
  const [result, setResult] = useState<{ postId: number; post?: PostDetail; applicants?: Applicant[]; error?: string }>();
  const [decisionError, setDecisionError] = useState<string>();
  const [pendingId, setPendingId] = useState<number>();

  useEffect(() => {
    let ignore = false;

    Promise.all([fetchPost(postId), fetchApplicants(postId)])
      .then(async ([postData, list]) => {
        if (ignore) return;
        // 목록에는 이름·상태만 있어서, 지원자마다 프로필(별점·완료 동행 수 등)을 따로 불러와 합칩니다.
        const withProfile = await Promise.all(
          list.map(async (item): Promise<Applicant> => {
            const profile = await fetchEscortProfile(item.applicationId).catch(() => undefined);
            return { applicationId: item.applicationId, escortId: item.escortId, name: item.escortName, status: item.status, profile };
          }),
        );
        if (!ignore) setResult({ postId, post: postData, applicants: withProfile });
      })
      .catch((error: Error) => !ignore && setResult({ postId, error: error.message }));

    return () => {
      ignore = true;
    };
  }, [postId]);

  const decide = async (applicationId: number, decision: 'approved' | 'rejected') => {
    setPendingId(applicationId);
    setDecisionError(undefined);
    try {
      if (decision === 'approved') {
        await acceptApplication(applicationId);
        // 승인하면 서버가 같은 공고의 나머지 대기 지원을 자동으로 거절 처리합니다.
        setResult((prev) =>
          prev && {
            ...prev,
            applicants: prev.applicants?.map((item) =>
              item.applicationId === applicationId
                ? { ...item, status: 'ACCEPTED' }
                : item.status === 'PENDING'
                  ? { ...item, status: 'REJECTED' }
                  : item,
            ),
          },
        );
      } else {
        await rejectApplication(applicationId);
        setResult((prev) => prev && { ...prev, applicants: prev.applicants?.map((item) => (item.applicationId === applicationId ? { ...item, status: 'REJECTED' } : item)) });
      }
    } catch (error) {
      setDecisionError(error instanceof Error ? error.message : '처리에 실패했습니다.');
    } finally {
      setPendingId(undefined);
    }
  };

  const post = result?.postId === postId ? result.post : undefined;
  const applicants = result?.postId === postId ? result.applicants : undefined;
  const loadError = result?.postId === postId ? result.error : undefined;

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

  if (!post || !applicants) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">{loadError ? `불러오지 못했습니다. (${loadError})` : '불러오는 중입니다.'}</p>
          <Link href="/client/posts" className="mx-auto mt-8 flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand">
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  const label = postStatusLabel(post.postStatus);
  const acceptedId = applicants.find((item) => item.status === 'ACCEPTED')?.applicationId;

  return (
    <AppShell>
      <section className="bg-white px-4 py-[50px]">
        <div className="mx-auto flex w-full max-w-[1122px] flex-col gap-[30px]">
          <SectionHeading title="지원자 확인" description="지원한 동행 매니저를 확인하고 승인 또는 거절할 수 있어요." className="-mb-1.5" />

          <section className="flex flex-col gap-6 rounded-[30px] border border-line-soft bg-white p-8 shadow-card lg:min-h-[145px] lg:flex-row lg:items-center lg:gap-[35px]">
            <div className="flex items-center gap-[35px]">
              <Image src="/icons/image-placeholder.svg" alt="" width={72} height={72} className="size-[71.6px] shrink-0" />
              {/* 제목·병원명은 줄바꿈될 수 있어서 글자 크기보다 넉넉한 leading 을 줍니다. (줄 간격은 Figma 와 같아 보이도록 gap 으로 맞춤) */}
              <div className="flex min-w-0 flex-col gap-[13px] text-brand lg:w-[177px]">
                <p className="text-2xl leading-8 font-semibold">{post.title}</p>
                <p className="text-base leading-[22px] font-semibold">{post.hospitalName}</p>
              </div>
            </div>
            <dl className="grid flex-1 grid-cols-2 gap-x-6 gap-y-5 text-brand lg:flex lg:items-stretch lg:gap-0">
              {[
                { term: '진료 일시', value: [post.startTime], width: 'lg:w-[230px]' },
                { term: '지역', value: [`${post.region} ${post.district}`], width: 'lg:w-[181px]' },
                { term: '시급', value: [`${post.hourlyPay.toLocaleString()}원`], width: 'lg:w-[181px]' },
              ].map((item) => (
                <div key={item.term} className={cn('flex flex-col gap-3 lg:border-l lg:border-[#e6e8ec]/50 lg:pl-[35px]', item.width)}>
                  <dt className="text-2xl leading-6 font-semibold">{item.term}</dt>
                  <dd className="text-base leading-[22px] font-semibold">
                    {item.value.map((line) => (
                      <span key={line} className="block">
                        {line}
                      </span>
                    ))}
                  </dd>
                </div>
              ))}
            </dl>
            <StatusLabel tone={label.tone} size="large">
              {label.text}
            </StatusLabel>
          </section>

          {decisionError && (
            <p role="alert" className="px-2 text-sm font-medium text-[#b91d1d]">
              {decisionError}
            </p>
          )}
          <p className="px-2 text-base leading-6 font-semibold text-brand">총 {applicants.length}명의 지원자가 있습니다.</p>

          {applicants.length === 0 ? (
            <p className="py-10 text-center text-base font-semibold text-brand-muted">아직 지원자가 없습니다.</p>
          ) : (
            <ul className="-mt-2.5 grid gap-x-4 gap-y-5 lg:grid-cols-2">
              {applicants.map((applicant) => {
                const rejected = applicant.status === 'REJECTED' || applicant.status === 'CANCELED';
                const locked = acceptedId !== undefined && acceptedId !== applicant.applicationId;
                const manager = {
                  name: applicant.name,
                  rating: applicant.profile?.rating ?? 0,
                  completedCount: applicant.profile?.completedCount ?? 0,
                  intro: applicant.profile?.intro ? [applicant.profile.intro] : ['자기소개를 아직 작성하지 않았습니다.'],
                };
                return (
                  <li
                    key={applicant.applicationId}
                    className={cn(
                      'flex min-h-[246px] flex-col justify-center gap-[15px] rounded-[30px] border-[0.68px] border-line bg-white p-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]',
                      (rejected || locked) && 'opacity-60',
                    )}
                  >
                    <ManagerProfile manager={manager} size="md" className="px-2 lg:px-[42px]" />
                    <div className="flex gap-[5px] px-2 lg:px-[42px]">
                      {applicant.status === 'ACCEPTED' ? (
                        <span className={cn(BUTTON, 'cursor-default bg-brand text-white')}>승인 완료</span>
                      ) : rejected ? (
                        <span className={cn(BUTTON, 'cursor-default bg-[#e6e8ec] text-brand')}>거절됨</span>
                      ) : (
                        <>
                          <button
                            type="button"
                            disabled={locked || pendingId === applicant.applicationId}
                            onClick={() => decide(applicant.applicationId, 'rejected')}
                            className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}
                          >
                            거절
                          </button>
                          <button
                            type="button"
                            disabled={locked || pendingId === applicant.applicationId}
                            onClick={() => decide(applicant.applicationId, 'approved')}
                            className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}
                          >
                            {pendingId === applicant.applicationId ? '처리 중…' : '승인'}
                          </button>
                        </>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}

          <section className="flex items-center gap-4 rounded-[30px] border border-line bg-line-soft px-6 py-6 lg:min-h-[114px]">
            <Image src="/icons/escort/notice.svg" alt="" width={48} height={61} className="-my-4 -mx-2.5 shrink-0" />
            <div className="min-w-0">
              <p className="text-xl leading-6 font-semibold text-brand lg:text-2xl">동행 매니저 선택 가이드</p>
              <p className="mt-3 text-xs leading-4 font-semibold text-brand lg:text-sm">
                리뷰, 별점, 완료 동행 수를 참고해 동행 매니저를 선택해보세요.
              </p>
            </div>
          </section>
        </div>
      </section>
    </AppShell>
  );
}
