'use client';

import Link from 'next/link';
import Container from '@/components/ui/Container';

const MENU = [
  { label: '서비스 소개', href: '#about' },
  { label: '공고 찾기', href: '#steps' },
  { label: '이용 방법', href: '#steps' },
  { label: '고객센터', href: '#footer' },
  { label: '개인정보 처리방침', href: '#footer' },
];

const BUTTON_BASE =
  'inline-flex items-center justify-center rounded-[30px] px-6 py-[18px] text-base leading-[18px] font-semibold whitespace-nowrap transition-colors';

export default function Header() {
  return (
    <header className="flex items-center bg-white py-4 lg:h-[118px] lg:py-0">
      <Container className="flex flex-wrap items-center justify-between gap-6">
        {/* 모바일에서는 메뉴를 아래로 내리고 가로 스크롤 */}
        <nav
          aria-label="주요 메뉴"
          className="order-2 flex w-full items-center gap-5 overflow-x-auto pb-1 lg:order-none lg:w-auto lg:gap-[33px] lg:overflow-visible lg:pb-0"
        >
          {MENU.map((item) => (
            <a
              key={item.label}
              href={item.href}
              className="text-base leading-[18px] whitespace-nowrap text-brand transition-colors hover:text-brand-hover lg:text-lg"
            >
              {item.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-2.5">
          <a
            href="#"
            className={`${BUTTON_BASE} min-w-[104px] border border-line bg-white text-brand hover:bg-line-soft`}
          >
            로그인
          </a>
          <Link href="/signup" className={`${BUTTON_BASE} bg-brand text-white hover:bg-brand-hover`}>
            회원가입
          </Link>
        </div>
      </Container>
    </header>
  );
}
