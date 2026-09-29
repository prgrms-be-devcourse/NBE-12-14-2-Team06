'use client';

import Image from 'next/image';
import { useState, type FormEvent, type ReactNode } from 'react';
import { Container, SectionHeading } from '@/components/ui';
import { cn } from '@/lib/cn';
import { COMPANY, INQUIRY_CATEGORIES, OPERATING_HOURS, telHref } from '../model';

/** 입력칸 공통 모양 (Figma Input Text: 높이 61 · 라운드 20 · 그림자) */
const FIELD =
  'h-[61px] w-full rounded-[20px] border border-line-soft bg-white px-4 text-base leading-5 text-brand shadow-card placeholder:text-brand-muted';

function Field({ label, htmlFor, children }: { label: string; htmlFor: string; children: ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col gap-[9px]">
      <label htmlFor={htmlFor} className="text-xl leading-5 font-semibold text-brand">
        {label}
      </label>
      {children}
    </div>
  );
}

/**
 * 1:1 문의 남기기.
 *
 * ⚠️ 문의 접수 API 가 아직 없어 화면에서만 완료 처리합니다.
 *    TODO: POST /api/v1/support/inquiries 연결
 */
export default function InquirySection() {
  const [submitted, setSubmitted] = useState(false);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitted(true);
  }

  return (
    <section id="inquiry" className="bg-white py-14 lg:py-20">
      <Container width="narrow">
        <SectionHeading
          title="1:1 문의 남기기"
          description={[
            '남겨주신 내용은 대표 이메일로 접수되며, 영업일 기준 1일 이내에 답변드려요.',
            `급한 문의는 ${COMPANY.phone} (${OPERATING_HOURS.weekday})로 전화해주세요.`,
          ]}
        />

        <div className="mx-auto w-full max-w-[860px] rounded-[30px] border border-line bg-white px-6 py-10 shadow-card lg:px-[60px] lg:py-[50px]">
          {submitted ? (
            <div className="flex flex-col items-center gap-5 py-10 text-center">
              <Image src="/icons/complete-check.svg" alt="" width={64} height={64} />
              <h3 className="text-2xl leading-8 font-extrabold text-brand">문의가 접수되었어요</h3>
              <p className="text-base leading-6 text-brand">
                작성해주신 이메일로 답변드릴게요.
                <br />
                답변이 늦어질 경우 {COMPANY.email} 로 다시 문의해주세요.
              </p>
              <button
                type="button"
                onClick={() => setSubmitted(false)}
                className="mt-2 h-[60px] w-full max-w-[300px] rounded-[30px] border border-line bg-white text-lg font-semibold text-brand transition-colors hover:bg-line-soft"
              >
                문의 더 남기기
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-[25px]">
              <div className="grid gap-[25px] sm:grid-cols-2">
                <Field label="이름*" htmlFor="inquiry-name">
                  <input
                    id="inquiry-name"
                    name="name"
                    type="text"
                    required
                    placeholder="이름을 입력해주세요."
                    className={FIELD}
                  />
                </Field>

                <Field label="연락처*" htmlFor="inquiry-phone">
                  <input
                    id="inquiry-phone"
                    name="phone"
                    type="tel"
                    required
                    inputMode="numeric"
                    placeholder="010-0000-0000"
                    className={FIELD}
                  />
                </Field>
              </div>

              <Field label="답변받을 이메일*" htmlFor="inquiry-email">
                <input
                  id="inquiry-email"
                  name="email"
                  type="email"
                  required
                  placeholder="이메일을 입력해주세요."
                  className={FIELD}
                />
              </Field>

              <Field label="문의 유형*" htmlFor="inquiry-category">
                <div className="relative">
                  <select
                    id="inquiry-category"
                    name="category"
                    required
                    defaultValue=""
                    className={cn(FIELD, 'appearance-none pr-12 invalid:text-brand-muted')}
                  >
                    <option value="" disabled hidden>
                      문의 유형을 선택해주세요.
                    </option>
                    {INQUIRY_CATEGORIES.map((category) => (
                      <option key={category.value} value={category.value} className="text-brand">
                        {category.label}
                      </option>
                    ))}
                  </select>
                  <Image
                    src="/icons/select-chevron.svg"
                    alt=""
                    width={13}
                    height={7}
                    className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2"
                  />
                </div>
              </Field>

              <Field label="문의 내용*" htmlFor="inquiry-message">
                <textarea
                  id="inquiry-message"
                  name="message"
                  required
                  maxLength={1000}
                  placeholder="언제, 어떤 화면에서, 어떤 문제가 있었는지 적어주시면 더 빠르게 도와드릴 수 있어요."
                  className="h-[200px] w-full resize-none rounded-[20px] border border-line-soft bg-white p-4 text-base leading-6 text-brand shadow-card placeholder:text-brand-muted"
                />
              </Field>

              <label className="flex items-start gap-2.5 text-base leading-6 text-brand">
                <input
                  type="checkbox"
                  name="privacy"
                  required
                  className="mt-0.5 size-5 shrink-0 accent-[#6d758f]"
                />
                <span>
                  문의 처리를 위해 이름·연락처·이메일 수집에 동의합니다. (필수)
                  <br />
                  <span className="text-sm text-brand-muted">
                    수집한 정보는 문의 답변 목적으로만 사용하고, 답변 완료 후 3개월 뒤 파기합니다.
                  </span>
                </span>
              </label>

              <button
                type="submit"
                className="mx-auto h-[60px] w-full max-w-[300px] rounded-[30px] bg-brand text-lg font-semibold text-white transition-colors hover:bg-brand-hover"
              >
                문의 보내기
              </button>

              <p className="text-center text-sm leading-5 text-brand-muted">
                전화 상담을 원하시면{' '}
                <a href={telHref(COMPANY.phone)} className="font-semibold text-brand underline-offset-4 hover:underline">
                  {COMPANY.phone}
                </a>
                {' · '}
                메일로 보내시려면{' '}
                <a href={`mailto:${COMPANY.email}`} className="font-semibold text-brand underline-offset-4 hover:underline">
                  {COMPANY.email}
                </a>
              </p>
            </form>
          )}
        </div>
      </Container>
    </section>
  );
}
