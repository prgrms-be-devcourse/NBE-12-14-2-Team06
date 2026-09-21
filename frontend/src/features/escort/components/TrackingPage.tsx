'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { StatusLabel } from '@/features/post';
import { cn } from '@/lib/cn';
import { MOCK_USER } from '@/lib/mockSession';
import { STAGE_INFO, getEscortCase } from '../model/cases';
import type { EscortCase, EscortStage } from '../types';

const CARD = 'rounded-[30px] border border-line bg-white shadow-card';
const CARD_TITLE = 'text-2xl leading-6 font-semibold text-brand';
const BUTTON = 'flex h-11 w-full items-center justify-center rounded-[25px] text-base leading-[18px] font-semibold transition-colors';
const STEP_LABELS = ['매칭 완료', '동행 중', '동행 완료'];
const STAGE_INDEX: Record<EscortStage, number> = { ready: 0, ongoing: 1, done: 2 };

/** 위쪽 3단계 표시 (Figma 진행상황: 파란색 계열) */
function StageBar({ stage }: { stage: EscortStage }) {
  const current = STAGE_INDEX[stage];
  return (
    <ol aria-label="동행 진행 단계" className="mx-auto mb-[30px] flex w-full max-w-[411px]">
      {STEP_LABELS.map((label, index) => {
        const done = index < current;
        const active = index === current;
        return (
          <li key={label} aria-current={active ? 'step' : undefined} className="flex flex-1 flex-col items-center gap-[18px] px-1 py-3">
            <span className="relative size-9 shrink-0">
              <Image
                src={done ? '/icons/step-done-bg.svg' : active ? '/icons/escort/step-active.svg' : '/icons/escort/step-upcoming.svg'}
                alt=""
                width={36}
                height={36}
              />
              {done ? (
                <Image src="/icons/step-done-check.svg" alt="완료" width={36} height={36} className="absolute inset-0" />
              ) : (
                <span
                  className={cn(
                    'absolute inset-0 grid place-items-center text-[15px] leading-none font-semibold',
                    active ? 'text-white' : 'text-[#6796db]',
                  )}
                >
                  {index + 1}
                </span>
              )}
            </span>
            <span className={cn('text-[15px] leading-[21px] font-bold whitespace-nowrap lg:text-lg', active ? 'text-[#6796db]' : 'text-[#91a9d8]')}>
              {label}
            </span>
          </li>
        );
      })}
    </ol>
  );
}

/** 지도 + 마커 (Figma 실시간 위치). 마커·경로 위치는 화면용 임시 값입니다. */
function MapCard({ stage }: { stage: EscortStage }) {
  const status =
    stage === 'ready' ? (
      <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 공유 대기중</span>
    ) : stage === 'ongoing' ? (
      <span className="flex items-center gap-3 text-base leading-5 font-semibold text-brand">
        <span className="flex items-center gap-1.5">
          <span aria-hidden="true" className="size-2 rounded-full bg-[#209d37]" />
          위치 공유중
        </span>
        <span className="text-[#c0c0c2]">최근 업데이트 9:00</span>
      </span>
    ) : (
      <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 공유 종료</span>
    );

  return (
    <section className={cn(CARD, 'px-6 pt-8 pb-6')}>
      <div className="mb-6 flex items-center justify-between gap-3">
        <h2 className={CARD_TITLE}>실시간 위치</h2>
        {status}
      </div>
      <div className="relative h-[300px] overflow-hidden rounded-[30px] bg-line-soft lg:h-[400px]">
        <Image src="/images/escort-map.png" alt="동행 경로 지도" fill sizes="700px" className="object-cover" />
        {stage !== 'ready' && (
          <svg aria-hidden="true" viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 size-full">
            <polyline
              points={stage === 'done' ? '22,72 22,48 40,48 40,30 58,30' : '22,72 22,48 40,48'}
              fill="none"
              stroke="#353e5c"
              strokeWidth="3"
              vectorEffect="non-scaling-stroke"
              strokeLinejoin="round"
            />
          </svg>
        )}
        <span className="absolute top-[30%] left-[58%] -translate-x-1/2 -translate-y-full">
          <Image src="/icons/escort/marker.svg" alt="병원" width={57} height={57} />
        </span>
        <span className="absolute top-[72%] left-[22%] grid size-11 -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full bg-[#353e5c]">
          <Image src="/icons/escort/home.svg" alt="출발지" width={20} height={20} className="brightness-0 invert" />
        </span>
      </div>
    </section>
  );
}

