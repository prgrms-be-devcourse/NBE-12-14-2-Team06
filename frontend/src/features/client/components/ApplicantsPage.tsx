'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { StatusLabel } from '@/features/post';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';
import { getApplicants } from '../model/applicants';
import { STATUS_LABEL, getClientPost } from '../model/posts';
import ManagerProfile from './ManagerProfile';

type Decision = 'approved' | 'rejected';

const BUTTON = 'flex h-[35px] flex-1 items-center justify-center rounded-[17px] text-[13px] leading-3 font-semibold transition-colors';

/**
 * 지원자 확인 — Figma 의뢰인_지원자 목록 381:3563
 *
 * ⚠️ 모의 데이터(model/applicants.ts)를 보여줍니다. 승인·거절은 이 화면 안에서만 바뀌고 서버에는 보내지 않습니다.
 */
export default function ApplicantsPage() {
  const params = useParams<{ postId: string }>();
  const postId = Number(params.postId);
  const post = getClientPost(postId);
  const applicants = getApplicants(postId);
  const [decisions, setDecisions] = useState<Record<number, Decision>>({});

  const approvedId = Number(Object.keys(decisions).find((id) => decisions[Number(id)] === 'approved'));

  if (!post) {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">공고를 찾을 수 없습니다.</p>
          <Link href="/client/posts" className="mx-auto mt-8 flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand">
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  // TODO: 지원 승인/거절 API(PATCH /api/v1/applications/{applicationId}/accept · reject) 연결
  const decide = (id: number, decision: Decision) => setDecisions((prev) => ({ ...prev, [id]: decision }));
  const label = STATUS_LABEL[post.status];

  return (
    <AppShell user={MOCK_CLIENT}>
      <section className="bg-white px-4 py-[50px]">
        <div className="mx-auto flex w-full max-w-[1122px] flex-col gap-[30px]">
          <SectionHeading title="지원자 확인" description="지원한 동행 매니저를 확인하고 승인 또는 거절할 수 있어요." className="-mb-1.5" />

          <section className="flex flex-col gap-6 rounded-[30px] border border-line-soft bg-white p-8 shadow-card lg:min-h-[145px] lg:flex-row lg:items-center lg:gap-[35px]">
            <div className="flex items-center gap-[35px]">
              <Image src="/icons/image-placeholder.svg" alt="" width={72} height={72} className="size-[71.6px] shrink-0" />
              <div className="flex min-w-0 flex-col gap-[25px] text-brand lg:w-[177px]">
                <p className="text-2xl leading-4 font-semibold">{post.title}</p>
                <p className="text-base leading-[14px] font-semibold">{post.hospitalName}</p>
              </div>
            </div>
            <dl className="grid flex-1 grid-cols-2 gap-x-6 gap-y-5 text-brand lg:flex lg:items-stretch lg:gap-0">
              {[
                { term: '진료 일시', value: [`${post.dateLabel}`, post.timeLabel], width: 'lg:w-[230px]' },
                { term: '지역', value: [post.location], width: 'lg:w-[181px]' },
                { term: '시급', value: [post.payLabel.replace('시급 ', '')], width: 'lg:w-[181px]' },
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

          <p className="px-2 text-base leading-6 font-semibold text-brand">총 {applicants.length}명의 지원자가 있습니다.</p>

          <ul className="-mt-2.5 grid gap-x-4 gap-y-5 lg:grid-cols-2">
            {applicants.map((applicant) => {
              const decision = decisions[applicant.id];
              const locked = Number.isFinite(approvedId) && approvedId !== applicant.id;
              return (
                <li key={applicant.id} className={cn('flex min-h-[246px] flex-col justify-center gap-[15px] rounded-[30px] border-[0.68px] border-line bg-white p-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]', (decision === 'rejected' || locked) && 'opacity-60')}>
                  <ManagerProfile manager={applicant} size="md" className="px-2 lg:px-[42px]" />
                  <div className="flex gap-[5px] px-2 lg:px-[42px]">
                    {decision ? (
                      <span className={cn(BUTTON, 'cursor-default', decision === 'approved' ? 'bg-brand text-white' : 'bg-[#e6e8ec] text-brand')}>
                        {decision === 'approved' ? '승인 완료' : '거절됨'}
                      </span>
                    ) : (
                      <>
                        <button type="button" disabled={locked} onClick={() => decide(applicant.id, 'rejected')} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft disabled:cursor-not-allowed')}>
                          거절
                        </button>
                        <button type="button" disabled={locked} onClick={() => decide(applicant.id, 'approved')} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover disabled:cursor-not-allowed')}>
                          승인
                        </button>
                      </>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>

          <section className="flex items-center gap-4 rounded-[30px] border border-line bg-line-soft px-6 py-6 lg:min-h-[114px]">
            <Image src="/icons/escort/notice.svg" alt="" width={48} height={61} className="-my-4 -mx-2.5 shrink-0" />
            <div className="min-w-0">
              <p className="text-xl leading-6 font-semibold text-brand lg:text-2xl">동행 매니저 선택 가이드</p>
              <p className="mt-3 text-xs leading-4 font-semibold text-brand lg:text-sm">
                리뷰, 별점, 완료 동행 수를 참고해 동행 매니저를 선택해보세요. 상세보기를 통해 더 자세한 정보를 확인할 수 있습니다.
              </p>
            </div>
          </section>
        </div>
      </section>
    </AppShell>
  );
}
