import type { SocialProvider } from '../types';

/** Figma 564:17999 — 간편 로그인 버튼 3종 (버튼 크기 52px 고정) */
export const SOCIAL_PROVIDERS: SocialProvider[] = [
  {
    id: 'google',
    label: '구글로 시작하기',
    icon: '/icons/social-google.svg',
    width: 23.1818,
    height: 23.1818,
    left: 14,
    top: 14,
  },
  {
    id: 'apple',
    label: '애플로 시작하기',
    icon: '/icons/social-apple.svg',
    width: 22,
    height: 27,
    left: 15,
    top: 11,
  },
  {
    id: 'facebook',
    label: '페이스북으로 시작하기',
    icon: '/icons/social-facebook-mark.svg',
    width: 11,
    height: 22,
    left: 21,
    top: 15,
    badgeColor: '#0066ff',
  },
];
