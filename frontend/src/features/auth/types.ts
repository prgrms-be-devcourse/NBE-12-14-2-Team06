/** 로그인 폼 값 */
export type LoginFormValues = {
  /** 아이디 또는 이메일 */
  username: string;
  password: string;
  /** 로그인 상태 유지 */
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
