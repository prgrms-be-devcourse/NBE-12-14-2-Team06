'use client';

import { useEffect, useRef, useState, type ChangeEvent, type FormEvent, type MouseEvent } from 'react';
import { useRouter } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { useAuth } from '@/features/auth';
import { ApiError } from '@/lib/api';
import { checkEmailAvailable, checkUsernameAvailable, createClientProfile, createEscortProfile, signUp } from '../api';
import { useDuplicateCheck, type DuplicateCheckMessage } from '../hooks/useDuplicateCheck';
import { useSignupRole } from '../hooks/useSignupRole';
import { AGREEMENT_GROUPS, BANKS, REGIONS } from '../model';
import { useSignup } from '../state/SignupContext';
import type { SignupFormValues, SignupRole } from '../types';
import SignupStepper from './SignupStepper';
import CheckButton from './form/CheckButton';
import FormCard from './form/FormCard';
import FormField from './form/FormField';
import GenderRadioGroup from './form/GenderRadioGroup';
import RoleSwitch from './form/RoleSwitch';
import SelectInput from './form/SelectInput';
import StepNavButton from './form/StepNavButton';
import TextArea from './form/TextArea';
import TextInput from './form/TextInput';

const PHONE_HINT = '‘-’ 없이 숫자만 입력해주세요.';
/** 계좌 정보 입력칸은 다른 칸보다 낮습니다 (Figma 높이 47) */
const ACCOUNT_FIELD = 'h-[47px]!';
/**
 * 생년월일 달력에서 고를 수 있는 마지막 날 = 어제. (백엔드 @Past 는 오늘을 받지 않습니다)
 * sv-SE 로캘이 달력 입력과 같은 YYYY-MM-DD 형식을 주고, UTC 가 아닌 현지 시간 기준이라 날짜가 밀리지 않습니다.
 */
const LAST_BIRTH_DATE = (() => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return date.toLocaleDateString('sv-SE');
})();
const GRID = 'mx-auto grid w-full max-w-[998px] gap-x-8 gap-y-[21px] lg:grid-cols-2';
const ERROR_TEXT = 'px-4 text-sm leading-5 font-medium text-[#b91d1d]';

type FieldKey = keyof SignupFormValues;

/** 가입(POST /api/v1/users)으로 보내는 칸. 계정이 만들어진 뒤에는 고쳐도 반영되지 않아 잠급니다. */
const ACCOUNT_FIELDS = new Set<FieldKey>([
  'name',
  'phoneNum',
  'username',
  'password',
  'passwordConfirm',
  'email',
  'gender',
  'birthDate',
  'region',
]);
/**
 * 자유 입력 필수 칸. 브라우저 required 는 공백만 쳐도 통과시키지만 백엔드 @NotBlank 는 거절합니다.
 * 특히 프로필 칸(보호자 실명 등)은 계정이 먼저 만들어진 뒤에 거절되므로, 보내기 전에 여기서 막습니다.
 * (아이디·이메일·전화번호·계좌번호는 pattern/type 이 공백을 막고, 선택 칸은 공백을 고를 수 없습니다)
 */
const BLANK_CHECK_FIELDS: Record<SignupRole, FieldKey[]> = {
  CLIENT: ['name', 'password', 'passwordConfirm', 'guardianName'],
  ESCORT: ['name', 'password', 'passwordConfirm', 'accountHolder', 'intro'],
};

/** 가입 요청이 거절된 입력칸과 그 사유. 해당 칸 아래에 보여 주고 그 칸으로 포커스를 옮깁니다. */
type FieldError = { key: FieldKey; text: string };

/** 중복(409-n) 에러코드 → 입력칸 */
const DUPLICATE_FIELDS: Record<string, FieldKey> = {
  '409-1': 'username',
  '409-2': 'email',
  '409-3': 'phoneNum',
};

