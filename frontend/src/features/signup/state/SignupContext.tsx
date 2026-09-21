'use client';

import {
  createContext,
  useContext,
  useState,
  type Dispatch,
  type ReactNode,
  type SetStateAction,
} from 'react';
import { INITIAL_FORM_VALUES } from '../model';
import type { SignupFormValues } from '../types';

type SignupContextValue = {
  values: SignupFormValues;
  setValues: Dispatch<SetStateAction<SignupFormValues>>;
};

const SignupContext = createContext<SignupContextValue | null>(null);

/**
 * 가입 1~4단계 화면이 입력값을 함께 씁니다 (app/signup/layout.tsx 에서 감쌉니다).
 * 비밀번호가 들어 있어 URL·브라우저 저장소에는 두지 않고 메모리에만 둡니다. 새로고침하면 사라집니다.
 */
export function SignupProvider({ children }: { children: ReactNode }) {
  const [values, setValues] = useState<SignupFormValues>(INITIAL_FORM_VALUES);
  return <SignupContext value={{ values, setValues }}>{children}</SignupContext>;
}

export function useSignup() {
  const context = useContext(SignupContext);
  if (!context) throw new Error('useSignup 은 SignupProvider 안에서만 사용할 수 있습니다.');
  return context;
}
