import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import type { LabelTone } from '../types';

const TONE: Record<LabelTone, string> = {
  green: 'border-[#e6ffe5] bg-[#e6ffe5] text-[#209d37]',
  red: 'border-[#ffe3e3] bg-[#ffe3e3] text-[#b91d1d]',
  blue: 'border-[#e8eefa] bg-[#e8eefa] text-[#203b9d]',
  gray: 'border-[#e6e8ec] bg-[#e6e8ec] text-footer',
  strong: 'border-[#6796db] bg-[#6796db] text-white',
};

const SIZE = {
  /** 목록 카드 (Figma 74×25) */
  small: 'h-[25px] min-w-[72px] px-4 text-[10.83px] leading-3',
  /** 공고 상세 제목 (Figma 102×36) */
  large: 'h-9 w-[102px] text-base leading-5',
} as const;

type Props = {
  tone: LabelTone;
  size?: keyof typeof SIZE;
  children: ReactNode;
};

/** 신규 · 오늘 마감 · 모집 중 · 대기 중 · 매칭 완료 … 같은 알약 모양 상태 라벨 */
export default function StatusLabel({ tone, size = 'small', children }: Props) {
  return (
    <span
      className={cn(
        'inline-flex shrink-0 items-center justify-center rounded-[17px] border-[0.68px] font-semibold whitespace-nowrap',
        TONE[tone],
        SIZE[size],
      )}
    >
      {children}
    </span>
  );
}
