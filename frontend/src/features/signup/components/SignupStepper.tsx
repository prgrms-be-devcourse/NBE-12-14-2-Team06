'use client';

import Image from 'next/image';
import { cn } from '@/lib/cn';
import { SIGNUP_STEPS } from '../model';

type Props = {
  /** 현재 단계 (1~4). 이전 단계는 체크 표시, 이후 단계는 흐리게 표시됩니다. */
  current: number;
};

/** Figma 564:17883 · 564:17749 — 가입 진행 상황 */
export default function SignupStepper({ current }: Props) {
  return (
    <ol aria-label="회원가입 진행 상황" className="flex w-full max-w-[538px]">
      {SIGNUP_STEPS.map(({ num, title }) => {
        const done = num < current;
        const active = num === current;
        return (
          <li
            key={num}
            aria-current={active ? 'step' : undefined}
            className="flex min-w-0 flex-1 flex-col items-center gap-[18px] px-1 py-3 lg:p-3"
          >
            <span className="relative size-9 shrink-0">
              <Image
                src={
                  done
                    ? '/icons/step-done-bg.svg'
                    : active
                      ? '/icons/step-active.svg'
                      : '/icons/step-inactive.svg'
                }
                alt=""
                width={36}
                height={36}
              />
              {done ? (
                <Image
                  src="/icons/step-done-check.svg"
                  alt="완료"
                  width={36}
                  height={36}
                  className="absolute inset-0"
                />
              ) : (
                <span
                  className={cn(
                    'absolute inset-0 grid place-items-center text-[15px] leading-none font-semibold tracking-[0.75px]',
                    active ? 'text-white' : 'text-brand-muted',
                  )}
                >
                  {num}
                </span>
              )}
            </span>
            <span
              className={cn(
                'text-[15px] leading-[21px] font-bold whitespace-nowrap lg:text-lg',
                active ? 'text-brand' : 'text-brand-muted',
              )}
            >
              {title}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
