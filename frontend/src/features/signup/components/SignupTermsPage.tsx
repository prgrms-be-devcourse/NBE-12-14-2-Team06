'use client';

import { useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useSignupRole } from '../hooks/useSignupRole';
import { AGREEMENT_GROUPS } from '../model';
import { useSignup } from '../state/SignupContext';
import SignupStepper from './SignupStepper';
import StepNavButton from './form/StepNavButton';
import AgreementCard from './terms/AgreementCard';

const SECTION_TITLE = 'flex h-[30px] items-center text-xl leading-6 font-semibold text-brand lg:text-2xl';

/**
 * 회원가입 3단계(약관 동의)
 * Figma 공통_회원가입_개인정보동의(의뢰인) 564:17321 · (동행매니저) 564:17438
 *
 * 필수 항목은 체크하지 않으면 제출되지 않습니다(브라우저 기본 검사).
 */
export default function SignupTermsPage() {
  const router = useRouter();
  const role = useSignupRole();
  const { setValues } = useSignup();
  const group = AGREEMENT_GROUPS[role];

  const [checked, setChecked] = useState<Record<string, boolean>>({});
  const allRequiredChecked = group.required.every((item) => checked[item.id]);

  const handleToggle = (id: string) => (value: boolean) => {
    setChecked((prev) => ({ ...prev, [id]: value }));
  };

  // "필수 항목에 모두 동의합니다" — 선택 항목은 건드리지 않습니다.
  const handleToggleAllRequired = (value: boolean) => {
    setChecked((prev) => ({
      ...prev,
      ...Object.fromEntries(group.required.map((item) => [item.id, value])),
    }));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    // TODO: 여기서 가입 API(POST /api/v1/users)를 호출하세요. 성공하면 아래로 이동합니다.
    //       성별 "선택 안 함"·보호자 정보는 백엔드가 아직 받지 않으니 PR 설명의 표를 확인하세요.
    setValues((prev) => ({ ...prev, password: '', passwordConfirm: '' })); // 비밀번호는 더 들고 있지 않습니다.
    router.push(`/signup/complete?role=${role}`);
  };

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <div className="mb-8 flex w-full justify-center lg:mb-[50px]">
            <SignupStepper current={3} />
          </div>

          <SectionHeading
            title="개인정보 이용 동의"
            description={[
              '가치동행 서비스 이용을 위해 아래의 내용을 확인하고 동의해주세요.',
              '소중한 개인정보는 안전하게 보호되며, 서비스 제공 목적에만 사용됩니다.',
            ]}
            className="mb-8 lg:mb-[42px]"
            descriptionClassName="leading-6 lg:leading-[30px]"
          />

          <form onSubmit={handleSubmit} className="flex w-full max-w-[1066px] flex-col gap-[30px]">
            <AgreementCard
              id="agree-all-required"
              title="필수 항목에 모두 동의합니다."
              description="선택 항목은 동의하지 않아도 서비스 이용이 가능합니다."
              checked={allRequiredChecked}
              onChange={handleToggleAllRequired}
              emphasized
            />

            <section aria-labelledby="agreements-required" className="flex flex-col gap-2.5">
              <h3 id="agreements-required" className={SECTION_TITLE}>
                {group.requiredTitle}
              </h3>
              {group.required.map((item) => (
                <AgreementCard
                  key={item.id}
                  id={`agree-${item.id}`}
                  title={item.title}
                  description={item.description}
                  checked={!!checked[item.id]}
                  onChange={handleToggle(item.id)}
                  badge="required"
                  required
                />
              ))}
            </section>

            <section aria-labelledby="agreements-optional" className="flex flex-col gap-2.5">
              <h3 id="agreements-optional" className={SECTION_TITLE}>
                선택 동의 항목
              </h3>
              {group.optional.map((item) => (
                <AgreementCard
                  key={item.id}
                  id={`agree-${item.id}`}
                  title={item.title}
                  description={item.description}
                  checked={!!checked[item.id]}
                  onChange={handleToggle(item.id)}
                  badge="optional"
                />
              ))}
            </section>

            <div className="mt-5 flex w-full gap-2.5">
              <StepNavButton href={`/signup/info?role=${role}`}>이전</StepNavButton>
              <StepNavButton variant="solid">가입하기</StepNavButton>
            </div>
          </form>
        </Container>
      </section>
    </AppShell>
  );
}
