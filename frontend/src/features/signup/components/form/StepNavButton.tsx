import Link from 'next/link';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  /** 'solid' = 채운 버튼(다음·가입하기) / 'ghost' = 흰 버튼(이전) */
  variant?: 'solid' | 'ghost';
  /** 있으면 링크, 없으면 폼 제출 버튼 */
  href?: string;
  /** 제출 버튼일 때만 씁니다. 가입 요청이 오가는 동안 두 번 눌리는 것을 막습니다. */
  disabled?: boolean;
  children: ReactNode;
};

const BASE =
  'flex h-14 flex-1 items-center justify-center rounded-[30px] px-6 text-xl leading-[18px] font-semibold transition-colors';

/** 가입 단계 아래쪽 "이전 / 다음" 버튼 (Figma 높이 56 · 라운드 30) */
export default function StepNavButton({ variant = 'ghost', href, disabled, children }: Props) {
  const className = cn(
    BASE,
    variant === 'solid'
      ? 'bg-brand text-white hover:bg-brand-hover'
      : 'border border-line bg-white text-brand hover:bg-line-soft',
  );

  return href ? (
    <Link href={href} className={className}>
      {children}
    </Link>
  ) : (
    <button
      type="submit"
      disabled={disabled}
      className={cn(className, 'disabled:cursor-not-allowed disabled:opacity-60')}
    >
      {children}
    </button>
  );
}
