'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { PROGRESS_ORDER, advanceProgress } from '@/features/application';
import { useRequireAuth } from '@/features/auth';
import { useShareLocation } from '@/features/location';
import { StatusLabel } from '@/features/post';
import { fetchReport } from '@/features/report';
import { cn } from '@/lib/cn';
import { showUnimplemented } from '@/lib/unimplemented';
import { fetchTrackingCase } from '../api';
import { STAGE_INFO, TIMELINE_BASE } from '../model/stage';
import type { EscortStage, TrackingCase } from '../types';
import MapCard from './tracking/MapCard';
import StageBar from './tracking/StageBar';
import Timeline from './tracking/Timeline';
import { CARD, CARD_TITLE } from './tracking/tracking';

const BUTTON = 'flex h-11 w-full items-center justify-center rounded-[25px] text-base leading-[18px] font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60';

type CaseResult =
  | { status: 'notFound' }
  | { status: 'error'; message: string }
  | { status: 'ready'; escort: TrackingCase };

/**
 * 동행 현황 — Figma 매칭 동행 현황 188:1295(시작) · 188:1296(동행 중) · 188:1293(완료)
 *
 * 동행 정보는 api.ts 의 fetchTrackingCase 로, 단계 이동은 PATCH /api/v1/applications/{id}/progress 로 처리합니다.
 * ⚠️ 진행 단계를 "읽는" API 가 없어서, 지금 몇 단계인지는 공고 상태로 추정합니다(model/mapper.ts).
 *    타임라인의 단계별 완료 시각도 그래서 표시하지 않습니다.
 */
