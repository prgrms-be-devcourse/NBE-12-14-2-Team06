/**
 * auth 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 *
 * 예외: components/layout 의 AppShell 은 여기를 거치면 순환 import 가 생겨서
 *      lib/AuthProvider · model/roleHome 을 직접 가져옵니다. (AppShell 주석 참고)
 */
export { default as LoginPage } from './components/LoginPage';
export { fetchMyProfile, login, logout } from './api';
export { AuthProvider, useAuth } from './lib/AuthProvider';
export { useCurrentUser, type CurrentUserState } from './lib/useCurrentUser';
export { useRequireAuth } from './lib/useRequireAuth';
export { LOGIN_HOME_BY_ROLE, MYPAGE_BY_ROLE } from './model';
export type { CurrentUser, LoginFormValues, SocialProvider, UserLoginResponse } from './types';
