'use client';

import Image from 'next/image';
import { Container, PillButton, ImagePlaceholder } from '@/components/ui';

/** Figma 564:18147 — 오늘 병원, 누구랑 가지? */
export default function HeroSection() {
  return (
    <section className="bg-white py-14 lg:py-20">
      <Container className="flex flex-col-reverse items-center gap-10 lg:flex-row">
        <div className="w-full min-w-0 lg:flex-[0_1_602px]">
          <h1 className="text-[clamp(2.5rem,6vw,4rem)] leading-[1.06] font-bold tracking-[-0.01em] text-brand lg:text-[64px] lg:leading-[66px]">
            오늘 병원,
            <br />
            누구랑 <span className="text-brand-muted">가지?</span>
          </h1>

          <p className="mt-6 text-base leading-6 text-brand">병원 동행 서비스 뮈시기 저쩌구 멘트</p>

          <div className="mt-12 flex flex-col items-stretch gap-[15px] sm:flex-row sm:items-center lg:mt-[84px]">
            <PillButton href="#steps">동행 요청하기</PillButton>
            <PillButton href="#steps" variant="ghost">
              공고 찾아보기
            </PillButton>
          </div>
        </div>

        <div className="w-full min-w-0 lg:flex-[1_1_598px]">
          <Image src="/images/hero.jpeg" alt="휠체어에 앉아 손을 든 어르신 일러스트" width={598} height={494} priority className="h-auto w-full rounded-card object-contain" />
        </div>
      </Container>
    </section>
  );
}
