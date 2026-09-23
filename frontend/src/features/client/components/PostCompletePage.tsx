'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';

const BUTTON = 'flex h-14 flex-1 items-center justify-center rounded-[30px] px-6 text-xl leading-[18px] font-semibold transition-colors';

/**
 * 공고 등록 완료 — Figma 의뢰인_공고 작성 결과 562:14558
 *
 * ⚠️ 공고 번호·시급·결제 금액은 앞 화면에서 넘어온 값입니다. (공고 등록·결제 API 연결 전)
 */
export default function PostCompletePage() {
  const { loading, user } = useRequireAuth('CLIENT');
  const searchParams = useSearchParams();
  const postId = searchParams.get('postId') ?? '1';
  const pay = Number(searchParams.get('pay') ?? 14000);
  const amount = Number(searchParams.get('amount') ?? 49000);

  if (loading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  const rows = [
    { label: '공고 번호', value: postId },
    { label: '공고 상태', value: '모집 중' },
    { label: '시급', value: `${pay.toLocaleString()}원` },
    { label: '결제 금액', value: `${amount.toLocaleString()}원` },
  ];

  return (
    <AppShell>
      <section className="flex min-h-[743px] items-center bg-white pt-[50px] pb-[74px]">
        <Container className="flex flex-col items-center">
          <Image src="/icons/escort/report-done-check.svg" alt="" width={126} height={126} className="mb-[30px]" />
          <SectionHeading title="공고 등록 완료" description="공고가 등록되었습니다." className="mb-[30px]" />

          <dl className="mb-[30px] flex flex-col gap-2.5 text-xl text-brand lg:text-2xl">
            {rows.map((row) => (
              <div key={row.label} className="grid h-[45px] grid-cols-[100px_150px] items-center gap-x-10 sm:gap-x-[180px]">
                <dt className="font-semibold">{row.label}</dt>
                <dd className="font-medium">{row.value}</dd>
              </div>
            ))}
          </dl>

          <div className="flex w-full max-w-[600px] gap-2.5">
            <Link href="/" className={`${BUTTON} border border-line bg-white text-brand hover:bg-line-soft`}>
              홈페이지로
            </Link>
            <Link href={`/client/posts/${postId}`} className={`${BUTTON} bg-brand text-white hover:bg-brand-hover`}>
              공고 상세 페이지로
            </Link>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
