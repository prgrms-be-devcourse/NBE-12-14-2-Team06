'use client';

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { ApiError } from '@/lib/api';
import { fetchMyProfile, logout as requestLogout } from '../api';
import type { CurrentUser } from '../types';

export type CurrentUserState = {
  user: CurrentUser | null;
  loading: boolean;
  /** 로그인이 필요한 상태 (토큰 없음·만료·유효하지 않음 — 백엔드 401) */
  unauthenticated: boolean;
  /** 401 이 아닌 이유로 조회에 실패했을 때의 메시지 */
  error: string | null;
};

type AuthContextValue = CurrentUserState & {
  /** 내 정보를 다시 불러옵니다. 로그인·회원가입 직후에 부르세요. 결과 사용자를 돌려줍니다. */
  reload: () => Promise<CurrentUser | null>;
  /** 로그아웃. 쿠키를 지우고 세션을 비웁니다. */
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

const LOADING: CurrentUserState = { user: null, loading: true, unauthenticated: false, error: null };
const LOGGED_OUT: CurrentUserState = {
  user: null,
  loading: false,
  unauthenticated: true,
  error: null,
};

/**
 * 내 정보를 불러와 화면이 쓰기 좋은 상태로 바꿉니다.
 * 백엔드 401 은 "로그인이 필요하다"는 뜻이라 실패가 아니라 상태로 다룹니다.
 */
async function fetchState(): Promise<CurrentUserState> {
  try {
    const user = await fetchMyProfile();
    return { user, loading: false, unauthenticated: false, error: null };
  } catch (cause: unknown) {
    const unauthenticated = cause instanceof ApiError && cause.statusCode.startsWith('401');
    if (unauthenticated) return LOGGED_OUT;

    return {
      user: null,
      loading: false,
      unauthenticated: false,
      error: cause instanceof Error ? cause.message : '내 정보를 불러오지 못했습니다.',
    };
  }
}

/**
 * 앱 전체가 함께 쓰는 로그인 세션.
 *
 * 화면마다 GET /api/v1/users/profile 을 따로 부르지 않도록 app/layout.tsx 에서 한 번만 감쌉니다.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<CurrentUserState>(LOADING);

  useEffect(() => {
    let ignore = false; // 언마운트되면 늦게 온 응답은 버립니다.

    fetchState().then((next) => {
      if (!ignore) setState(next);
    });

    return () => {
      ignore = true;
    };
  }, []);

  const reload = async () => {
    const next = await fetchState();
    setState(next);
    return next.user;
  };

  const signOut = async () => {
    try {
      await requestLogout();
    } catch {
      // 토큰이 이미 만료된 경우처럼 서버 쪽 정리가 실패해도 화면은 로그아웃 상태로 둡니다.
    }
    setState(LOGGED_OUT);
  };

  return <AuthContext value={{ ...state, reload, signOut }}>{children}</AuthContext>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);

  if (!value) {
    throw new Error('useAuth 는 AuthProvider 안에서만 쓸 수 있습니다. app/layout.tsx 를 확인하세요.');
  }

  return value;
}
