'use client';

import { Container, SectionHeading, ArrowRightIcon } from '@/components/ui';
import { cn } from '@/lib/cn';
import { REQUESTER_STEPS, MANAGER_STEPS } from '../model';
import type { UsageStep } from '../types';

function StepRow({ steps }: { steps: UsageStep[] }) {
  return (
    <div className="flex flex-col gap-[22px] lg:flex-row">
      {steps.map((step) => (
        <article
          key={`${step.num}-${step.title}`}
          className="flex min-w-0 flex-1 flex-col items-center gap-5 rounded-card border border-line bg-white px-6 py-[46px] text-center shadow-card"
        >
          <div className="text-[64px] leading-[60px] font-semibold text-brand">{step.num}</div>
          <h3 className="text-xl leading-7 font-extrabold text-brand">
            {step.title}
            {step.highlight && <span className="text-brand-muted">{step.highlight}</span>}
          </h3>
          <p className="text-base leading-6 text-brand">{step.description}</p>
        </article>
      ))}
    </div>
  );
}

function Cta({
  title,
  sub,
  variant,
}: {
  title: string;
  sub: string;
  variant: 'solid' | 'ghost';
}) {
  const solid = variant === 'solid';
  return (
    <a
      href="#"
      className={cn(
        'flex min-h-[112px] min-w-0 flex-1 items-center justify-between gap-6 rounded-card px-10 py-8 shadow-card transition-transform hover:-translate-y-0.5',
        solid ? 'bg-brand text-white' : 'border border-line-soft bg-white text-brand',
      )}
    >
      <span>
        <span className="block text-xl leading-6 font-semibold">{title}</span>
        <span className="mt-3 block text-base leading-[22px]">{sub}</span>
      </span>
      <span
        aria-hidden="true"
        className={cn(
          'grid size-10 shrink-0 place-items-center rounded-[30px]',
          solid ? 'bg-white text-brand' : 'bg-brand text-white',
        )}
      >
        <ArrowRightIcon className="size-[18px]" />
      </span>
    </a>
  );
}

/** Figma 564:18047 — 이렇게 이용해요 + CTA */
export default function StepSection() {
  return (
    <section id="steps" className="bg-white py-14 lg:py-20">
      <Container width="narrow">
        <SectionHeading title="이렇게 이용해요" description="간단한 3단계로, 병원 동행이 시작됩니다." />

        <div className="mb-[50px] flex flex-col gap-5">
          <StepRow steps={REQUESTER_STEPS} />
          <StepRow steps={MANAGER_STEPS} />
        </div>

        <div className="flex flex-col gap-5 lg:flex-row">
          <Cta variant="solid" title="병원 동행이 필요하신가요?" sub="동행 요청하기" />
          <Cta variant="ghost" title="고소득꿀알바 하고 싶으신가요?" sub="공고 찾아보기" />
        </div>
      </Container>
    </section>
  );
}
