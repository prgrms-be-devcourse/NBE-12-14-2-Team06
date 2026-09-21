import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  children: ReactNode;
  /** 'default' = 1200px (Hero·가지란·변화) / 'narrow' = 1106px (추천 대상·이용 방법) */
  width?: 'default' | 'narrow';
  className?: string;
};

export default function Container({ children, width = 'default', className }: Props) {
  return (
    <div
      className={cn(
        'mx-auto w-full px-6',
        width === 'narrow' ? 'max-w-[1106px]' : 'max-w-[1200px]',
        className,
      )}
    >
      {children}
    </div>
  );
}