/** 오른쪽 아래 "실시간 현황" 타임라인 */
function Timeline({ steps }: { steps: EscortCase['timeline'] }) {
  return (
    <section className={cn(CARD, 'px-6 py-8')}>
      <h2 className={cn(CARD_TITLE, 'mb-6')}>실시간 현황</h2>
      <ol>
        {steps.map((step, index) => {
          const last = index === steps.length - 1;
          return (
            <li key={step.label} className="relative flex gap-4 pb-[18px] last:pb-0">
              {!last && (
                <span
                  aria-hidden="true"
                  className={cn('absolute top-[25px] bottom-0 left-[11px] w-[3px] rounded-full', step.done && steps[index + 1].done ? 'bg-brand' : 'bg-[#a3a3a4]/30')}
                />
              )}
              <Image
                src={step.done ? '/icons/escort/timeline-check.svg' : '/icons/escort/timeline-dot.svg'}
                alt={step.done ? '완료' : '대기'}
                width={25}
                height={25}
                className="relative shrink-0"
              />
              <div className={cn('flex min-w-0 flex-1 items-center gap-3', !step.done && 'opacity-40')}>
                <span className="w-[86px] shrink-0 text-base leading-5 font-semibold text-brand">{step.label}</span>
                <span className="min-w-0 flex-1 text-xs leading-4 font-medium text-brand-muted">{step.description}</span>
                {step.time && <span className="shrink-0 text-xs leading-4 font-medium text-brand-muted">{step.time}</span>}
              </div>
            </li>
          );
        })}
      </ol>
    </section>
  );
}

/**
 * 동행 현황 — Figma 매칭 동행 현황 188:1295(시작) · 188:1296(동행 중) · 188:1293(완료)
 *
 * ⚠️ 모의 데이터(model/cases.ts)를 보여줍니다. 위치 공유·진행 상태 변경은 아직 연결하지 않았습니다.
 */
export default function TrackingPage() {
  const params = useParams<{ applicationId: string }>();
  const escort = getEscortCase(Number(params.applicationId));

  if (!escort) {
    return (
      <AppShell user={MOCK_USER}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</p>
          <Link href="/mypage/applications" className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-brand')}>
            신청 목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  const info = STAGE_INFO[escort.stage];
  const detailHref = `/posts/${escort.postId}`;

  return (
    <AppShell user={MOCK_USER}>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading
            title="동행 현황"
            description="매칭된 동행 일정의 진행 상태와 위치를 확인할 수 있습니다."
            className="mb-6"
          />
          <StageBar stage={escort.stage} />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={cn(CARD, 'px-6 pt-8 pb-6')}>
                <h2 className={cn(CARD_TITLE, 'mb-[21px]')}>동행 정보</h2>
                <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="공고 제목" labelWidth={92}>{escort.title}</InfoRow>
                    <InfoRow label="병원명" labelWidth={92}>{escort.hospitalName}</InfoRow>
                    <InfoRow label="날짜" labelWidth={92}>{escort.dateLabel}</InfoRow>
                    <InfoRow label="시간" labelWidth={92}>{escort.timeLabel}</InfoRow>
                  </div>
                  <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-40" />
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="의뢰인명" labelWidth={92}>{escort.clientName}</InfoRow>
                    <InfoRow label="연락처" labelWidth={92}>{escort.clientPhone}</InfoRow>
                    <InfoRow label="병원 주소" labelWidth={92}>{escort.hospitalAddress}</InfoRow>
                    <InfoRow label="특이사항" labelWidth={92}>{escort.note}</InfoRow>
                  </div>
                </dl>
              </section>

              <MapCard stage={escort.stage} />

              <section className={cn(CARD, 'flex items-center gap-4 px-6 py-6')}>
                <Image src="/icons/escort/notice.svg" alt="" width={48} height={61} className="-my-4 -mx-2.5 shrink-0" />
                <div className="min-w-0">
                  <p className="text-xl leading-6 font-semibold text-brand">
                    {escort.stage === 'ready'
                      ? '동행이 시작되면 실시간 위치 공유가 활성화됩니다.'
                      : escort.stage === 'ongoing'
                        ? '동행 진행 상황을 실시간으로 업데이트해 주세요.'
                        : '위치 공유가 종료되었습니다.'}
                  </p>
                  <p className="mt-2 text-xs leading-4 font-medium text-brand">
                    {escort.stage === 'ready'
                      ? '동행 시작 후, 현재 위치와 이동 경로가 실시간으로 공유됩니다.'
                      : escort.stage === 'ongoing'
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
                <div className="mt-5 flex flex-col gap-1.5">
                  {/* TODO: 진행 상태 변경 API(PATCH /api/v1/applications/{id}/progress) 연결 */}
                  {escort.stage === 'done' ? (
                    <Link href={`/escort/${escort.applicationId}/report/new`} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
                      {info.primary}
                    </Link>
                  ) : (
                    <button type="button" className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
                      {info.primary}
                    </button>
                  )}
                  {escort.stage === 'done' ? (
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

              <Timeline steps={escort.timeline} />
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
