import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import { ArrowRightIcon } from './icons';

type Props = {
  children: ReactNode;
  href: string;
  variant?: 'solid' | 'ghost';
  className?: string;
};

/** Hero의 "동행 요청하기 / 공고 찾아보기" 버튼 (Figma 564:18150, 564:18156) */
export default function PillButton({ children, href, variant = 'solid', className }: Props) {
  return (
    <a
      href={href}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-pill px-9 py-6',
        'text-lg leading-[18px] font-bold whitespace-nowrap',
        'transition-colors active:translate-y-px',
        variant === 'solid'
          ? 'bg-brand text-white hover:bg-brand-hover'
          : 'border border-line bg-white text-footer hover:bg-line-soft',
        className,
      )}
    >
      {children}
      <ArrowRightIcon className="size-[18px] shrink-0" />
    </a>
  );
}
