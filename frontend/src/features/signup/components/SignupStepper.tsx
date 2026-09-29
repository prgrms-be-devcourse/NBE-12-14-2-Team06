'use client';

import { StepBar } from '@/components/ui';
import { SIGNUP_STEPS } from '../model';

const STEP_TITLES = SIGNUP_STEPS.map(({ title }) => title);

type Props = {
  /** 현재 단계 (1~4). 이전 단계는 체크 표시, 이후 단계는 흐리게 표시됩니다. */
  current: number;
};

/** Figma 564:17883 · 564:17749 — 가입 진행 상황 (동행 현황의 진행 단계와 같은 모양) */
export default function SignupStepper({ current }: Props) {
  return (
    <StepBar
      steps={STEP_TITLES}
      current={current}
      label="회원가입 진행 상황"
      className="max-w-[538px]"
    />
  );
}
