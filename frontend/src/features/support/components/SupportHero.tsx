'use client';

import { Container, MailIcon, PhoneIcon } from '@/components/ui';
import { COMPANY, OPERATING_HOURS, telHref } from '../model';

const CONTACT_PILL =
  'inline-flex items-center gap-2.5 rounded-pill border border-line bg-white px-6 py-4 text-lg leading-[18px] font-bold text-brand shadow-card transition-colors hover:bg-line-soft';

/** 고객센터 상단 — 대표 전화·대표 이메일을 가장 먼저 보여줍니다. */
export default function SupportHero() {
  return (
    <section className="bg-line-soft py-14 lg:py-20">
      <Container className="flex flex-col items-center gap-6 text-center">
        <span className="rounded-pill bg-white px-5 py-2 text-base leading-[18px] font-semibold text-brand-muted">
          고객센터
        </span>

        <h1 className="text-[clamp(2rem,5vw,3rem)] leading-tight font-extrabold text-brand">
          무엇을 도와드릴까요?
        </h1>

        <p className="text-base leading-6 text-brand lg:text-xl lg:leading-7">
          동행 예약부터 결제까지, 궁금한 점이 있으면 언제든 알려주세요.
          <br />
          {OPERATING_HOURS.weekday} 상담원이 직접 답변드립니다.
        </p>

        <div className="mt-4 flex w-full flex-col items-stretch gap-[15px] sm:w-auto sm:flex-row sm:items-center">
          <a href={telHref(COMPANY.phone)} className={`${CONTACT_PILL} justify-center`}>
            <PhoneIcon className="size-5 shrink-0" />
            {COMPANY.phone}
          </a>
          <a href={`mailto:${COMPANY.email}`} className={`${CONTACT_PILL} justify-center`}>
            <MailIcon className="size-5 shrink-0" />
            {COMPANY.email}
          </a>
        </div>
      </Container>
    </section>
  );
}
