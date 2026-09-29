/**
 * Figma 원본 SVG를 내려받지 못해(네트워크 차단) 동일 형태의 인라인 SVG로 대체했습니다.
 * 원본을 export 하면 각 함수 내부만 교체하면 됩니다.
 */
import type { SVGProps } from 'react';

export function ArrowRightIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 18 18" fill="none" aria-hidden="true" {...props}>
      <path d="M3.375 9h11.25M10.125 4.5 14.625 9l-4.5 4.5" stroke="currentColor" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function UserIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <circle cx="12" cy="8" r="3.4" stroke="currentColor" strokeWidth={1.6} />
      <path d="M5 20c.6-3.3 3.4-5.3 7-5.3s6.4 2 7 5.3" stroke="currentColor" strokeWidth={1.6} strokeLinecap="round" />
    </svg>
  );
}

export function ShieldCheckIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M12 3 5 6v5.5c0 4.2 2.9 8.1 7 9.5 4.1-1.4 7-5.3 7-9.5V6l-7-3Z" stroke="currentColor" strokeWidth={1.5} strokeLinejoin="round" />
      <path d="m9 12 2 2 4-4" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function ManagerIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <circle cx="12" cy="8" r="3.6" stroke="currentColor" strokeWidth={1.5} />
      <path d="M4.8 20c.6-3.5 3.6-5.6 7.2-5.6s6.6 2.1 7.2 5.6" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" />
    </svg>
  );
}

export function TapIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M8 4.5a1.6 1.6 0 1 1 3.2 0v6.2" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" />
      <path d="M11.2 10.5V9a1.5 1.5 0 0 1 3 0v1.5m0 0V9.8a1.5 1.5 0 0 1 3 0v1.4m0 0a1.5 1.5 0 0 1 3 0v3.3c0 3.1-2.4 5.5-5.6 5.5h-1.4c-2.2 0-3.6-1-4.6-2.6l-2.3-3.8a1.5 1.5 0 0 1 2.4-1.8l1.3 1.5" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function HeartIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M12 20s-7-4.3-7-9a4 4 0 0 1 7-2.6A4 4 0 0 1 19 11c0 4.7-7 9-7 9Z" stroke="currentColor" strokeWidth={1.5} strokeLinejoin="round" />
    </svg>
  );
}

export function ImageGlyphIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <rect x="3" y="4" width="18" height="16" rx="3" stroke="currentColor" strokeWidth={1.4} />
      <circle cx="8.5" cy="9.5" r="1.8" stroke="currentColor" strokeWidth={1.4} />
      <path d="m4 17 4.5-4.5 3 3 3.5-3.5L20 17" stroke="currentColor" strokeWidth={1.4} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function PhoneIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M5 4h3.2l1.6 4-2 1.5a11.5 11.5 0 0 0 5.2 5.2l1.5-2 4 1.6V18a2 2 0 0 1-2.2 2C9.4 19.3 4.7 14.6 4 6.2A2 2 0 0 1 5 4Z" stroke="currentColor" strokeWidth={1.5} strokeLinejoin="round" />
    </svg>
  );
}

export function MailIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <rect x="3" y="5" width="18" height="14" rx="3" stroke="currentColor" strokeWidth={1.5} />
      <path d="m4.5 8 6.4 4.6a2 2 0 0 0 2.2 0L19.5 8" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" />
    </svg>
  );
}

export function ClockIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <circle cx="12" cy="12" r="8.4" stroke="currentColor" strokeWidth={1.5} />
      <path d="M12 7.4V12l3 1.8" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function ChatIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M4 7.5A2.5 2.5 0 0 1 6.5 5h11A2.5 2.5 0 0 1 20 7.5v6a2.5 2.5 0 0 1-2.5 2.5H11l-4.4 3.2V16h-.1A2.5 2.5 0 0 1 4 13.5v-6Z" stroke="currentColor" strokeWidth={1.5} strokeLinejoin="round" />
    </svg>
  );
}

export function SirenIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" {...props}>
      <path d="M6 17v-4a6 6 0 0 1 12 0v4" stroke="currentColor" strokeWidth={1.5} strokeLinejoin="round" />
      <rect x="4" y="17" width="16" height="3.2" rx="1.6" stroke="currentColor" strokeWidth={1.5} />
      <path d="M12 3v1.8M4.6 6.2l1.3 1.3M19.4 6.2l-1.3 1.3" stroke="currentColor" strokeWidth={1.5} strokeLinecap="round" />
    </svg>
  );
}
