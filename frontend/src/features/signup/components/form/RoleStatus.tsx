import { cn } from '@/lib/cn';
import type { SignupRole } from '../../types';

const ITEMS: { role: SignupRole; label: string }[] = [
  { role: 'CLIENT', label: '의뢰인' },
  { role: 'ESCORT', label: '동행 매니저' },
];

/** 1단계에서 고른 가입 유형을 보여주는 알약 (Figma "역할 선택 상태" 564:17786) */
export default function RoleStatus({ role }: { role: SignupRole }) {
  return (
    <div
      role="group"
      aria-label="선택한 가입 유형"
      className="flex h-11 w-[250px] items-center justify-center gap-2.5 rounded-full border border-line bg-white"
    >
      {ITEMS.map((item) => {
        const selected = item.role === role;
        return (
          <span
            key={item.role}
            aria-current={selected ? 'true' : undefined}
            className={cn(
              'flex h-11 w-[120px] shrink-0 items-center justify-center p-2.5 text-base leading-5 font-semibold whitespace-nowrap',
              selected
                ? 'rounded-full bg-brand text-white shadow-[0_1px_4px_rgba(78,159,255,0.2)]'
                : 'rounded-[30px] text-brand',
            )}
          >
            {item.label}
          </span>
        );
      })}
    </div>
  );
}
