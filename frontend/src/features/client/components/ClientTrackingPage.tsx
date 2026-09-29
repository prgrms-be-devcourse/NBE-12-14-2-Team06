'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { MapCard, StageBar, Timeline, type EscortStage } from '@/features/escort';
import { fetchPendingPayment, type PaymentDto } from '@/features/payment';
import { StatusLabel } from '@/features/post';
import { cn } from '@/lib/cn';
import { buildKakaoTCallUrl } from '@/lib/kakaoT';
import { showUnimplemented } from '@/lib/unimplemented';
import { completeEscort, fetchClientEscortCase } from '../api';
import { STAGE_VIEW } from '../model/escort';
import type { ClientEscortCase, ClientEscortStage } from '../types';
import ManagerInfoCard from './ManagerInfoCard';
import TripSummary from './TripSummary';

const CARD = 'rounded-[30px] border border-line bg-white shadow-card';
const BUTTON = 'flex h-11 w-full items-center justify-center rounded-[25px] text-base leading-[18px] font-semibold transition-colors';
const SOLID = 'bg-brand text-white hover:bg-brand-hover';
const GHOST = 'border border-line bg-white text-brand hover:bg-line-soft';

/** 다섯 단계를 위쪽 3단계 표시(StageBar)와 지도(MapCard)의 3단계로 줄입니다. */
const BASE_STAGE: Record<ClientEscortStage, EscortStage> = {
  ready: 'ready',
  ongoing: 'ongoing',
  arrived: 'ongoing',
  finishing: 'ongoing',
  done: 'done',
};

/** 카카오 T 호출 방향: 병원 도착 전에는 집 → 병원, 도착한 뒤부터는 병원 → 집 */
const GOING_HOME: Record<ClientEscortStage, boolean> = {
  ready: false,
  ongoing: false,
  arrived: true,
  finishing: true,
  done: true,
};

/** 진행 요약 카드의 항목 (단계마다 조금씩 다릅니다) */
function summaryRows(escort: ClientEscortCase): { label: string; value: string[] }[] {
  const rows = [
    { label: '예상 시작 시간', value: [escort.startAt] },
    { label: '예상 종료 시간', value: [escort.endAt] },
  ];
  if (escort.stage === 'finishing' && escort.confirmedEndAt) {
    rows.push({ label: '종료 확정 시간', value: [escort.confirmedEndAt] });
  }
  rows.push({ label: '이동수단', value: [escort.transport] });
  if (escort.stage === 'ready') {
    rows.push({ label: '만날 장소', value: [escort.meetingPlace] });
  }
  if (escort.postCompleted) {
    rows.push({ label: '안내 사항', value: ['동행이 정상적으로 완료되었습니다.', '동행인 리뷰를 해주세요.'] });
  }
  return rows;
}

/**
 * 의뢰인 동행 현황 — Figma 의뢰인_매칭 동행 현황 431:4289(매칭) · 431:4119(동행 중) · 464:3497(병원 도착)
 * · 506:2059(귀가 완료) · 431:3953(완료)
 *
 * postId 쿼리가 있으면 실제 API 로 채웁니다(공고: GET /api/v1/posts/{postId}, 매니저: .../escort-profile,
 * 이동수단: GET /api/v1/rides/posts/{postId}). 다른 화면에서 postId 없이 들어올 수도 있어, 그때는
 * 지금처럼 모의 데이터(model/escort.ts)를 보여줍니다.
 * 진행 단계(5종)는 GET /api/v1/applications/{id}/progress 로 실제 조회합니다(model/escort.ts 의
 * PROGRESS_TO_STAGE 참고). ⚠️ 타임라인 단계별 "시각"과 지도의 실시간 위치는 아직 API 가 없어 모의 값입니다.
 */
