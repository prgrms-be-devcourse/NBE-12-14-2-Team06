'use client';

import Image from 'next/image';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useSignupRole } from '../hooks/useSignupRole';
import { useSignup } from '../state/SignupContext';
import SignupStepper from './SignupStepper';
import StepNavButton from './form/StepNavButton';

const ROLE_LABEL = { CLIENT: '의뢰인', ESCORT: '동행 매니저' } as const;

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

          <Image
            src="/icons/complete-check.svg"
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
            {/* TODO: 로그인 화면이 생기면 주소를 확인하세요. */}
            <StepNavButton href="/login">로그인 하러 가기</StepNavButton>
            <StepNavButton href="/" variant="solid">
              메인 페이지로 가기
            </StepNavButton>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
