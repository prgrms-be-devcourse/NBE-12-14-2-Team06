'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/cn';
import type { SignupRole } from '../../types';

const ITEMS: { role: SignupRole; label: string }[] = [
  { role: 'CLIENT', label: '의뢰인' },
  { role: 'ESCORT', label: '동행 매니저' },
];

/**
 * 가입 유형을 고르는 알약 (Figma "역할 선택 상태" 564:17786)
 *
 * 누르면 주소의 ?role= 만 바꾸고, 그에 맞는 입력칸이 나옵니다.
 * 이미 입력한 값은 SignupContext 에 남아 있어 유형을 오가도 그대로입니다.
 * 뒤로 가기에 같은 단계가 쌓이지 않도록 replace 로 이동합니다.
 */
export default function RoleSwitch({ role }: { role: SignupRole }) {
  const pathname = usePathname();

  return (
    <div
      role="group"
      aria-label="가입 유형 선택"
      className="flex h-11 w-[250px] items-center justify-center gap-2.5 rounded-full border border-line bg-white"
    >
      {ITEMS.map((item) => {
        const selected = item.role === role;
        return (
          <Link
            key={item.role}
            href={`${pathname}?role=${item.role}`}
            replace
            aria-current={selected ? 'true' : undefined}
            className={cn(
              'flex h-11 w-[120px] shrink-0 items-center justify-center p-2.5 text-base leading-5 font-semibold whitespace-nowrap transition-colors',
              'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand',
              selected
                ? 'rounded-full bg-brand text-white shadow-[0_1px_4px_rgba(78,159,255,0.2)]'
                : 'rounded-[30px] text-brand hover:bg-line-soft',
            )}
          >
            {item.label}
          </Link>
        );
      })}
    </div>
  );
}
