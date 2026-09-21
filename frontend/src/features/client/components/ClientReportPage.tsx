'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';
import { STAGE_VIEW, getClientEscortCase } from '../model/escort';
import ManagerInfoCard from './ManagerInfoCard';
import TripSummary from './TripSummary';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-[35px]';
const TITLE = 'text-2xl leading-6 font-semibold text-brand';
const MENU_BUTTON = 'flex h-11 w-full items-center justify-center gap-2.5 rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft';

/**
 * 진료 보고서 (의뢰인이 보는 화면) — Figma 의뢰인_보고서 조회 459:3004
 *
 * ⚠️ 모의 데이터(model/escort.ts)를 보여줍니다. 보고서 조회·AI 요약·PDF 저장 API 는 아직 없습니다.
 */
export default function ClientReportPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const escort = getClientEscortCase(Number(applicationId));
  const report = escort?.report;

  if (!escort || !report) {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">제출된 보고서가 없습니다.</p>
          <Link href="/client/posts" className={cn(MENU_BUTTON, 'mx-auto mt-8 h-14 w-60')}>
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

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
                    {report.summary.map((line) => (
                      <span key={line} className="block">{line}</span>
                    ))}
                  </InfoRow>
                </dl>
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-[30px]')}>AI 요약</h2>
                <ul className="flex flex-col rounded-[30px] border border-line bg-line-soft px-[30px] py-5">
                  {report.aiSummary.map((line) => (
                    <li key={line} className="flex min-h-[41px] items-center gap-[19px] text-base leading-[30px] font-semibold text-brand">
                      <Image src="/icons/escort/bullet.svg" alt="" width={7.5} height={7.5} className="shrink-0" />
                      {line}
                    </li>
                  ))}
                </ul>
              </section>

              <section className={CARD}>
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
                  <button type="button" className="flex h-[38px] items-center gap-2.5 rounded-[30px] border border-[#e6e8ec] bg-line-soft px-6 text-base leading-[22px] font-semibold text-brand transition-colors hover:bg-line">
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
            </div>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <ManagerInfoCard manager={escort.manager} size="sm" title="동행 매니저 정보" />

              <section className="rounded-[30px] border border-line bg-white px-5 py-8 shadow-card">
                <h2 className={cn(TITLE, 'mb-6')}>관련 메뉴</h2>
                <div className="flex flex-col gap-2.5">
                  <Link href={`/client/escort/${escort.applicationId}/review`} className={cn(MENU_BUTTON, 'border-brand bg-brand text-white hover:bg-brand-hover')}>
                    리뷰 작성하기
                  </Link>
                  {/* TODO: PDF 저장 연결 */}
                  <button type="button" className={MENU_BUTTON}>
                    <Image src="/icons/escort/download.svg" alt="" width={12} height={12} />
                    PDF 저장
                  </button>
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
