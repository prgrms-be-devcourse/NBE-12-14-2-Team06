'use client';

import { useEffect, useState } from 'react';
import { ApiError } from '@/lib/api';
import { fetchMyProfile } from '../api';
import type { CurrentUser } from '../types';

export type CurrentUserState = {
  user: CurrentUser | null;
  loading: boolean;
  /** 로그인이 필요한 상태 (토큰 없음·만료·유효하지 않음 — 백엔드 401) */
  unauthenticated: boolean;
  /** 401 이 아닌 이유로 조회에 실패했을 때의 메시지 */
  error: string | null;
};

/**
 * 로그인한 사용자를 불러옵니다.
 *
 * ⚠️ 아직 전역 세션이 없어서 이 훅을 쓰는 화면마다 요청이 한 번씩 나갑니다.
 * TODO: MOCK_CLIENT/MOCK_USER 를 쓰는 화면들을 이 훅으로 옮길 때,
 *       AppShell 쪽에 Context 를 두어 요청을 한 번으로 합치세요.
 */
export function useCurrentUser(): CurrentUserState {
  const [state, setState] = useState<CurrentUserState>({
    user: null,
    loading: true,
    unauthenticated: false,
    error: null,
  });

  useEffect(() => {
    let ignore = false; // 언마운트되면 늦게 온 응답은 버립니다.

    fetchMyProfile()
      .then((user) => {
        if (!ignore) setState({ user, loading: false, unauthenticated: false, error: null });
      })
      .catch((cause: unknown) => {
        if (ignore) return;

        // 백엔드 401 은 "로그인이 필요하다"는 뜻이라 실패가 아니라 상태로 다룹니다.
        const unauthenticated = cause instanceof ApiError && cause.statusCode.startsWith('401');
        const error = unauthenticated
          ? null
          : cause instanceof Error
            ? cause.message
            : '내 정보를 불러오지 못했습니다.';

        setState({ user: null, loading: false, unauthenticated, error });
      });

    return () => {
      ignore = true;
    };
  }, []);

  return state;
}
