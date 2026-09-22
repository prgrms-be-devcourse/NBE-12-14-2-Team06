'use client';

import { useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { LOGIN_HOME_BY_ROLE } from '../model';
import type { CurrentUser } from '../types';
import { useAuth, type CurrentUserState } from './AuthProvider';

/**
 * 로그인이 필요한 화면에서 부릅니다.
 *
 * - 로그인이 안 돼 있으면 /login?next=<지금 주소> 로 보냅니다. 로그인하면 이 화면으로 돌아옵니다.
 * - role 을 주면 그 역할만 남고, 다른 역할은 자기 첫 화면으로 돌아갑니다.
 *
 * 돌려주는 상태의 loading 이 true 인 동안에는 "불러오는 중" 화면을 보여 주세요.
 * (로그인 여부가 정해지기 전에는 내용을 그리면 안 됩니다.)
 */
export function useRequireAuth(role?: CurrentUser['role']): CurrentUserState {
  const state = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  const { loading, unauthenticated, user } = state;

  useEffect(() => {
    if (loading) return;

    if (unauthenticated) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }

    if (user && role && user.role !== role) {
      router.replace(LOGIN_HOME_BY_ROLE[user.role]);
    }
  }, [loading, unauthenticated, user, role, pathname, router]);

  return state;
}