export default function TrackingPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const params = useParams<{ applicationId: string }>();
  const applicationId = Number(params.applicationId);

  const [result, setResult] = useState<{ key?: number; data?: CaseResult }>({});
  // 완료한 단계 수. 불러온 추정값에서 시작해, 버튼을 누를 때마다 1씩 늘어납니다.
  const [doneCount, setDoneCount] = useState<number>();
  const [advancing, setAdvancing] = useState(false);
  const [progressError, setProgressError] = useState<string>();
  // 보고서를 이미 썼는지. true 면 "보고서 작성" 버튼을 "보고서 조회"로 바꿔서
  // 이미 작성된 보고서를 또 쓰는 화면으로 들어가지 않게 합니다.
  const [hasReport, setHasReport] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(applicationId)) return;
    let ignore = false;

    // 실패(보고서 미작성 포함)하면 hasReport 는 기본값 false 그대로 둡니다.
    fetchReport(applicationId)
      .then(() => !ignore && setHasReport(true))
      .catch(() => {});

    return () => {
      ignore = true;
    };
  }, [applicationId]);

  useEffect(() => {
    if (!Number.isFinite(applicationId)) return;
    let ignore = false;

    fetchTrackingCase(applicationId)
      .then((escort) => {
        if (ignore) return;
        setResult({ key: applicationId, data: escort ? { status: 'ready', escort } : { status: 'notFound' } });
        if (escort) setDoneCount(escort.doneCount);
      })
      .catch((error: unknown) => {
        if (ignore) return;
        setResult({
          key: applicationId,
          data: { status: 'error', message: error instanceof Error ? error.message : '동행 정보를 불러오지 못했습니다.' },
        });
      });

    return () => {
      ignore = true;
    };
  }, [applicationId]);

  const state = result.key === applicationId ? result.data : undefined;

  // 동행 중(출발 기록 후 ~ 귀가 완료 전)에만 이 기기의 위치를 의뢰인에게 공유합니다.
  // 훅은 아래의 조기 return 보다 먼저 불러야 해서, 단계 계산(stage)과 같은 기준을 여기서 한 번 더 씁니다.
  const sharingLocation =
    state?.status === 'ready' && doneCount !== undefined && doneCount >= 2 && doneCount < PROGRESS_ORDER.length;
  const share = useShareLocation(applicationId, sharingLocation);

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

  if (!state) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 불러오는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (state.status !== 'ready' || doneCount === undefined) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p role={state.status === 'error' ? 'alert' : undefined} className="text-xl font-semibold text-brand">
            {state.status === 'error' ? state.message : '동행 정보를 찾을 수 없습니다.'}
          </p>
          <Link href="/mypage/applications" className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-brand')}>
            신청 목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  const escort = state.escort;
  // 완료한 단계 수(doneCount)로 화면 3단계(매칭 완료/동행 중/동행 완료)와 타임라인을 다시 계산합니다.
  const timeline = TIMELINE_BASE.map((step, index) => ({ ...step, done: index < doneCount }));
  const finished = doneCount >= PROGRESS_ORDER.length;
  // 병원 도착(AT_HOSPITAL)까지 끝냈으면 동행이 끝나기 전에도 보고서를 쓸 수 있게 합니다.
  const arrivedAtHospital = doneCount > PROGRESS_ORDER.indexOf('AT_HOSPITAL');
  const stage: EscortStage = finished ? 'done' : doneCount >= 2 ? 'ongoing' : 'ready';
  const info = STAGE_INFO[stage];
  const nextLabel = timeline[doneCount]?.label;
  const detailHref = `/posts/${escort.postId}`;

  const handleAdvance = async () => {
    if (finished) return;
    if (!window.confirm(`'${nextLabel}'(으)로 진행 상태를 변경하시겠습니까?`)) return;
    setAdvancing(true);
    setProgressError(undefined);
    try {
      // TODO: 승인(ACCEPTED)된 지원의 동행 매니저 본인 로그인 쿠키가 있어야 합니다.
      await advanceProgress(escort.applicationId, PROGRESS_ORDER[Math.max(0, doneCount - 1)]);
      setDoneCount((count) => (count ?? 0) + 1);
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
                    <InfoRow label="공고 제목" labelWidth={110}>{escort.title}</InfoRow>
                    <InfoRow label="병원명" labelWidth={110}>{escort.hospitalName}</InfoRow>
                    <InfoRow label="병원 주소" labelWidth={110}>{escort.hospitalAddress}</InfoRow>
                    <InfoRow label="날짜" labelWidth={110}>{escort.dateLabel}</InfoRow>
                    <InfoRow label="시간" labelWidth={110}>{escort.timeLabel}</InfoRow>
                    {/* 이 공고 한 건에만 해당하는 메모 (의뢰인이 공고 등록할 때 씀) */}
                    <InfoRow label="특이사항" labelWidth={110}>{escort.note}</InfoRow>
                  </div>
                  <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-[221px]" />
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="의뢰인" labelWidth={110}>{escort.clientName}</InfoRow>
                    <InfoRow label="연락처" labelWidth={110}>{escort.clientPhone}</InfoRow>
                    <InfoRow label="보호자" labelWidth={110}>{escort.emergencyContactName}</InfoRow>
                    <InfoRow label="보호자 연락처" labelWidth={110}>{escort.emergencyContactPhone}</InfoRow>
                    {/* 의뢰인 개인한테 항상 붙어있는 메모 (마이페이지에 등록해둔 것) */}
                    <InfoRow label="의뢰인 메모" labelWidth={110}>{escort.careNote}</InfoRow>
                  </div>
                </dl>
              </section>

              <MapCard
                stage={stage}
                position={share.position}
                updatedAt={share.lastSentLabel}
                note={share.message}
              />

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
                  {!finished && (
                    <button type="button" onClick={handleAdvance} disabled={advancing} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
                      {advancing ? '처리 중…' : nextLabel}
                    </button>
                  )}
                  {/* 남은 단계(귀가 중·귀가 완료)가 있을 때는 진행 버튼이 주 버튼이라 보고서 버튼은 외곽선으로 둡니다. */}
                  {arrivedAtHospital &&
                    (hasReport ? (
                      <Link
                        href={`/escort/${escort.applicationId}/report`}
                        className={cn(BUTTON, finished ? 'bg-brand text-white hover:bg-brand-hover' : 'border border-line bg-white text-brand hover:bg-line-soft')}
                      >
                        보고서 조회
                      </Link>
                    ) : (
                      <Link
                        href={`/escort/${escort.applicationId}/report/new`}
                        className={cn(BUTTON, finished ? 'bg-brand text-white hover:bg-brand-hover' : 'border border-line bg-white text-brand hover:bg-line-soft')}
                      >
                        보고서 작성
                      </Link>
                    ))}
                  {finished ? (
                    <>
                      <Link href={detailHref} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>공고 상세보기</Link>
                      {/* TODO: 정산 요청 API 가 생기면 연결하세요. */}
                      {showUnimplemented() && (
                        <button type="button" className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>{info.third}</button>
                      )}
                    </>
                  ) : (
                    <>
                      {showUnimplemented() && (
                        <button type="button" className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>의뢰인 연락하기</button>
                      )}
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
