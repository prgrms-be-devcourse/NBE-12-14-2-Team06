'use client';

import Container from '@/components/ui/Container';
import { FacebookIcon, InstagramIcon, YoutubeIcon } from '@/components/ui/icons';

const LINKS = [
  { label: '서비스 소개', href: '#about' },
  { label: '이용 방법', href: '#steps' },
  { label: '고객센터', href: '#' },
  { label: '개인정보 처리방침', href: '#' },
];

const SOCIAL = [
  { label: 'Facebook', Icon: FacebookIcon },
  { label: 'Instagram', Icon: InstagramIcon },
  { label: 'YouTube', Icon: YoutubeIcon },
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
            <a
              key={link.label}
              href={link.href}
              className="text-lg leading-[18px] whitespace-nowrap text-footer transition-colors hover:text-brand"
            >
              {link.label}
            </a>
          ))}
        </nav>

        <div className="flex gap-4">
          {SOCIAL.map(({ label, Icon }) => (
            <a
              key={label}
              href="#"
              aria-label={label}
              className="grid size-9 place-items-center rounded-lg bg-line-soft text-brand transition-colors hover:bg-line"
            >
              <Icon className="size-5" />
            </a>
          ))}
        </div>
      </Container>
    </footer>
  );
}
