/** 로그인한 사용자 (GET /api/v1/users/profile 의 UserResponse) */
export type CurrentUser = {
  username: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'CLIENT' | 'ESCORT';
  gender: 'MALE' | 'FEMALE';
  /** yyyy-MM-dd */
  birthDate: string;
  /** 하이픈이 포함된 형태로 옵니다. (예: 010-1000-0001) */
  phoneNum: string;
  region: string;
  createdAt: string;
};

/**
 * 로그인 성공 응답 (POST /api/v1/auth/login 의 UserLoginResponse).
 * 역할(role)이 없어서, 로그인 뒤 갈 화면을 정하려면 프로필을 한 번 더 불러와야 합니다.
 */
export type UserLoginResponse = {
  id: number;
  name: string;
};

/** 로그인 폼 값 */
export type LoginFormValues = {
  /** 백엔드는 username 으로만 찾습니다 (이메일 로그인은 아직 없습니다) */
  username: string;
  password: string;
  /** 로그인 상태 유지 — 백엔드에 해당 항목이 없어서 아직 화면 표시용입니다 */
  rememberMe: boolean;
};

/** 간편 로그인 제공자 */
export type SocialProvider = {
  id: 'google' | 'apple' | 'facebook';
  /** 스크린리더/aria-label 용 문구 */
  label: string;
  icon: string;
  /** 아이콘 원본 크기 · 52px 버튼 안에서의 위치 (Figma 564:17999) */
  width: number;
  height: number;
  left: number;
  top: number;
  /** 아이콘 뒤에 깔리는 원형 배경색 (페이스북만 사용) */
  badgeColor?: string;
};
