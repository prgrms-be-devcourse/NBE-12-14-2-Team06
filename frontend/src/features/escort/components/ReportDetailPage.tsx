'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { StatusLabel } from '@/features/post';
import { cn } from '@/lib/cn';
import { MOCK_USER } from '@/lib/mockSession';
import { getEscortCase } from '../model/cases';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-[35px]';
const TITLE = 'text-2xl leading-6 font-semibold text-brand';
const MENU_BUTTON = 'flex h-[45px] w-full items-center justify-center gap-2.5 rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft';

/**
 * 동행 보고서 상세 — Figma 동행 매니저_보고서 조회 276:845
 *
 * ⚠️ 모의 데이터(model/cases.ts)를 보여줍니다. 보고서 조회 API 연결 전입니다.
 */
export default function ReportDetailPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const escort = getEscortCase(Number(applicationId));
  const report = escort?.report;

  if (!escort || !report) {
    return (
      <AppShell user={MOCK_USER}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">제출한 보고서가 없습니다.</p>
          <Link href="/mypage/applications" className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>신청 목록으로</Link>
        </section>
      </AppShell>
    );
  }

  return (
    <AppShell user={MOCK_USER}>
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
                    {report.summary.map((line) => (
                      <span key={line} className="block">{line}</span>
                    ))}
                  </InfoRow>
                </dl>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-6 py-7 shadow-card lg:px-[35px]">
                <h2 className={cn(TITLE, 'mb-5')}>특이사항</h2>
                <p className="text-sm leading-6 font-medium text-brand">
                  {report.notes.map((line) => (
                    <span key={line} className="block">{line}</span>
                  ))}
                </p>
              </section>

              <section className={CARD}>
                <div className="mb-[30px] flex items-center justify-between gap-2">
                  <h2 className={TITLE}>첨부사진({report.photoCount}장)</h2>
                  {/* TODO: 첨부 파일 다운로드 연결 */}
                  <button type="button" className="flex h-[38px] items-center gap-2.5 rounded-[30px] border border-[#e6e8ec] bg-[#f5f5f5] px-6 text-base leading-[22px] font-semibold text-brand transition-colors hover:bg-line">
                    <Image src="/icons/escort/download.svg" alt="" width={16} height={16} className="size-3.5" />
                    전체 다운로드
                  </button>
                </div>
                <ul className="grid grid-cols-2 gap-[15px] sm:grid-cols-4">
                  {Array.from({ length: report.photoCount }, (_, index) => (
                    <li key={index} className="grid aspect-[186/137] place-items-center rounded-[22px] bg-[#e6e8ec]">
                      <Image src="/icons/escort/photo-icon.svg" alt={`첨부사진 ${index + 1}`} width={41} height={36} />
                    </li>
                  ))}
                </ul>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-6 py-7 shadow-card lg:pr-6 lg:pl-[35px]">
                <h2 className={cn(TITLE, 'mb-[15px]')}>제출 정보</h2>
                <dl className="grid gap-x-[22px] lg:grid-cols-[373px_1px_1fr] lg:items-center">
                  <InfoRow label="작성자" labelWidth={106} style={{ minHeight: 47 }}>{report.writer}</InfoRow>
                  <div aria-hidden="true" className="hidden h-[30px] bg-[#e6e8ec] opacity-50 lg:block" />
                  <InfoRow label="제출일" labelWidth={106}>{report.submittedAt}</InfoRow>
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
                  <InfoRow label="제출일" labelWidth={94}>{report.submittedAt}</InfoRow>
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
