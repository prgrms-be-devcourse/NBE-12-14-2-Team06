'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { StatusLabel } from '@/features/post';
import { aiSummaryItems, fetchReport, isReportNotFoundError, parseAiSummary, type ReportDto } from '@/features/report';
import { cn } from '@/lib/cn';
import { formatDateTime } from '../lib/date';
import { getEscortCase } from '../model/cases';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-[35px]';
const TITLE = 'text-2xl leading-6 font-semibold text-brand';
const MENU_BUTTON = 'flex h-[45px] w-full items-center justify-center gap-2.5 rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft';
const MUTED_NOTICE = 'text-sm leading-6 font-medium text-brand-muted';

type ReportResult =
  | { status: 'notFound' }
  | { status: 'error'; message: string }
  | { status: 'ready'; report: ReportDto };

/** 여러 줄 텍스트를 InfoRow 안에서 줄 단위로 보여줍니다. */
function MultilineText({ text }: { text: string }) {
  return (
    <>
      {text.split('\n').filter((line) => line.trim() !== '').map((line, index) => (
        <span key={index} className="block">{line}</span>
      ))}
    </>
  );
}

/**
 * 동행 보고서 상세 — Figma 동행 매니저_보고서 조회 276:845
 *
 * 진료 내용은 GET /api/v1/applications/{applicationId}/report 로 가져옵니다.
 * 공고 정보·동행인 정보는 아직 연결 전이라 모의 데이터(model/cases.ts)를 그대로 씁니다.
 */
export default function ReportDetailPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const { applicationId } = useParams<{ applicationId: string }>();
  const escort = getEscortCase(Number(applicationId));
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

  if (!escort) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</p>
          <Link href="/mypage/applications" className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>신청 목록으로</Link>
        </section>
      </AppShell>
    );
  }

  if (!state) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">보고서를 불러오는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (state.status === 'notFound') {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">아직 보고서가 작성되지 않았습니다.</p>
          <Link href={`/escort/${escort.applicationId}`} className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>동행 내역으로 돌아가기</Link>
        </section>
      </AppShell>
    );
  }

  if (state.status === 'error') {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p role="alert" className="text-xl font-semibold text-brand">{state.message}</p>
          <Link href={`/escort/${escort.applicationId}`} className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>동행 내역으로 돌아가기</Link>
        </section>
      </AppShell>
    );
  }

  const report = state.report;
  const aiSummary = parseAiSummary(report.aiSummary);
  const summaryItems = aiSummary ? aiSummaryItems(aiSummary) : [];

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading title="동행 보고서 상세" description="작성한 동행 보고서의 내용을 확인할 수 있습니다." className="mb-6" />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,858px)_minmax(0,396px)] lg:justify-center">
            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className={cn(CARD, 'lg:px-6')}>
                <h2 className={cn(TITLE, 'mb-[21px]')}>기본 정보</h2>
                <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="공고 제목" labelWidth={92}>{escort.title}</InfoRow>
                    <InfoRow label="병원명" labelWidth={92}>{escort.hospitalName}</InfoRow>
                    <InfoRow label="동행일" labelWidth={92}>{escort.dateLabel}</InfoRow>
                    <InfoRow label="동행시간" labelWidth={92}>{escort.workTime}</InfoRow>
                  </div>
                  <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-40" />
                  <div className="flex flex-col gap-[3px]">
                    <InfoRow label="의뢰인명" labelWidth={92}>{escort.clientName}</InfoRow>
                    <InfoRow label="병원 주소" labelWidth={92}>{escort.hospitalAddress}</InfoRow>
                    <InfoRow label="특이사항" labelWidth={92}>{escort.note}</InfoRow>
                  </div>
                </dl>
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-[21px]')}>진료 내용</h2>
                <dl className="flex flex-col gap-[5px]">
                  <InfoRow label="진료 과목" labelWidth={132}>{report.department}</InfoRow>
                  <InfoRow label="진료 목적" labelWidth={132}>{report.purpose}</InfoRow>
                  <InfoRow label="진료 내용 요약" labelWidth={132} className="items-start">
                    <MultilineText text={report.originContent} />
                  </InfoRow>
                </dl>
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-[21px]')}>AI 요약</h2>
                {summaryItems.length > 0 ? (
                  <dl className="flex flex-col gap-[5px]">
                    {summaryItems.map((item) => (
                      <InfoRow key={item.label} label={item.label} labelWidth={132} className="items-start">
                        {item.value}
                      </InfoRow>
                    ))}
                  </dl>
                ) : (
                  <p className={MUTED_NOTICE}>AI 요약을 준비 중입니다.</p>
                )}
              </section>

              {report.notes && (
                <section className="rounded-[30px] border border-line bg-white px-6 py-7 shadow-card lg:px-[35px]">
                  <h2 className={cn(TITLE, 'mb-5')}>특이사항</h2>
                  <p className="text-sm leading-6 font-medium text-brand">
                    <MultilineText text={report.notes} />
                  </p>
                </section>
              )}

              <section className="rounded-[30px] border border-line bg-white px-6 py-7 shadow-card lg:pr-6 lg:pl-[35px]">
                <h2 className={cn(TITLE, 'mb-[15px]')}>제출 정보</h2>
                <dl className="grid gap-x-[22px] lg:grid-cols-[373px_1px_1fr] lg:items-center">
                  <InfoRow label="작성자" labelWidth={106} style={{ minHeight: 47 }}>{user.name}(동행 매니저)</InfoRow>
                  <div aria-hidden="true" className="hidden h-[30px] bg-[#e6e8ec] opacity-50 lg:block" />
                  <InfoRow label="제출일" labelWidth={106}>{formatDateTime(report.createdAt)}</InfoRow>
                </dl>
              </section>
            </div>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className="rounded-[30px] border border-line bg-white px-5 pt-7 pb-7 shadow-card">
                <div className="mb-5 flex items-center justify-between gap-2">
                  <h2 className={TITLE}>제출 상태</h2>
                  <StatusLabel tone="strong" size="large">완료</StatusLabel>
                </div>
                <dl className="flex flex-col gap-[3px]">
                  <InfoRow label="제출일" labelWidth={94}>{formatDateTime(report.createdAt)}</InfoRow>
                </dl>
                <div className="mt-[3px] rounded-[30px] border border-line bg-line-soft px-6 py-6 text-sm leading-6 font-semibold text-brand">
                  <p>보고서가 정상적으로 제출되었습니다.</p>
                  <p>의뢰인이 확인한 후 서비스가 완료 처리됩니다.</p>
                </div>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-5 py-8 shadow-card">
                <h2 className={cn(TITLE, 'mb-4')}>관련 메뉴</h2>
                <div className="flex flex-col gap-2.5">
                  <Link href={`/escort/${escort.applicationId}`} className={MENU_BUTTON}>
                    <Image src="/icons/escort/back-arrow.svg" alt="" width={11.3} height={9.3} />
                    동행 내역으로 돌아가기
                  </Link>
                  {/* TODO: 메시지 기능 연결 */}
                  <button type="button" className={MENU_BUTTON}>
                    <Image src="/icons/escort/chat.svg" alt="" width={14.3} height={12.3} />
                    의뢰인에게 메시지 보내기
                  </button>
                </div>
              </section>
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
