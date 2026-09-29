'use client';

import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { ROLE_OPTIONS } from '../model';
import RoleCard from './RoleCard';
import SignupStepper from './SignupStepper';

/** 회원가입 1단계(선택) — Figma 공통_회원가입(선택) 564:17880 */
export default function SignupSelectPage() {
  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <div className="mb-8 flex w-full justify-center lg:mb-[50px]">
            <SignupStepper current={1} />
          </div>

          <SectionHeading
            title="어떤 목적으로 이용하시나요?"
            description="선택하신 목적에 맞는 맞춤형 서비스와 기능을 제공합니다."
          />

          <div className="flex w-full flex-col items-center gap-[22px] lg:flex-row lg:items-stretch lg:justify-center">
            {ROLE_OPTIONS.map((option) => (
              <RoleCard key={option.role} option={option} />
            ))}
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
