import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  children: ReactNode;
  /**
   * 'default' = 1200px (Hero·가지란·변화) / 'narrow' = 1106px (추천 대상·이용 방법)
   * 'wide' = 1324px (공고 상세처럼 안쪽 내용이 1276px 인 화면)
   */
  width?: 'default' | 'narrow' | 'wide';
  className?: string;
};

const MAX_WIDTH = {
  default: 'max-w-[1200px]',
  narrow: 'max-w-[1106px]',
  wide: 'max-w-[1324px]',
} as const;

export default function Container({ children, width = 'default', className }: Props) {
  return <div className={cn('mx-auto w-full px-6', MAX_WIDTH[width], className)}>{children}</div>;
}
