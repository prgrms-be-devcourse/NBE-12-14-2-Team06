'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import type { ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { cn } from '@/lib/cn';
import type { MyPageRole } from '../types';

// TODO: 설정 화면은 아직 디자인/구현이 없습니다.
const MENUS: Record<MyPageRole, { label: string; href: string }[]> = {
  escort: [
    { label: '내 정보', href: '/mypage' },
    { label: '내가 신청한 공고', href: '/mypage/applications' },
    { label: '내 정산', href: '/mypage/settlements' },
    { label: '받은 리뷰', href: '/mypage/reviews' },
    { label: '설정', href: '#' },
  ],
  client: [
    { label: '내 정보', href: '/client' },
    { label: '작성한 공고', href: '/client/posts' },
    { label: '설정', href: '#' },
  ],
};

type Props = {
  children: ReactNode;
  /** 동행 매니저(기본) / 의뢰인 */
  role?: MyPageRole;
};

/** 마이페이지 공통 틀 — 왼쪽 메뉴(256px) + 구분선 + 오른쪽 내용 (Figma Main_MyPage) */
export default function MyPageShell({ children, role = 'escort' }: Props) {
  const pathname = usePathname();
  const menu = MENUS[role];

  return (
    <AppShell>
      <div className="flex flex-col bg-white lg:flex-row">
        <nav
          aria-label="마이페이지 메뉴"
          className="flex gap-2.5 overflow-x-auto px-4 py-4 lg:w-[257px] lg:shrink-0 lg:flex-col lg:border-r lg:border-[#e6e8ec] lg:py-[50px]"
        >
          {menu.map((item) => {
            const active = item.href === pathname;
            return (
              <Link
                key={item.label}
                href={item.href}
                aria-current={active ? 'page' : undefined}
                className={cn(
                  'flex h-14 shrink-0 items-center justify-center rounded-[15px] px-5 text-lg leading-[22px] font-semibold whitespace-nowrap transition-colors lg:w-full',
                  active ? 'bg-line-soft text-brand' : 'text-brand-muted hover:text-brand',
                )}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="min-w-0 flex-1 px-4 pb-10 lg:pr-6 lg:pb-[50px] lg:pl-[70px]">{children}</div>
      </div>
    </AppShell>
  );
}