export default function ClientTrackingPage() {
  const { loading: authLoading, user } = useRequireAuth('CLIENT');
  const params = useParams<{ applicationId: string }>();
  const searchParams = useSearchParams();
  const applicationId = Number(params.applicationId);
  const postIdParam = searchParams.get('postId');
  const postId = postIdParam ? Number(postIdParam) : undefined;

  const [live, setLive] = useState<{ key?: number; escort?: ClientEscortCase; error?: string }>({});
  // 동행 완료 처리 뒤 공고 상태·미결제 건을 다시 받아오려고 올립니다.
  const [reloadKey, setReloadKey] = useState(0);
  const [completing, setCompleting] = useState(false);
  const [completeError, setCompleteError] = useState<string>();
  const [paymentResult, setPaymentResult] = useState<{ key?: number; data: PaymentDto | null }>({ data: null });

  useEffect(() => {
    if (postId === undefined) return;
    let ignore = false;

    fetchClientEscortCase(postId, applicationId)
      .then((escort) => {
        if (!ignore) setLive({ key: postId, escort });
      })
      .catch((error: unknown) => {
        if (!ignore) setLive({ key: postId, error: error instanceof Error ? error.message : '동행 현황을 불러오지 못했습니다.' });
      });

    return () => {
      ignore = true;
    };
  }, [postId, applicationId, reloadKey]);

  // 동행이 끝나야 "추가 결제"가 생깁니다. 완료 전의 READY 결제는 아직 안 낸 최초 결제라 물어보면 안 됩니다.
  const completed = live.key === postId && !!live.escort?.postCompleted;

  useEffect(() => {
    if (postId === undefined || !completed) return;
    let ignore = false;

    fetchPendingPayment(postId)
      .then((payment) => !ignore && setPaymentResult({ key: postId, data: payment }))
      // 조회에 실패해도 동행 현황 자체는 보여 줍니다. (추가 결제 버튼만 안 뜹니다)
      .catch(() => !ignore && setPaymentResult({ key: postId, data: null }));

    return () => {
      ignore = true;
    };
  }, [postId, completed, reloadKey]);

  // 다른 공고로 이동한 직후에는 이전 공고의 결제 정보를 쓰지 않습니다.
  const pendingPayment = completed && paymentResult.key === postId ? paymentResult.data : null;

  const liveLoading = postId !== undefined && live.key !== postId;
  // postId 가 없으면 불러올 방법이 없어 아래 "동행 정보를 찾을 수 없습니다" 로 떨어집니다.
  const liveError = live.key === postId ? live.error : undefined;
  const escort = live.key === postId ? live.escort : undefined;

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

  if (liveLoading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 현황을 불러오는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (liveError) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p role="alert" className="text-xl font-semibold text-brand">{liveError}</p>
          <Link href="/client/posts" className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-brand')}>
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  if (!escort) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</p>
          <Link href="/client/posts" className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-brand')}>
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  const view = STAGE_VIEW[escort.stage];
  const baseStage = BASE_STAGE[escort.stage];
  const rows = summaryRows(escort);
  const base = `/client/escort/${escort.applicationId}`;

  const [from, to] = GOING_HOME[escort.stage]
    ? [escort.hospitalPoint, escort.pickupPoint]
    : [escort.pickupPoint, escort.hospitalPoint];
  // 동행이 끝나면 부를 택시가 없으므로 호출 버튼도 숨깁니다.
  const kakaoT =
    escort.stage !== 'done' && from && to
      ? { href: buildKakaoTCallUrl(from, to), label: `${from.name} → ${to.name}` }
      : undefined;
  const hasStageButtons = escort.stage !== 'ready';

  /**
   * 추가 결제도 공고 등록 때와 같은 결제 화면(토스 위젯)을 씁니다.
   * flow=extra 는 결제 후 공고 등록 완료 화면으로 가지 않기 위한 표시입니다. 본보기: post/components/PostDetailPage.tsx
   */
  const extraPaymentHref = pendingPayment
    ? `/client/posts/new/payment?${new URLSearchParams({
        postId: String(escort.postId),
        paymentId: String(pendingPayment.id),
        amount: String(pendingPayment.amount),
        pay: String(pendingPayment.hourlyPaySnapshot),
        flow: 'extra',
      })}`
    : '';

  /**
   * 동행 완료 처리. 동행인이 "귀가 완료"를 찍어야 서버가 받아주므로,
   * 아직이면 서버 메시지("귀가완료 기록이 없습니다")를 그대로 보여줍니다.
   */
  const handleComplete = async () => {
    if (postId === undefined) return;
    if (!window.confirm('동행을 완료 처리하시겠습니까? 실제 동행 시간을 기준으로 정산·결제가 진행됩니다.')) return;
    setCompleting(true);
    setCompleteError(undefined);
    try {
      await completeEscort(postId);
      setReloadKey((key) => key + 1);
    } catch (error) {
      setCompleteError(error instanceof Error ? error.message : '동행 완료 처리에 실패했습니다.');
    } finally {
      setCompleting(false);
    }
  };

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading title="동행 현황" description="매칭된 동행 일정의 진행 상태와 위치를 확인할 수 있습니다." className="mb-6" />
          <StageBar stage={baseStage} />

          <TripSummary
            badge={view.badge}
            title={escort.title}
            hospitalName={escort.hospitalName}
            region={escort.region}
            scheduleLabel={escort.scheduleLabel}
            durationLabel={escort.durationLabel}
            payLabel={escort.payLabel}
            detailHref={`/client/posts/${escort.postId}`}
          />

          <div className="mt-[22px] grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <div className="flex min-w-0 flex-col gap-[22px]">
              <ManagerInfoCard manager={escort.manager} size="lg" title="동행 정보" />
              <MapCard stage={baseStage} updatedAt={escort.updatedAt} />

              <section className={cn(CARD, 'flex items-center gap-4 px-6 py-6 lg:min-h-[114px]')}>
                <Image src="/icons/escort/notice.svg" alt="" width={48} height={61} className="-my-4 -mx-2.5 shrink-0" />
                <div className="min-w-0">
                  <p className="text-xl leading-6 font-semibold text-brand">{view.notice[0]}</p>
                  <p className="mt-2 text-xs leading-4 font-medium text-brand">{view.notice[1]}</p>
                </div>
              </section>
            </div>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={cn(CARD, 'px-[35px] py-8')}>
                <div className="mb-[15px] flex items-center justify-between gap-3">
                  <h2 className="text-2xl leading-6 font-semibold text-brand">진행 요약</h2>
                  <StatusLabel tone={view.badge.tone} size="large">
                    {view.badge.text}
                  </StatusLabel>
                </div>
                <dl className="flex flex-col gap-[3px]">
                  {rows.map((row) => (
                    <InfoRow key={row.label} label={row.label} labelWidth={125} className={row.value.length > 1 ? 'min-h-[60px] items-start' : ''}>
                      {row.value.map((line) => (
                        <span key={line} className="block">
                          {line}
                        </span>
                      ))}
                    </InfoRow>
                  ))}
                </dl>

                {completeError && (
                  <p role="alert" className="mt-3 text-sm leading-5 font-medium text-[#b91d1d]">
                    {completeError}
                  </p>
                )}

                {(hasStageButtons || kakaoT) && (
                  <div className="mt-5 flex flex-col gap-[5px]">
                    {/* TODO: 추가 결제 · 정산 API 연결 */}
                    {/* stage 는 동행 매니저의 실제 진행 단계라 ARRIVED_HOME 에 닿으면 바로 'done' 이 되지만,
                        Post 는 이 버튼을 눌러야 완료됩니다 — 그래서 stage 가 아니라 postCompleted 로 가립니다. */}
                    {!escort.postCompleted && escort.stage !== 'ready' && (
                      <button type="button" onClick={handleComplete} disabled={completing} className={cn(BUTTON, SOLID, 'disabled:cursor-not-allowed disabled:opacity-60')}>
                        {completing ? '처리 중...' : '동행 완료 처리'}
                      </button>
                    )}
                    {pendingPayment && (
                      <Link href={extraPaymentHref} className={cn(BUTTON, SOLID)}>
                        {`${pendingPayment.amount.toLocaleString()}원 추가 결제`}
                      </Link>
                    )}
                    {escort.postCompleted && (
                      <>
                        {/* 추가 결제가 남아 있으면 정산부터 할 수 없어서 그때는 숨깁니다. */}
                        {!pendingPayment && showUnimplemented() && (
                          <button type="button" className={cn(BUTTON, SOLID)}>
                            정산하기
                          </button>
                        )}
                        {/* 두 화면도 공고 정보가 필요한데 applicationId 만으로는 찾을 수 없어 postId 를 함께 넘깁니다. */}
                        <Link href={`${base}/review?postId=${escort.postId}`} className={cn(BUTTON, GHOST)}>
                          리뷰 작성
                        </Link>
                        <Link href={`${base}/report?postId=${escort.postId}`} className={cn(BUTTON, GHOST)}>
                          보고서 조회
                        </Link>
                      </>
                    )}
                    {kakaoT && (
                      <a
                        href={kakaoT.href}
                        target="_blank"
                        rel="noreferrer"
                        aria-label={`카카오 T 호출하기 (${kakaoT.label})`}
                        className={cn(BUTTON, GHOST)}
                      >
                        카카오 T 호출하기
                        <span className="ml-2 text-sm font-medium text-[#6796db]">{kakaoT.label}</span>
                      </a>
                    )}
                  </div>
                )}
              </section>

              <Timeline steps={escort.timeline} compact />
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
