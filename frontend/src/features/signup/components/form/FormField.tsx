import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  label: string;
  /** 입력창 id. 없으면 라디오 묶음처럼 <span> 라벨을 씁니다. */
  htmlFor?: string;
  /** htmlFor 가 없을 때, 묶음이 aria-labelledby 로 참조할 라벨 id */
  labelId?: string;
  className?: string;
  children: ReactNode;
};

/** 라벨 + 입력 영역 (Figma: 라벨 20px semibold, 입력창과 12px 간격) */
export default function FormField({ label, htmlFor, labelId, className, children }: Props) {
  const labelClass = 'text-xl leading-5 font-semibold text-brand';
  return (
    <div className={cn('flex min-w-0 flex-col gap-[9px]', className)}>
      {htmlFor ? (
        <label htmlFor={htmlFor} className={labelClass}>
          {label}
        </label>
      ) : (
        <span id={labelId} className={labelClass}>
          {label}
        </span>
      )}
      {children}
    </div>
  );
}
