'use client';

import { useEffect, useRef, type ChangeEvent, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useSignupRole } from '../hooks/useSignupRole';
import { BANKS, REGIONS } from '../model';
import { useSignup } from '../state/SignupContext';
import type { SignupFormValues } from '../types';
import SignupStepper from './SignupStepper';
import CheckButton from './form/CheckButton';
import FormCard from './form/FormCard';
import FormField from './form/FormField';
import GenderRadioGroup from './form/GenderRadioGroup';
import RoleStatus from './form/RoleStatus';
import SelectInput from './form/SelectInput';
import StepNavButton from './form/StepNavButton';
import TextArea from './form/TextArea';
import TextInput from './form/TextInput';

const PHONE_HINT = '‘-’ 없이 숫자만 입력해주세요.';
/** 계좌 정보 입력칸은 다른 칸보다 낮습니다 (Figma 높이 47) */
const ACCOUNT_FIELD = 'h-[47px]!';
const GRID = 'mx-auto grid w-full max-w-[998px] gap-x-8 gap-y-[21px] lg:grid-cols-2';

/**
 * 회원가입 2단계(정보 입력) — Figma 공통_회원가입_의뢰인(정보 입력) 564:17746
 *                              · 공통_회원가입_동행 매니저(정보 입력) 564:17621
 *
 * 형식 검사는 브라우저 기본 검사(required · pattern · type)를 씁니다.
 */
