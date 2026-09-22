/** 가입 가능한 역할 (백엔드 Role enum 중 ADMIN 제외) */
export type SignupRole = 'CLIENT' | 'ESCORT';

/** 1단계 — 가입 유형 선택 카드 */
export type RoleOption = {
  role: SignupRole;
  title: string;
  description: [string, string];
  image: string;
  imageAlt: string;
  /** 버튼 문구 */
  cta: string;
  /** 'solid' = 채운 버튼 / 'ghost' = 흰 버튼 */
  variant: 'solid' | 'ghost';
  /** 다음 단계(정보 입력)로 이동할 주소 */
  href: string;
  benefits: string[];
};

/** 가입 진행 단계 */
export type SignupStep = {
  num: number;
  title: string;
};

/** 성별 선택값 (백엔드 Gender enum 과 같습니다) */
export type SignupGender = 'MALE' | 'FEMALE';

/** 2단계 — 정보 입력 폼 값 */
export type SignupFormValues = {
  name: string;
  phoneNum: string;
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  gender: SignupGender | '';
  /** 달력(<input type="date">)에서 오는 값이라 YYYY-MM-DD 입니다. */
  birthDate: string;
  region: string;
  /** 의뢰인 추가 정보 */
  guardianName: string;
  guardianPhone: string;
  careNote: string;
  /** 동행 매니저 추가 정보 (백엔드 EscortProfile: bankName · accountHolder · accountNumber · intro) */
  bankName: string;
  accountHolder: string;
  accountNumber: string;
  intro: string;
};

/** 3단계 — 약관 동의 항목 */
export type Agreement = {
  id: string;
  title: string;
  description: string;
  required: boolean;
};

/** 역할별 약관 목록 */
export type AgreementGroup = {
  /** "필수 동의 항목(…)" 소제목 */
  requiredTitle: string;
  required: Agreement[];
  optional: Agreement[];
};

/* ───────────── 서버로 보내는 요청 본문 (백엔드 DTO) ───────────── */

/** POST /api/v1/users 요청 본문 (백엔드 UserSignUpRequest) */
export type UserSignUpRequest = {
  username: string;
  password: string;
  email: string;
  name: string;
  role: SignupRole;
  gender: SignupGender;
  /** yyyy-MM-dd */
  birthDate: string;
  /** 하이픈을 붙여서 보냅니다. (예: 010-1000-0001) */
  phoneNum: string;
  region: string;
};

/** POST /api/v1/users 응답 (백엔드 UserSignUpResponse) */
export type UserSignUpResponse = {
  id: number;
  name: string;
};

/** POST /api/v1/users/profile/client 요청 본문 (백엔드 ClientProfileRequest) */
export type ClientProfileRequest = {
  emergencyContactName: string;
  /** 하이픈을 붙여서 보냅니다. */
  emergencyContactPhone: string;
  /** 비워서 보내면 백엔드가 "특이사항 없음"으로 저장합니다. */
  careNote: string;
};

/** POST /api/v1/users/profile/escort 요청 본문 (백엔드 EscortProfileRequest) */
export type EscortProfileRequest = {
  intro: string;
  bankName: string;
  accountHolder: string;
  accountNumber: string;
};
