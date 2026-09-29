import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = { children: ReactNode; className?: string };

/** 폼을 감싸는 흰 카드 (Figma "Form Wrapper" — 라운드 30, 테두리 + 그림자) */
export default function FormCard({ children, className }: Props) {
  return (
    <div className={cn('w-full rounded-[30px] border border-line bg-white shadow-card', className)}>
      {children}
    </div>
  );
}
