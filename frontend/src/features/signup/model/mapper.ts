import type {
  ClientProfileRequest,
  EscortProfileRequest,
  SignupFormValues,
  SignupRole,
  UserSignUpRequest,
} from '../types';

/**
 * 화면은 "-" 없이 숫자만 받지만, 백엔드에는 하이픈을 붙여서 보냅니다.
 * 백엔드는 받은 문자열을 그대로 저장하므로 형식을 프론트에서 맞춰 줍니다.
 *
 * 02-1234-5678 · 031-123-4567 · 010-1234-5678 을 모두 처리하고,
 * 자릿수가 예상과 다르면 숫자만 남긴 값을 그대로 돌려줍니다.
 */
export function formatPhoneNum(value: string): string {
  const digits = value.replace(/\D/g, '');
  if (digits.length === 11) return digits.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  if (digits.length === 10) {
    return digits.startsWith('02')
      ? digits.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3')
      : digits.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  }
  return digits;
}

/**
 * 2단계 입력값 → 회원가입 요청 본문 (POST /api/v1/users)
 * birthDate 는 달력 입력이라 이미 yyyy-MM-dd 형식입니다.
 */
export function toSignUpRequest(values: SignupFormValues, role: SignupRole): UserSignUpRequest {
  return {
    username: values.username,
    password: values.password,
    email: values.email,
    name: values.name,
    role,
    // 성별은 2단계에서 required 라 빈 값으로 여기까지 올 수 없습니다.
    gender: values.gender as UserSignUpRequest['gender'],
    birthDate: values.birthDate,
    phoneNum: formatPhoneNum(values.phoneNum),
    region: values.region,
  };
}

/** 2단계 입력값 → 의뢰인 프로필 요청 본문 (POST /api/v1/users/profile/client) */
export function toClientProfileRequest(values: SignupFormValues): ClientProfileRequest {
  return {
    emergencyContactName: values.guardianName,
    emergencyContactPhone: formatPhoneNum(values.guardianPhone),
    careNote: values.careNote,
  };
}

/** 2단계 입력값 → 동행 매니저 프로필 요청 본문 (POST /api/v1/users/profile/escort) */
export function toEscortProfileRequest(values: SignupFormValues): EscortProfileRequest {
  return {
    intro: values.intro,
    bankName: values.bankName,
    accountHolder: values.accountHolder,
    accountNumber: values.accountNumber,
  };
}
