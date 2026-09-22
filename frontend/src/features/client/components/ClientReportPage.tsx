'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { aiSummaryItems, fetchReport, isReportNotFoundError, parseAiSummary, type ReportDto } from '@/features/report';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';
import { STAGE_VIEW, getClientEscortCase } from '../model/escort';
import ManagerInfoCard from './ManagerInfoCard';
import TripSummary from './TripSummary';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-[35px]';
const TITLE = 'text-2xl leading-6 font-semibold text-brand';
const MENU_BUTTON = 'flex h-11 w-full items-center justify-center gap-2.5 rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft';

type ReportResult =
  | { status: 'notFound' }
  | { status: 'error'; message: string }
  | { status: 'ready'; report: ReportDto };

/** 줄바꿈이 있는 텍스트를 줄 단위 배열로 바꿉니다. */
function lines(text: string): string[] {
  return text.split('\n').filter((line) => line.trim() !== '');
}

/**
 * 진료 보고서 (의뢰인이 보는 화면) — Figma 의뢰인_보고서 조회 459:3004
 *
 * 진료 내용은 GET /api/v1/applications/{applicationId}/report 로 가져옵니다(동행인 화면과 같은 API).
 * 동행 정보·매니저 정보는 아직 연결 전이라 모의 데이터(model/escort.ts)를 그대로 씁니다.
 */
export default function ClientReportPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const escort = getClientEscortCase(Number(applicationId));
  const targetApplicationId = escort?.applicationId;

  const [result, setResult] = useState<{ key?: number; data?: ReportResult }>({});

  useEffect(() => {
    if (targetApplicationId === undefined) return;
    let ignore = false;

    fetchReport(targetApplicationId)
      .then((report) => {
        if (!ignore) setResult({ key: targetApplicationId, data: { status: 'ready', report } });
      })
      .catch((error: unknown) => {
        if (ignore) return;
        setResult({
          key: targetApplicationId,
          data: isReportNotFoundError(error)
            ? { status: 'notFound' }
            : { status: 'error', message: error instanceof Error ? error.message : '보고서를 불러오지 못했습니다.' },
        });
      });

    return () => {
      ignore = true;
    };
  }, [targetApplicationId]);

  const state = result.key === targetApplicationId ? result.data : undefined;

  if (!escort) {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</p>
          <Link href="/client/posts" className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  if (!state) {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">보고서를 불러오는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (state.status === 'notFound') {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">아직 보고서가 작성되지 않았습니다.</p>
          <Link href={`/client/escort/${escort.applicationId}`} className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>
            동행 현황으로 돌아가기
          </Link>
        </section>
      </AppShell>
    );
  }

  if (state.status === 'error') {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p role="alert" className="text-xl font-semibold text-brand">{state.message}</p>
          <Link href={`/client/escort/${escort.applicationId}`} className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>
            동행 현황으로 돌아가기
          </Link>
        </section>
      </AppShell>
    );
  }

  const report = state.report;
  const aiSummary = parseAiSummary(report.aiSummary);
  const summaryItems = aiSummary ? aiSummaryItems(aiSummary) : [];

  return (
    <AppShell user={MOCK_CLIENT}>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading title="진료 보고서" description="동행이 완료된 후 작성된 진료 내용을 확인할 수 있어요." className="mb-6" />

          <TripSummary
            badge={STAGE_VIEW.done.badge}
            title={escort.title}
            hospitalName={escort.hospitalName}
            region={escort.region}
            scheduleLabel={escort.scheduleLabel}
            durationLabel={escort.durationLabel}
            payLabel={escort.payLabel}
            detailHref={`/client/posts/${escort.postId}`}
          />

          <div className="mt-[22px] grid items-start gap-[22px] lg:grid-cols-[minmax(0,858px)_minmax(0,396px)] lg:justify-center">
            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-[25px]')}>진료 내용</h2>
                <dl className="flex flex-col gap-[5px]">
                  <InfoRow label="진료 과목" labelWidth={132}>{report.department}</InfoRow>
                  <InfoRow label="진료 목적" labelWidth={132}>{report.purpose}</InfoRow>
                  <InfoRow label="진료 내용 요약" labelWidth={132} className="items-start">
                    {lines(report.originContent).map((line, index) => (
                      <span key={index} className="block">{line}</span>
                    ))}
                  </InfoRow>
                </dl>
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-[30px]')}>AI 요약</h2>
                <ul className="flex flex-col rounded-[30px] border border-line bg-line-soft px-[30px] py-5">
                  {summaryItems.length > 0 ? (
                    summaryItems.map((item) => (
                      <li key={item.label} className="flex min-h-[41px] items-center gap-[19px] text-base leading-[30px] font-semibold text-brand">
                        <Image src="/icons/escort/bullet.svg" alt="" width={7.5} height={7.5} className="shrink-0" />
                        {item.label}: {item.value}
                      </li>
                    ))
                  ) : (
                    <li className="flex min-h-[41px] items-center gap-[19px] text-base leading-[30px] font-semibold text-brand-muted">
                      AI 요약을 준비 중입니다.
                    </li>
                  )}
                </ul>
              </section>

              {report.notes && (
                <section className={CARD}>
                  <h2 className={cn(TITLE, 'mb-5')}>특이사항</h2>
                  <p className="text-sm leading-6 font-medium text-brand">
                    {lines(report.notes).map((line, index) => (
                      <span key={index} className="block">{line}</span>
                    ))}
                  </p>
                </section>
              )}
            </div>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <ManagerInfoCard manager={escort.manager} size="sm" title="동행 매니저 정보" />

              <section className="rounded-[30px] border border-line bg-white px-5 py-8 shadow-card">
                <h2 className={cn(TITLE, 'mb-6')}>관련 메뉴</h2>
                <div className="flex flex-col gap-2.5">
                  <Link href={`/client/escort/${escort.applicationId}/review`} className={cn(MENU_BUTTON, 'border-brand bg-brand text-white hover:bg-brand-hover')}>
                    리뷰 작성하기
                  </Link>
                  <Link href="/client/posts" className={MENU_BUTTON}>
                    <Image src="/icons/client/list.svg" alt="" width={10} height={8} />내 공고 보기
                  </Link>
                </div>
              </section>
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
