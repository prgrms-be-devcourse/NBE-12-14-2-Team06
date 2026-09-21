'use client';

import Image from 'next/image';
import Link from 'next/link';
import Container from '@/components/ui/Container';

const MENU = [
  { label: '서비스 소개', href: '#about' },
  { label: '공고 찾기', href: '/posts' },
  { label: '이용 방법', href: '#steps' },
  { label: '고객센터', href: '#footer' },
  { label: '개인정보 처리방침', href: '#footer' },
];

const BUTTON_BASE =
  'inline-flex items-center justify-center rounded-[30px] px-6 py-[18px] text-base leading-[18px] font-semibold whitespace-nowrap transition-colors';

type Props = {
  /** 로그인한 사용자. 있으면 "로그인 / 회원가입" 대신 "이름 ⌄ / 로그아웃"을 보여줍니다. href 는 이름을 눌렀을 때 이동할 마이페이지 (기본 /mypage) */
  user?: { name: string; href?: string };
};

export default function Header({ user }: Props) {
  return (
    <header className="flex items-center bg-white py-4 lg:h-[118px] lg:py-0">
      <Container className="flex flex-wrap items-center justify-between gap-6">
        {/* 모바일에서는 메뉴를 아래로 내리고 가로 스크롤 */}
        <nav
          aria-label="주요 메뉴"
          className="order-2 flex w-full items-center gap-5 overflow-x-auto pb-1 lg:order-none lg:w-auto lg:gap-[33px] lg:overflow-visible lg:pb-0"
        >
          {MENU.map((item) => (
            <Link
              key={item.label}
              href={item.href}
              className="text-base leading-[18px] whitespace-nowrap text-brand transition-colors hover:text-brand-hover lg:text-lg"
            >
              {item.label}
            </Link>
          ))}
        </nav>

        {user ? (
          <div className="flex items-center gap-[25px]">
            <Link
              href={user.href ?? '/mypage'}
              className="flex items-center gap-2.5 text-lg leading-[18px] font-semibold text-brand transition-colors hover:text-brand-hover"
            >
              {user.name}
              <Image src="/icons/nav-chevron.svg" alt="" width={13.0667} height={7.23333} />
            </Link>
            {/* TODO: 로그아웃 API(DELETE /api/v1/auth/logout) 연결 */}
            <button
              type="button"
              className={`${BUTTON_BASE} min-w-[104px] border border-line bg-white text-brand hover:bg-line-soft`}
            >
              로그아웃
            </button>
          </div>
        ) : (
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
        )}
      </Container>
    </header>
  );
}
