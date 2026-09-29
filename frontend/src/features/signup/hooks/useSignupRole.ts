'use client';

import { useSearchParams } from 'next/navigation';
import type { SignupRole } from '../types';

/** 주소의 ?role= 값을 읽습니다. 없거나 잘못된 값이면 의뢰인으로 봅니다. */
export function useSignupRole(): SignupRole {
  return useSearchParams().get('role') === 'ESCORT' ? 'ESCORT' : 'CLIENT';
}
