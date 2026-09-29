'use client';

import type { ReactNode } from 'react';
import { SignupProvider } from '@/features/signup';

/** /signup 아래 모든 단계가 입력값을 공유하도록 감쌉니다. (단계 사이를 이동해도 상태가 유지됩니다) */
export default function SignupLayout({ children }: { children: ReactNode }) {
  return <SignupProvider>{children}</SignupProvider>;
}