/** 백엔드 DTO 필드 이름 → 입력칸. 나머지(username · birthDate · intro 등)는 이름이 같습니다. */
const DTO_FIELDS: Record<string, FieldKey> = {
  emergencyContactName: 'guardianName',
  emergencyContactPhone: 'guardianPhone',
};

/**
 * 가입 실패를 입력칸 에러로 바꿉니다. 어느 칸 문제인지 모르면 null.
 * - 409-1·2·3 : 아이디·이메일·전화번호 중복
 * - 400-1     : DTO 검증 실패. 백엔드가 "birthDate: 생년월일은 과거 날짜여야 합니다., ..." 형태로 주므로 첫 항목을 씁니다.
 */
function toFieldError(error: unknown, values: SignupFormValues): FieldError | null {
  if (!(error instanceof ApiError)) return null;

  const duplicate = DUPLICATE_FIELDS[error.statusCode];
  if (duplicate) return { key: duplicate, text: error.message };

  if (error.statusCode === '400-1') {
    const match = /^(\w+): ([^,]+)/.exec(error.message);
    const key = match && (DTO_FIELDS[match[1]] ?? match[1]);
    if (key && key in values) return { key: key as FieldKey, text: match[2] };
  }
  return null;
}

/** 가입 요청이 거절된 사유 한 줄 (빨간색) */
function FieldErrorMessage({ error, field }: { error: FieldError | null; field: FieldKey }) {
  if (error?.key !== field) return null;
  return (
    <p role="alert" className={ERROR_TEXT}>
      {error.text}
    </p>
  );
}

/** 중복 확인 결과 한 줄 (통과는 차분한 색, 실패는 빨간색) */
function CheckMessage({ message }: { message: DuplicateCheckMessage | null }) {
  if (!message) return null;
  return (
    <p
      role={message.ok ? 'status' : 'alert'}
      className={`px-4 text-sm leading-5 font-medium ${message.ok ? 'text-brand' : 'text-[#b91d1d]'}`}
    >
      {message.text}
    </p>
  );
}

/**
 * 회원가입 3단계(정보 입력) — Figma 공통_회원가입_의뢰인(정보 입력) 564:17746
 *                              · 공통_회원가입_동행 매니저(정보 입력) 564:17621
 *
 * 형식 검사는 브라우저 기본 검사(required · pattern · type)를 씁니다.
 * "가입하기"를 누르면 여기서 가입을 요청합니다. 전화번호 중복처럼 서버만 아는 문제로 거절되면
 * 입력값을 그대로 둔 채 문제 칸 아래에 사유를 보여 주고 그 칸으로 포커스를 옮깁니다.
 */
