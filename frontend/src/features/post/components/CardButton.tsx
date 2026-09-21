import Link from 'next/link';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  /** ghost = 흰 버튼(상세보기) / solid = 채운 버튼 / disabled = 누를 수 없음(지원불가) */
  variant?: 'ghost' | 'solid' | 'disabled';
  href?: string;
  onClick?: () => void;
  /** wide = 큰 카드용 186×38 버튼 (의뢰인 "내가 작성한 공고") */
  size?: 'default' | 'wide';
  children: ReactNode;
};

const BASE =
  'flex h-[37.2px] w-full max-w-[148.9px] flex-1 items-center justify-center rounded-[17px] px-4 text-[10.83px] leading-3 font-semibold transition-colors';

const SIZE = {
  default: '',
  wide: 'h-[38px] max-w-[186.3px]',
} as const;

const VARIANT = {
  ghost: 'border-[0.68px] border-line bg-white text-brand hover:bg-line-soft',
  solid: 'bg-brand text-white hover:bg-brand-hover',
  disabled: 'cursor-not-allowed bg-[#e6e8ec] text-brand',
} as const;

/** 공고 카드 아래쪽 버튼 (Figma 149×37, wide 는 186×38) */
export default function CardButton({ variant = 'ghost', href, onClick, size = 'default', children }: Props) {
  const className = cn(BASE, SIZE[size], VARIANT[variant]);

  if (href) {
    return (
      <Link href={href} className={className}>
        {children}
      </Link>
    );
  }
  return (
    <button type="button" onClick={onClick} disabled={variant === 'disabled'} className={className}>
      {children}
    </button>
  );
}
