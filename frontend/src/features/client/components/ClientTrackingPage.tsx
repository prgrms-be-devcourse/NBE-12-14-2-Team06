'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { fetchEscortProfile } from '@/features/application';
import { useRequireAuth } from '@/features/auth';
import { MapCard, StageBar, Timeline, type EscortStage } from '@/features/escort';
import { fetchPostRaw, StatusLabel } from '@/features/post';
import { fetchRidesByPost } from '@/features/ride';
import { fetchUserReviews } from '@/features/review';
import { cn } from '@/lib/cn';
import { formatTransport, toManager, topReviewTagLabels } from '../model/mapper';
import { STAGE_VIEW, getClientEscortCase, toClientEscortCase } from '../model/escort';
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
    rows.push({ label: '만남 장소', value: ['김가지님 댁 1층', '(서울특별시 강남구 OO아파트 OOO동)'] });
  }
  if (escort.stage === 'done') {
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
 * ⚠️ 진행 단계는 공고 상태로 3단계까지만 대신하고, 타임라인 시각·실시간 위치·결제·정산은 아직 모의 값입니다.
 */
export default function ClientTrackingPage() {
  const { loading: authLoading, user } = useRequireAuth('CLIENT');
  const params = useParams<{ applicationId: string }>();
  const searchParams = useSearchParams();
  const applicationId = Number(params.applicationId);
  const postIdParam = searchParams.get('postId');
  const postId = postIdParam ? Number(postIdParam) : undefined;

  const [live, setLive] = useState<{ key?: number; escort?: ClientEscortCase; error?: string }>({});

  useEffect(() => {
    if (postId === undefined) return;
    let ignore = false;

    Promise.all([fetchPostRaw(postId), fetchEscortProfile(applicationId), fetchRidesByPost(postId)])
      .then(async ([post, profile, rides]) => {
        // 리뷰는 매니저 카드의 태그 계산용이라, 실패해도 나머지 화면은 그대로 보여줍니다.
        const reviews = await fetchUserReviews(profile.escortId).catch(() => []);
        if (ignore) return;
        const manager = toManager(profile, topReviewTagLabels(reviews));
        setLive({ key: postId, escort: toClientEscortCase(post, applicationId, manager, formatTransport(rides)) });
      })
      .catch((error: unknown) => {
        if (!ignore) setLive({ key: postId, error: error instanceof Error ? error.message : '동행 현황을 불러오지 못했습니다.' });
      });

    return () => {
      ignore = true;
    };
  }, [postId, applicationId]);

  const liveLoading = postId !== undefined && live.key !== postId;
  const liveError = postId !== undefined && live.key === postId ? live.error : undefined;
  const escort = postId !== undefined ? (live.key === postId ? live.escort : undefined) : getClientEscortCase(applicationId);

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

                {escort.stage !== 'ready' && escort.stage !== 'ongoing' && (
                  <div className="mt-5 flex flex-col gap-[5px]">
                    {/* TODO: 동행 종료 · 추가 결제 · 정산 API 연결 */}
                    {escort.stage === 'arrived' && (
                      <button type="button" className={cn(BUTTON, SOLID)}>
                        동행 종료
                      </button>
                    )}
                    {escort.stage === 'finishing' && (
                      <button type="button" className={cn(BUTTON, SOLID)}>
                        추가 결제
                      </button>
                    )}
                    {escort.stage === 'done' && (
                      <>
                        <button type="button" className={cn(BUTTON, SOLID)}>
                          정산하기
                        </button>
                        <Link href={`${base}/review`} className={cn(BUTTON, GHOST)}>
                          리뷰 작성
                        </Link>
                        <Link href={`${base}/report`} className={cn(BUTTON, GHOST)}>
                          보고서 조회
                        </Link>
                      </>
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