export default function SignupInfoPage() {
  const router = useRouter();
  const { reload } = useAuth();
  const role = useSignupRole();
  // 입력값은 다른 단계와 공유하고, 이전 단계로 돌아와도 유지됩니다.
  const { values, setValues, verified, setVerified, agreements, createdRole, setCreatedRole } = useSignup();
  const formRef = useRef<HTMLFormElement>(null);
  const confirmRef = useRef<HTMLInputElement>(null);
  const usernameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);

  // 아이디·이메일은 중복 확인을 통과해야 다음 단계로 넘어갑니다.
  const usernameCheck = useDuplicateCheck({
    inputRef: usernameRef,
    value: values.username,
    verifiedValue: verified.username,
    onVerified: (username) => setVerified((prev) => ({ ...prev, username })),
    check: checkUsernameAvailable,
    label: '아이디',
  });
  const emailCheck = useDuplicateCheck({
    inputRef: emailRef,
    value: values.email,
    verifiedValue: verified.email,
    onVerified: (email) => setVerified((prev) => ({ ...prev, email })),
    check: checkEmailAvailable,
    label: '이메일',
  });

  const [submitting, setSubmitting] = useState(false);
  const [fieldError, setFieldError] = useState<FieldError | null>(null);
  /** 어느 칸 문제인지 알 수 없는 실패 (네트워크 오류 등) */
  const [submitError, setSubmitError] = useState('');

  // 필수 약관에 동의하지 않았으면(주소로 바로 들어왔거나 유형을 바꾼 경우) 약관 단계로 돌려보냅니다.
  const agreed = AGREEMENT_GROUPS[role].required.every((item) => agreements[item.id]);
  // 이미 계정이 만들어졌으면 그 계정의 유형 화면만 씁니다.
  // (역할 선택 화면으로 돌아가 다른 유형 카드를 눌러도 여기로 되돌아옵니다)
  const roleMismatch = createdRole !== null && createdRole !== role;
  useEffect(() => {
    if (roleMismatch) router.replace(`/signup/info?role=${createdRole}`);
    else if (!agreed) router.replace(`/signup/terms?role=${role}`);
  }, [roleMismatch, createdRole, agreed, role, router]);

  // 거절된 칸으로 포커스를 옮기고 화면 가운데로 스크롤합니다. (성별 라디오는 첫 번째 버튼)
  useEffect(() => {
    if (!fieldError) return;
    const input = formRef.current?.querySelector<HTMLElement>(`[name="${fieldError.key}"]`);
    input?.focus({ preventScroll: true });
    input?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, [fieldError]);

  // 계정만 만들어지고 프로필이 실패한 상태. 기본 정보 칸은 모양 그대로 두고 값만 바뀌지 않게 막습니다.
  const accountCreated = createdRole !== null;

  const handleChange =
    (key: keyof SignupFormValues) =>
    (event: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      if (accountCreated && ACCOUNT_FIELDS.has(key)) return;
      setValues((prev) => ({ ...prev, [key]: event.target.value }));
      // 거절된 칸을 고치기 시작하면 사유를 지웁니다.
      if (fieldError?.key === key) setFieldError(null);
    };

  // 비밀번호와 확인 값이 다르면 제출을 막습니다.
  useEffect(() => {
    const mismatch = values.passwordConfirm !== '' && values.password !== values.passwordConfirm;
    confirmRef.current?.setCustomValidity(mismatch ? '비밀번호가 일치하지 않습니다.' : '');
  }, [values.password, values.passwordConfirm]);

  const unverified = [!usernameCheck.verified && '아이디', !emailCheck.verified && '이메일'].filter(
    (label) => label !== false,
  );

  // 중복 확인이 남아 있으면 "다음"은 흐리게 보이고, 누르면 팝업으로 알려 줍니다.
  // 브라우저 기본 검사보다 먼저 돌아야 해서 제출(onSubmit)이 아니라 클릭에서 막습니다.
  const handleNextClick = (event: MouseEvent<HTMLButtonElement>) => {
    if (unverified.length === 0) return;
    event.preventDefault();
    window.alert(`${unverified.join(', ')} 중복 확인을 해주세요.`);
    (usernameCheck.verified ? emailRef : usernameRef).current?.focus();
  };

  /** 거절 사유를 붙일 칸이 지금 화면에 있는지. 없으면 버튼 위 한 줄로 보여 줍니다. */
  const hasField = (key: FieldKey) => !!formRef.current?.querySelector(`[name="${key}"]`);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setFieldError(null);
    setSubmitError('');

    const blank = BLANK_CHECK_FIELDS[role].find((key) => values[key].trim() === '');
    if (blank) {
      setFieldError({ key: blank, text: '공백만으로는 입력할 수 없습니다.' });
      return;
    }

    setSubmitting(true);
    // 계정이 이미 있으면 그 역할로 프로필만 다시 만듭니다.
    const profileRole = createdRole ?? role;
    try {
      // 가입(POST /api/v1/users) 후, 거기서 받은 로그인 쿠키로 역할별 프로필까지 만듭니다.
      if (!accountCreated) {
        await signUp(values, role);
        setCreatedRole(role);
      }
      await (profileRole === 'CLIENT' ? createClientProfile(values) : createEscortProfile(values));
      await reload(); // 가입하면 바로 로그인 상태라서, 헤더가 이름을 보여주도록 세션을 다시 읽습니다.
      setValues((prev) => ({ ...prev, password: '', passwordConfirm: '' })); // 비밀번호는 더 들고 있지 않습니다.
      // 가입이 끝났으니 "프로필만 재시도" 상태를 풉니다. 남겨 두면 비밀번호가 빈 채로 잠겨 폼이 막힙니다.
      setCreatedRole(null);
      // 완료 화면에서 뒤로 가기로 이미 제출한 폼에 돌아오지 않도록 기록을 바꿔치기합니다.
      router.replace(`/signup/complete?role=${profileRole}`);
    } catch (error) {
      const next = toFieldError(error, values);
      if (next && (next.key === 'username' || next.key === 'email')) {
        // 확인 뒤에 누가 먼저 가입한 아이디·이메일이면 중복 확인을 다시 받습니다.
        setVerified((prev) => ({ ...prev, [next.key]: '' }));
      }
      if (next && hasField(next.key)) {
        setFieldError(next);
      } else {
        setSubmitError(next?.text ?? (error instanceof Error ? error.message : '회원가입에 실패했습니다.'));
      }
      setSubmitting(false);
    }
  };

  if (roleMismatch || !agreed) return null;

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <div className="mb-8 flex w-full justify-center lg:mb-[50px]">
            <SignupStepper current={3} />
          </div>

          <form
            ref={formRef}
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
                    <RoleSwitch role={role} locked={accountCreated} />
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
                    <FieldErrorMessage error={fieldError} field="name" />
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
                    <FieldErrorMessage error={fieldError} field="phoneNum" />
                  </FormField>

                  <FormField label="아이디*" htmlFor="signup-username" className="lg:col-span-2">
                    <div className="flex gap-2.5 lg:max-w-[485px]">
                      <TextInput
                        ref={usernameRef}
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
                      <CheckButton onClick={usernameCheck.handleCheck} disabled={usernameCheck.checking} />
                    </div>
                    <CheckMessage message={usernameCheck.message} />
                    <FieldErrorMessage error={fieldError} field="username" />
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
                    <FieldErrorMessage error={fieldError} field="password" />
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
                    <FieldErrorMessage error={fieldError} field="passwordConfirm" />
                  </FormField>

                  <FormField label="이메일*" htmlFor="signup-email">
                    {/* 디자인상 이 줄은 485px 로 열(483px)보다 살짝 넓습니다 */}
                    <div className="flex gap-2.5 lg:w-[485px]">
                      <TextInput
                        ref={emailRef}
                        id="signup-email"
                        name="email"
                        type="email"
                        autoComplete="email"
                        required
                        placeholder="이메일을 입력해주세요."
                        value={values.email}
                        onChange={handleChange('email')}
                      />
                      <CheckButton onClick={emailCheck.handleCheck} disabled={emailCheck.checking} />
                    </div>
                    <CheckMessage message={emailCheck.message} />
                    <FieldErrorMessage error={fieldError} field="email" />
                  </FormField>
                  <FormField label="성별*" labelId="signup-gender-label" className="lg:self-center">
                    <GenderRadioGroup
                      labelId="signup-gender-label"
                      value={values.gender}
                      onChange={(gender) => !accountCreated && setValues((prev) => ({ ...prev, gender }))}
                    />
                    <FieldErrorMessage error={fieldError} field="gender" />
                  </FormField>

                  <FormField label="생년월일*" htmlFor="signup-birth-date">
                    <TextInput
                      id="signup-birth-date"
                      name="birthDate"
                      type="date"
                      autoComplete="bday"
                      required
                      max={LAST_BIRTH_DATE}
                      value={values.birthDate}
                      onChange={handleChange('birthDate')}
                    />
                    <FieldErrorMessage error={fieldError} field="birthDate" />
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
                    <FieldErrorMessage error={fieldError} field="region" />
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
                        maxLength={50}
                        required
                        placeholder="보호자 이름을 입력해주세요."
                        value={values.guardianName}
                        onChange={handleChange('guardianName')}
                      />
                      <FieldErrorMessage error={fieldError} field="guardianName" />
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
                      <FieldErrorMessage error={fieldError} field="guardianPhone" />
                    </FormField>
                    <FormField label="의뢰인 특이사항" htmlFor="signup-care-note" className="lg:col-span-2">
                      <TextArea
                        id="signup-care-note"
                        name="careNote"
                        maxLength={500}
                        placeholder="의뢰인의 건강상태, 주의사항, 필요한 도움 등을 자유롭게 작성해주세요."
                        value={values.careNote}
                        onChange={handleChange('careNote')}
                      />
                      <FieldErrorMessage error={fieldError} field="careNote" />
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
                  {/* 정산받을 계좌 — 백엔드 EscortProfileRequest 가 셋 다 필수라 모두 required 입니다. */}
                  <div className="flex flex-col gap-[21px] py-2.5 lg:flex-row lg:justify-between lg:gap-8">
                    <FormField label="은행*" htmlFor="signup-bank" className="lg:w-[222px]">
                      <SelectInput
                        id="signup-bank"
                        name="bankName"
                        required
                        value={values.bankName}
                        onChange={handleChange('bankName')}
                        className={ACCOUNT_FIELD}
                      >
                        <option value="" disabled hidden>
                          은행을 선택해주세요.
                        </option>
                        {BANKS.map((bank) => (
                          <option key={bank} value={bank} className="text-brand">
                            {bank}
                          </option>
                        ))}
                      </SelectInput>
                      <FieldErrorMessage error={fieldError} field="bankName" />
                    </FormField>
                    <FormField label="예금주*" htmlFor="signup-account-holder" className="lg:w-[222px]">
                      <TextInput
                        id="signup-account-holder"
                        name="accountHolder"
                        maxLength={50}
                        required
                        placeholder="예금주명을 입력해주세요."
                        value={values.accountHolder}
                        onChange={handleChange('accountHolder')}
                        className={ACCOUNT_FIELD}
                      />
                      <FieldErrorMessage error={fieldError} field="accountHolder" />
                    </FormField>
                    <FormField label="계좌번호*" htmlFor="signup-account-number" className="lg:w-[222px]">
                      <TextInput
                        id="signup-account-number"
                        name="accountNumber"
                        inputMode="numeric"
                        maxLength={30}
                        required
                        pattern="[0-9\-]{8,30}"
                        title="숫자와 ‘-’만 입력해주세요. (8자 이상)"
                        placeholder="계좌번호를 입력해주세요."
                        value={values.accountNumber}
                        onChange={handleChange('accountNumber')}
                        className={ACCOUNT_FIELD}
                      />
                      <FieldErrorMessage error={fieldError} field="accountNumber" />
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
                    <FieldErrorMessage error={fieldError} field="intro" />
                  </FormField>
                </FormCard>
              </section>
            )}

            {accountCreated && (
              <p role="status" className={`w-full ${ERROR_TEXT}`}>
                계정은 만들어졌어요. 추가 정보만 고친 뒤 다시 가입하기를 눌러 주세요.
              </p>
            )}
            {submitError && (
              <p role="alert" className={`w-full ${ERROR_TEXT}`}>
                {submitError}
              </p>
            )}

            <div className="flex w-full gap-2.5">
              <StepNavButton href={`/signup/terms?role=${role}`}>이전</StepNavButton>
              <StepNavButton
                variant="solid"
                disabled={submitting}
                inactive={unverified.length > 0}
                onClick={handleNextClick}
              >
                {submitting ? '가입 중...' : '가입하기'}
              </StepNavButton>
            </div>
          </form>
        </Container>
      </section>
    </AppShell>
  );
}
