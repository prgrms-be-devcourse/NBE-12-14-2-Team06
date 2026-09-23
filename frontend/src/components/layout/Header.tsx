'use client';

import Link from 'next/link';
import Container from '@/components/ui/Container';
import { cn } from '@/lib/cn';
import UserMenu, { type UserMenuItem } from './UserMenu';

type MenuItem = { label: string; href: string; emphasized?: boolean };

const MENU: MenuItem[] = [
  { label: '서비스 소개', href: '/#about' },
  { label: '공고 찾기', href: '/posts' },
  { label: '이용 방법', href: '/#steps' },
  { label: '고객센터', href: '/support' },
  { label: '개인정보 처리방침', href: '/privacy' },
];

/** 의뢰인·관리자로 로그인했을 때만 "공고 찾기" 옆에 끼워 넣는 메뉴 */
const POST_REGISTER_ITEM: MenuItem = { label: '공고등록', href: '/client/posts/new', emphasized: true };

const BUTTON_BASE =
  'inline-flex items-center justify-center rounded-[30px] px-6 py-[18px] text-base leading-[18px] font-semibold whitespace-nowrap transition-colors';

type Props = {
  /**
   * 로그인한 사용자. 있으면 "로그인 / 회원가입" 대신 "이름 ⌄ / 로그아웃"을 보여줍니다.
   * href 는 이름을 눌렀을 때 이동할 마이페이지(기본 /mypage), menu 는 화살표를 눌렀을 때 펼쳐지는 목록입니다.
   */
  user?: { name: string; href?: string; menu?: UserMenuItem[] };
  /** 세션을 확인하는 중. 로그인 상태가 정해질 때까지 버튼 자리를 비워 둡니다(깜빡임 방지). */
  pending?: boolean;
  /** 로그아웃 버튼을 눌렀을 때 */
  onLogout?: () => void;
  /** 의뢰인 또는 관리자로 로그인했는지 — "공고 찾기" 옆에 "공고등록"을 진하게 보여줍니다. */
  canRegisterPost?: boolean;
  /** 동행 매니저로 로그인했는지 — "공고 찾기"를 지원용 목록(/escort/posts)으로 보냅니다. */
  isEscort?: boolean;
};

export default function Header({ user, pending, onLogout, canRegisterPost, isEscort }: Props) {
  const menuItems = canRegisterPost ? [...MENU.slice(0, 2), POST_REGISTER_ITEM, ...MENU.slice(2)] : MENU;

  return (
    <header className="flex items-center bg-white py-4 lg:h-[118px] lg:py-0">
      <Container className="flex flex-wrap items-center justify-between gap-6">
        {/* 모바일에서는 메뉴를 아래로 내리고 가로 스크롤 */}
        <nav
          aria-label="주요 메뉴"
          className="order-2 flex w-full items-center gap-5 overflow-x-auto pb-1 lg:order-none lg:w-auto lg:gap-[33px] lg:overflow-visible lg:pb-0"
        >
          {menuItems.map((item) => {
            // "공고 찾기"는 동행 매니저에게만 지원용 목록(/escort/posts, 로그인 필요)으로 보내고,
            // 그 외(의뢰인·관리자·비로그인)는 원래 공개 목록(/posts)으로 보냅니다.
            // ⚠️ /escort/posts 는 ESCORT 전용 가드가 걸려 있어서, 예전처럼 "로그인만 하면" 보내면
            //    CLIENT/ADMIN 은 눌러도 그 페이지에서 바로 튕겨나와 안 눌리는 것처럼 보입니다.
            const href = item.label === '공고 찾기' && isEscort ? '/escort/posts' : item.href;

            return (
                <Link
                    key={item.label}
                    href={href}
                    className={cn(
                      'text-base leading-[18px] whitespace-nowrap transition-colors hover:text-brand-hover lg:text-lg',
                      item.emphasized ? 'font-bold text-brand' : 'text-brand',
                    )}
                >
                  {item.label}
                </Link>
            );
          })}
        </nav>

        {pending ? (
          // 로그인 여부가 정해지기 전에는 버튼 자리만 잡아 둡니다 (54px = 버튼 높이).
          <div aria-hidden="true" className="h-[54px] min-w-[104px]" />
        ) : user ? (
          <div className="flex items-center gap-[25px]">
            <UserMenu name={user.name} href={user.href ?? '/mypage'} items={user.menu} />
            <button
              type="button"
              onClick={onLogout}
              className={`${BUTTON_BASE} min-w-[104px] border border-line bg-white text-brand hover:bg-line-soft`}
            >
              로그아웃
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-2.5">
            <Link
              href="/login"
              className={`${BUTTON_BASE} min-w-[104px] border border-line bg-white text-brand hover:bg-line-soft`}
            >
              로그인
            </Link>
            <Link href="/signup" className={`${BUTTON_BASE} bg-brand text-white hover:bg-brand-hover`}>
              회원가입
            </Link>
          </div>
        )}
      </Container>
    </header>
  );
}
