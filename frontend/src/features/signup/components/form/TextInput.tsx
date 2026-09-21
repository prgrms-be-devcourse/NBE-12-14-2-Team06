import type { ComponentProps } from 'react';
import { cn } from '@/lib/cn';
import { FIELD_CLASS } from './fieldStyle';

export default function TextInput({ className, ...props }: ComponentProps<'input'>) {
  return <input {...props} className={cn(FIELD_CLASS, 'text-brand', className)} />;
}
