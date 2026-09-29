import type { ComponentProps } from 'react';
import { cn } from '@/lib/cn';

/** Figma "Text Area" — 높이 92, 안쪽 여백 16, drop-shadow */
export default function TextArea({ className, ...props }: ComponentProps<'textarea'>) {
  return (
    <textarea
      {...props}
      className={cn(
        'h-[92px] w-full resize-none rounded-[20px] border border-line-soft bg-white p-4 text-base leading-5 text-brand drop-shadow-soft placeholder:text-brand-muted',
        className,
      )}
    />
  );
}
