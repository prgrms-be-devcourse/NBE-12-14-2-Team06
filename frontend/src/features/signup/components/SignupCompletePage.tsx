'use client';

import Image from 'next/image';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useSignupRole } from '../hooks/useSignupRole';
import { useSignup } from '../state/SignupContext';
import SignupStepper from './SignupStepper';
import StepNavButton from './form/StepNavButton';

const ROLE_LABEL = { CLIENT: '의뢰인', ESCORT: '동행 매니저' } as const;

/**
 * 가입하면 바로 로그인 상태라서, 로그인 대신 역할에 맞는 첫 할 일로 보냅니다.
 * 의뢰인은 동행을 요청하는 공고를 쓰고, 동행 매니저는 지원할 공고를 찾습니다.
 */
const NEXT_STEP = {
  CLIENT: { href: '/client/posts/new', label: '공고 작성하러 가기' },
  ESCORT: { href: '/escort/posts', label: '공고 찾아보기' },
} as const;

/** 회원가입 4단계(완료) — Figma 공통_회원가입 완료 페이지 564:17555 */
export default function SignupCompletePage() {
  const role = useSignupRole();
  const { values } = useSignup();

  const rows = [
    { label: '선택 역할', value: ROLE_LABEL[role] },
    { label: '가입 아이디', value: values.username || '-' },
  ];

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <div className="mb-8 flex w-full justify-center lg:mb-[50px]">
            <SignupStepper current={4} />
          </div>

          {/* 결제 성공 화면(토스)의 파란 체크와 같은 색(#3182F6) 입니다. */}
          <Image
            src="/icons/complete-check-blue.svg"
            alt=""
            width={126}
            height={126}
            className="mb-8 lg:mb-[42px]"
          />

          <SectionHeading
            title="회원가입이 완료되었습니다."
            description="가지와 함께 더 안전하고 편리한 병원 동행 서비스를 시작해보세요."
            className="mb-8 lg:mb-11"
          />

          <dl className="mb-8 flex h-[148px] w-full max-w-[600px] flex-col items-center justify-center gap-2.5 rounded-[20px] border border-line-soft bg-white px-4 drop-shadow-soft lg:mb-[50px]">
            {rows.map((row) => (
              <div key={row.label} className="flex h-[47px] w-full items-center gap-4 lg:w-auto lg:gap-10">
                <dt className="w-28 shrink-0 px-2 text-lg leading-5 font-semibold text-brand lg:w-40 lg:px-4">
                  {row.label}
                </dt>
                <dd
                  title={row.value}
                  className="min-w-0 flex-1 truncate px-2 text-lg leading-5 font-medium text-brand lg:w-40 lg:flex-none lg:px-4"
                >
                  {row.value}
                </dd>
              </div>
            ))}
          </dl>

          <div className="flex w-full max-w-[600px] gap-2.5">
            <StepNavButton href={NEXT_STEP[role].href}>{NEXT_STEP[role].label}</StepNavButton>
            <StepNavButton href="/" variant="solid">
              메인 페이지로 가기
            </StepNavButton>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
