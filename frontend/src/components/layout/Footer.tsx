'use client';

import Image from 'next/image';
import Link from 'next/link';
import Container from '@/components/ui/Container';

const LINKS = [
  { label: '서비스 소개', href: '/#about' },
  { label: '이용 방법', href: '/#steps' },
  { label: '고객센터', href: '/support' },
  { label: '개인정보 처리방침', href: '/privacy' },
];

const SOCIAL = [
  { label: 'Facebook', src: '/icons/social-facebook.svg' },
  { label: 'Instagram', src: '/icons/social-instagram.svg' },
  { label: 'YouTube', src: '/icons/social-youtube.svg' },
];

export default function Footer() {
  return (
    <footer id="footer" className="flex items-center bg-white py-8 lg:min-h-[162px] lg:py-0">
      <Container className="flex flex-col items-start justify-between gap-5 lg:flex-row lg:flex-wrap lg:items-center lg:gap-8">
        <p className="text-lg leading-[18px] text-footer">
          Copyright © 2026 GAJI | All Rights Reserved
        </p>

        <nav aria-label="하단 메뉴" className="flex flex-wrap items-center gap-5 lg:gap-10">
          {LINKS.map((link) => (
            <Link
              key={link.label}
              href={link.href}
              className="text-lg leading-[18px] whitespace-nowrap text-footer transition-colors hover:text-brand"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="flex gap-4">
          {SOCIAL.map(({ label, src }) => (
            <a key={label} href="#" aria-label={label} className="shrink-0 transition-opacity hover:opacity-80">
              <Image src={src} alt="" width={36} height={36} />
            </a>
          ))}
        </div>
      </Container>
    </footer>
  );
}
