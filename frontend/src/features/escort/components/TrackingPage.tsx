'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { PROGRESS_ORDER, advanceProgress } from '@/features/application';
import { StatusLabel } from '@/features/post';
import { cn } from '@/lib/cn';
import { STAGE_INFO, getEscortCase } from '../model/cases';
import type { EscortStage } from '../types';
import MapCard from './tracking/MapCard';
import StageBar from './tracking/StageBar';
import Timeline from './tracking/Timeline';
import { CARD, CARD_TITLE } from './tracking/tracking';

const BUTTON = 'flex h-11 w-full items-center justify-center rounded-[25px] text-base leading-[18px] font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60';

/**
 * 동행 현황 — Figma 매칭 동행 현황 188:1295(시작) · 188:1296(동행 중) · 188:1293(완료)
 *
 * ⚠️ 이 화면(제목·병원·의뢰인 정보 등)은 모의 데이터(model/cases.ts)입니다. 백엔드에 "내 지원 상세 조회" API가
 *    아직 없어서 전체를 API로 바꿀 수 없습니다. 대신 "다음 단계로" 버튼은 실제 진행 상태 변경
 *    API(PATCH /api/v1/applications/{id}/progress)를 호출합니다 — 새로고침하면 진행 상태는 모의 데이터로 되돌아갑니다.
 */
export default function TrackingPage() {
  const params = useParams<{ applicationId: string }>();
  const escort = getEscortCase(Number(params.applicationId));

  // 완료된 단계 수. escort.timeline(모의 데이터)의 초기값에서 시작해서, 버튼을 누를 때마다 1씩 늘어납니다.
  const [doneCount, setDoneCount] = useState(() => escort?.timeline.filter((step) => step.done).length ?? 0);
  const [advancing, setAdvancing] = useState(false);
  const [progressError, setProgressError] = useState<string>();

  if (!escort) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</p>
          <Link href="/mypage/applications" className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-brand')}>
            신청 목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  // 완료된 단계 수(doneCount)로 화면 3단계(매칭 완료/동행 중/동행 완료)와 타임라인을 다시 계산합니다.
  const timeline = escort.timeline.map((step, index) => ({ ...step, done: index < doneCount }));
  const finished = doneCount >= PROGRESS_ORDER.length;
  const stage: EscortStage = finished ? 'done' : doneCount >= 2 ? 'ongoing' : 'ready';
  const info = STAGE_INFO[stage];
  const nextLabel = timeline[doneCount]?.label;
  const detailHref = `/posts/${escort.postId}`;

  const handleAdvance = async () => {
    if (finished) return;
    setAdvancing(true);
    setProgressError(undefined);
    try {
      // TODO: 승인(ACCEPTED)된 지원의 동행 매니저 본인 로그인 쿠키가 있어야 합니다.
      await advanceProgress(escort.applicationId, PROGRESS_ORDER[Math.max(0, doneCount - 1)]);
      setDoneCount((count) => count + 1);
    } catch (error) {
      setProgressError(error instanceof Error ? error.message : '진행 상태 변경에 실패했습니다.');
    } finally {
      setAdvancing(false);
    }
  };

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading
            title="동행 현황"
            description="매칭된 동행 일정의 진행 상태와 위치를 확인할 수 있습니다."
            className="mb-6"
          />
          <StageBar stage={stage} />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={cn(CARD, 'px-6 pt-8 pb-6')}>
                <h2 className={cn(CARD_TITLE, 'mb-[21px]')}>동행 정보</h2>
                <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="공고 제목" labelWidth={92}>{escort.title}</InfoRow>
                    <InfoRow label="병원명" labelWidth={92}>{escort.hospitalName}</InfoRow>
                    <InfoRow label="병원 주소" labelWidth={92}>{escort.hospitalAddress}</InfoRow>
                    <InfoRow label="날짜" labelWidth={92}>{escort.dateLabel}</InfoRow>
                    <InfoRow label="시간" labelWidth={92}>{escort.timeLabel}</InfoRow>
                  </div>
                  <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-[221px]" />
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="의뢰인명" labelWidth={120}>{escort.clientName}</InfoRow>
                    <InfoRow label="연락처" labelWidth={120}>{escort.clientPhone}</InfoRow>
                    <InfoRow label="특이사항" labelWidth={120}>{escort.note}</InfoRow>
                    <InfoRow label="보호자명" labelWidth={120}>{escort.guardianName}</InfoRow>
                    <InfoRow label="보호자 연락처" labelWidth={120}>{escort.guardianPhone}</InfoRow>
                  </div>
                </dl>
              </section>

              <MapCard stage={stage} />

              <section className={cn(CARD, 'flex items-center gap-4 px-6 py-6')}>
                <Image src="/icons/escort/notice.svg" alt="" width={48} height={61} className="-my-4 -mx-2.5 shrink-0" />
                <div className="min-w-0">
                  <p className="text-xl leading-6 font-semibold text-brand">
                    {stage === 'ready'
                      ? '동행이 시작되면 실시간 위치 공유가 활성화됩니다.'
                      : stage === 'ongoing'
                        ? '동행 진행 상황을 실시간으로 업데이트해 주세요.'
                        : '위치 공유가 종료되었습니다.'}
                  </p>
                  <p className="mt-2 text-xs leading-4 font-medium text-brand">
                    {stage === 'ready'
                      ? '동행 시작 후, 현재 위치와 이동 경로가 실시간으로 공유됩니다.'
                      : stage === 'ongoing'
                        ? '동행하신 위치는 의뢰인이 확인할 수 있습니다. 안전한 동행을 위해 정확한 정보를 제공해주세요.'
                        : '동행이 완료되어 실시간 위치 공유가 중지되었습니다.'}
                  </p>
                </div>
              </section>
            </div>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={cn(CARD, 'px-6 py-8')}>
                <div className="mb-6 flex items-center justify-between gap-3">
                  <h2 className={CARD_TITLE}>진행 요약</h2>
                  <StatusLabel tone={info.badgeTone} size="large">{info.badge}</StatusLabel>
                </div>
                <dl className="flex flex-col gap-[3px]">
                  <InfoRow label="예상 시작 시간" labelWidth={135}>{escort.startAt}</InfoRow>
                  <InfoRow label="예상 종료 시간" labelWidth={135}>{escort.endAt}</InfoRow>
                  <InfoRow label="이동수단" labelWidth={135}>{escort.transport}</InfoRow>
                  <InfoRow label="안내 사항" labelWidth={135} className="items-start">
                    {info.guide.map((line) => (
                      <span key={line} className="block">{line}</span>
                    ))}
                  </InfoRow>
                </dl>
                {progressError && (
                  <p role="alert" className="mt-3 text-sm font-medium text-[#b91d1d]">
                    {progressError}
                  </p>
                )}
                <div className="mt-5 flex flex-col gap-1.5">
                  {finished ? (
                    <Link href={`/escort/${escort.applicationId}/report/new`} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
                      {info.primary}
                    </Link>
                  ) : (
                    <button type="button" onClick={handleAdvance} disabled={advancing} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
                      {advancing ? '처리 중…' : nextLabel}
                    </button>
                  )}
                  {finished ? (
                    <>
                      <Link href={detailHref} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>공고 상세보기</Link>
                      <button type="button" className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>{info.third}</button>
                    </>
                  ) : (
                    <>
                      <button type="button" className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>의뢰인 연락하기</button>
                      <Link href={detailHref} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>{info.third}</Link>
                    </>
                  )}
                </div>
              </section>

              <Timeline steps={timeline} />
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
