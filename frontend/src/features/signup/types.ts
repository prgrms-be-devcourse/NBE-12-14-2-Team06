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

/**
 * 성별 선택값.
 * ⚠️ 디자인에 "선택 안 함"이 있지만 백엔드 Gender enum 은 MALE / FEMALE 뿐입니다.
 */
export type SignupGender = 'MALE' | 'FEMALE' | 'NONE';

/** 2단계 — 정보 입력 폼 값 */
export type SignupFormValues = {
  name: string;
  phoneNum: string;
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  gender: SignupGender | '';
  /** 화면 입력 형식 YYYY.MM.DD */
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
