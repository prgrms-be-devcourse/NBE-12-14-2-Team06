import type { ComponentProps } from 'react';
import { cn } from '@/lib/cn';
import { FIELD_CLASS } from './fieldStyle';

/** 입력창과 같은 모양의 선택 상자. 아직 고르지 않았으면 안내 문구 색으로 보입니다. */
export default function SelectInput({ className, children, ...props }: ComponentProps<'select'>) {
  return (
    <select
      {...props}
      className={cn(FIELD_CLASS, 'appearance-none', props.value ? 'text-brand' : 'text-brand-muted', className)}
    >
      {children}
    </select>
  );
}
