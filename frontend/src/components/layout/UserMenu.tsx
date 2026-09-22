'use client';

import { useEffect, useRef, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/cn';

export type UserMenuItem = { label: string; href: string };

type Props = {
  name: string;
  /** 이름을 눌렀을 때 가는 마이페이지 */
  href: string;
  /** 화살표를 눌렀을 때 펼쳐지는 메뉴. 없으면 화살표도 보여주지 않습니다. */
  items?: UserMenuItem[];
};

/**
 * 헤더의 "이름 ⌄".
 * 이름은 마이페이지로 가는 링크이고, 화살표를 누르면 역할별 메뉴가 펼쳐집니다.
 */
export default function UserMenu({ name, href, items }: Props) {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;

    // 메뉴 바깥을 누르면 닫습니다.
    const handlePointerDown = (event: PointerEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    };

    // Esc 로 닫고, 포커스는 화살표로 되돌립니다.
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return;
      setOpen(false);
      buttonRef.current?.focus();
    };

    document.addEventListener('pointerdown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [open]);

  return (
    <div ref={rootRef} className="relative flex items-center gap-2.5">
      <Link
        href={href}
        className="text-lg leading-[18px] font-semibold text-brand transition-colors hover:text-brand-hover"
      >
        {name}
      </Link>

      {items && items.length > 0 && (
        <>
          <button
            ref={buttonRef}
            type="button"
            onClick={() => setOpen((prev) => !prev)}
            aria-expanded={open}
            aria-haspopup="true"
            aria-label={open ? '내 메뉴 닫기' : '내 메뉴 열기'}
            className="grid size-6 shrink-0 cursor-pointer place-items-center rounded-full transition-colors hover:bg-line-soft focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
          >
            <Image
              src="/icons/nav-chevron.svg"
              alt=""
              width={13.0667}
              height={7.23333}
              className={cn('transition-transform', open && 'rotate-180')}
            />
          </button>

          {open && (
            <ul className="absolute top-full right-0 z-20 mt-3 flex w-[200px] flex-col overflow-hidden rounded-[20px] border border-line bg-white py-2 shadow-card">
              {items.map((item) => (
                <li key={item.label}>
                  <Link
                    href={item.href}
                    onClick={() => setOpen(false)}
                    className={cn(
                      'block px-5 py-3 text-base leading-5 font-medium whitespace-nowrap transition-colors hover:bg-line-soft',
                      item.href === pathname ? 'font-semibold text-brand' : 'text-brand-muted',
                    )}
                  >
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  );
}