export default function SignupInfoPage() {
  const router = useRouter();
  const role = useSignupRole();
  // 입력값은 3·4단계와 공유하고, 이전 단계로 돌아와도 유지됩니다.
  const { values, setValues } = useSignup();
  const confirmRef = useRef<HTMLInputElement>(null);
  // 계좌 정보는 선택이지만, 하나라도 적었다면 세 칸을 모두 채워야 합니다.
  const accountRequired = Boolean(values.bankName || values.accountHolder || values.accountNumber);

  const handleChange =
    (key: keyof SignupFormValues) =>
    (event: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      setValues((prev) => ({ ...prev, [key]: event.target.value }));
    };

  // 비밀번호와 확인 값이 다르면 제출을 막습니다.
  useEffect(() => {
    const mismatch = values.passwordConfirm !== '' && values.password !== values.passwordConfirm;
    confirmRef.current?.setCustomValidity(mismatch ? '비밀번호가 일치하지 않습니다.' : '');
  }, [values.password, values.passwordConfirm]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    router.push(`/signup/terms?role=${role}`);
  };

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <div className="mb-8 flex w-full justify-center lg:mb-[50px]">
            <SignupStepper current={2} />
          </div>

          <form
            onSubmit={handleSubmit}
            className="flex w-full max-w-[1066px] flex-col items-center gap-8 lg:gap-[50px]"
          >
            <section className="w-full">
              <SectionHeading
                title="기본 정보를 입력해주세요"
                description="정보 입력은 더 나은 매칭과 안전한 서비스 이용을 위해 필요합니다."
                className="mb-6"
              />
              <FormCard className="px-5 py-8 lg:px-[33px] lg:py-[39px]">
                <div className={GRID}>
                  <div className="lg:col-span-2">
                    <RoleStatus role={role} />
                  </div>

                  <FormField label="이름*" htmlFor="signup-name">
                    <TextInput
                      id="signup-name"
                      name="name"
                      autoComplete="name"
                      required
                      placeholder="이름을 입력해주세요."
                      value={values.name}
                      onChange={handleChange('name')}
                    />
                  </FormField>
                  <FormField label="전화번호*" htmlFor="signup-phone">
                    <TextInput
                      id="signup-phone"
                      name="phoneNum"
                      type="tel"
                      inputMode="numeric"
                      autoComplete="tel"
                      required
                      pattern="[0-9]{10,11}"
                      title={PHONE_HINT}
                      placeholder={PHONE_HINT}
                      value={values.phoneNum}
                      onChange={handleChange('phoneNum')}
                    />
                  </FormField>

                  <FormField label="아이디*" htmlFor="signup-username" className="lg:col-span-2">
                    <div className="flex gap-2.5 lg:max-w-[485px]">
                      <TextInput
                        id="signup-username"
                        name="username"
                        autoComplete="username"
                        required
                        pattern="(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{4,}"
                        title="영문, 숫자를 포함해 4자 이상 입력해주세요."
                        placeholder="영문, 숫자 포함 4자 이상"
                        value={values.username}
                        onChange={handleChange('username')}
                      />
                      <CheckButton />
                    </div>
                  </FormField>

                  <FormField label="비밀번호*" htmlFor="signup-password">
                    <TextInput
                      id="signup-password"
                      name="password"
                      type="password"
                      autoComplete="new-password"
                      required
                      placeholder="비밀번호를 입력해주세요."
                      value={values.password}
                      onChange={handleChange('password')}
                    />
                  </FormField>
                  <FormField label="비밀번호 확인*" htmlFor="signup-password-confirm">
                    <TextInput
                      ref={confirmRef}
                      id="signup-password-confirm"
                      name="passwordConfirm"
                      type="password"
                      autoComplete="new-password"
                      required
                      placeholder="비밀번호를 다시 입력해주세요."
                      value={values.passwordConfirm}
                      onChange={handleChange('passwordConfirm')}
                    />
                  </FormField>

                  <FormField label="이메일*" htmlFor="signup-email">
                    {/* 디자인상 이 줄은 485px 로 열(483px)보다 살짝 넓습니다 */}
                    <div className="flex gap-2.5 lg:w-[485px]">
                      <TextInput
                        id="signup-email"
                        name="email"
                        type="email"
                        autoComplete="email"
                        required
                        placeholder="이메일을 입력해주세요."
                        value={values.email}
                        onChange={handleChange('email')}
                      />
                      <CheckButton />
                    </div>
                  </FormField>
                  <FormField label="성별*" labelId="signup-gender-label" className="lg:self-center">
                    <GenderRadioGroup
                      labelId="signup-gender-label"
                      value={values.gender}
                      onChange={(gender) => setValues((prev) => ({ ...prev, gender }))}
                    />
                  </FormField>

                  <FormField label="생년월일*" htmlFor="signup-birth-date">
                    <TextInput
                      id="signup-birth-date"
                      name="birthDate"
                      inputMode="numeric"
                      autoComplete="off"
                      required
                      pattern="[0-9]{4}\.[0-9]{2}\.[0-9]{2}"
                      title="YYYY.MM.DD 형식으로 입력해주세요."
                      placeholder="YYYY.MM.DD"
                      value={values.birthDate}
                      onChange={handleChange('birthDate')}
                    />
                  </FormField>
                  <FormField label="거주 지역*" htmlFor="signup-region">
                    <SelectInput
                      id="signup-region"
                      name="region"
                      autoComplete="address-level1"
                      required
                      value={values.region}
                      onChange={handleChange('region')}
                    >
                      <option value="" disabled hidden>
                        시/도를 선택해주세요.
                      </option>
                      {REGIONS.map((region) => (
                        <option key={region} value={region} className="text-brand">
                          {region}
                        </option>
                      ))}
                    </SelectInput>
                  </FormField>
                </div>
              </FormCard>
            </section>

            {role === 'CLIENT' && (
              <section className="w-full">
                <SectionHeading
                  title="의뢰인 추가 정보"
                  description="더 안전하고 정확한 동행 매칭을 위해 추가 정보를 입력해주세요."
                  className="mb-6"
                />
                <FormCard className="px-5 py-8 lg:pt-[28px] lg:pr-[29px] lg:pb-[31px] lg:pl-[31px]">
                  <div className="grid w-full gap-x-8 gap-y-[21px] lg:grid-cols-2">
                    <FormField label="보호자 실명*" htmlFor="signup-guardian-name">
                      <TextInput
                        id="signup-guardian-name"
                        name="guardianName"
                        required
                        placeholder="보호자 이름을 입력해주세요."
                        value={values.guardianName}
                        onChange={handleChange('guardianName')}
                      />
                    </FormField>
                    <FormField label="보호자 전화번호*" htmlFor="signup-guardian-phone">
                      <TextInput
                        id="signup-guardian-phone"
                        name="guardianPhone"
                        type="tel"
                        inputMode="numeric"
                        required
                        pattern="[0-9]{10,11}"
                        title={PHONE_HINT}
                        placeholder={PHONE_HINT}
                        value={values.guardianPhone}
                        onChange={handleChange('guardianPhone')}
                      />
                    </FormField>
                    <FormField label="의뢰인 특이사항" htmlFor="signup-care-note" className="lg:col-span-2">
                      <TextArea
                        id="signup-care-note"
                        name="careNote"
                        placeholder="의뢰인의 건강상태, 주의사항, 필요한 도움 등을 자유롭게 작성해주세요."
                        value={values.careNote}
                        onChange={handleChange('careNote')}
                      />
                    </FormField>
                  </div>
                </FormCard>
              </section>
            )}

            {role === 'ESCORT' && (
              <section className="w-full">
                <SectionHeading
                  title="동행 매니저 추가 정보"
                  description="신뢰할 수 있는 매칭을 위해 추가 정보를 입력해주세요."
                  className="mb-6"
                />
                <FormCard className="flex flex-col gap-2.5 px-5 py-8 lg:pr-[30px] lg:pl-8">
                  {/* 정산받을 계좌 (선택) */}
                  <div className="flex flex-col gap-[21px] py-2.5 lg:flex-row lg:justify-between lg:gap-8">
                    <FormField label="은행" htmlFor="signup-bank" className="lg:w-[222px]">
                      <SelectInput
                        id="signup-bank"
                        name="bankName"
                        required={accountRequired}
                        value={values.bankName}
                        onChange={handleChange('bankName')}
                        className={ACCOUNT_FIELD}
                      >
                        <option value="">은행을 선택해주세요.</option>
                        {BANKS.map((bank) => (
                          <option key={bank} value={bank} className="text-brand">
                            {bank}
                          </option>
                        ))}
                      </SelectInput>
                    </FormField>
                    <FormField label="예금주" htmlFor="signup-account-holder" className="lg:w-[222px]">
                      <TextInput
                        id="signup-account-holder"
                        name="accountHolder"
                        maxLength={50}
                        required={accountRequired}
                        placeholder="예금주명을 입력해주세요."
                        value={values.accountHolder}
                        onChange={handleChange('accountHolder')}
                        className={ACCOUNT_FIELD}
                      />
                    </FormField>
                    <FormField label="계좌번호" htmlFor="signup-account-number" className="lg:w-[222px]">
                      <TextInput
                        id="signup-account-number"
                        name="accountNumber"
                        inputMode="numeric"
                        maxLength={30}
                        required={accountRequired}
                        pattern="[0-9\-]{8,30}"
                        title="숫자와 ‘-’만 입력해주세요. (8자 이상)"
                        placeholder="계좌번호를 입력해주세요."
                        value={values.accountNumber}
                        onChange={handleChange('accountNumber')}
                        className={ACCOUNT_FIELD}
                      />
                    </FormField>
                  </div>

                  <FormField label="자기소개*" htmlFor="signup-intro">
                    <TextArea
                      id="signup-intro"
                      name="intro"
                      required
                      maxLength={500}
                      placeholder={'간단한 자기소개를 입력해주세요.\n예) 경력, 보유 자격증, 성격 등'}
                      value={values.intro}
                      onChange={handleChange('intro')}
                    />
                  </FormField>
                </FormCard>
              </section>
            )}

            <div className="flex w-full gap-2.5">
              <StepNavButton href="/signup">이전</StepNavButton>
              <StepNavButton variant="solid">다음</StepNavButton>
            </div>
          </form>
        </Container>
      </section>
    </AppShell>
  );
}
