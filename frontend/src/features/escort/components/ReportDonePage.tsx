'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { MOCK_USER } from '@/lib/mockSession';

const BUTTON = 'flex h-14 flex-1 items-center justify-center rounded-[30px] px-6 text-xl leading-[18px] font-semibold transition-colors';

/** 동행 보고서 제출 완료 — Figma 동행 매니저_보고서 작성 완료 276:844 */
export default function ReportDonePage() {
  const { applicationId } = useParams<{ applicationId: string }>();

  return (
    <AppShell user={MOCK_USER}>
      <section className="flex min-h-[743px] items-center bg-white pt-[50px] pb-[74px]">
        <Container className="flex flex-col items-center">
          <Image src="/icons/escort/report-done-check.svg" alt="" width={126} height={126} className="mb-8 lg:mb-[42px]" />
          <SectionHeading
            title="보고서가 정상적으로 제출되었습니다."
            description={[
              '소중한 동행 기록을 남겨주셔서 감사합니다.',
              '의뢰인에게 보고서가 전달되었으며, 검토 후 서비스가 완료 처리됩니다.',
            ]}
            className="mb-8 lg:mb-11"
          />
          <div className="flex w-full max-w-[600px] gap-2.5">
            <Link href={`/escort/${applicationId}/report`} className={`${BUTTON} border border-line bg-white text-brand hover:bg-line-soft`}>
              내 보고서 보기
            </Link>
            <Link href={`/escort/${applicationId}`} className={`${BUTTON} bg-brand text-white hover:bg-brand-hover`}>
              동행 현황으로 돌아가기
            </Link>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
