import type { ComponentType, SVGProps } from 'react';

/** 고객센터 상단의 연락 수단 카드 (대표 전화 · 대표 이메일 · 운영 시간) */
export type ContactChannel = {
  icon: ComponentType<SVGProps<SVGSVGElement>>;
  /** 카드 제목 (예: "대표 전화") */
  title: string;
  /** 크게 보여줄 값 (예: "1544-0808") */
  value: string;
  /** 값 아래 보조 설명 */
  description: string;
  /** 값을 누르면 걸리는 링크 (tel:, mailto:). 없으면 일반 텍스트로 보여줍니다. */
  href?: string;
};

/** 1:1 문의 폼의 문의 유형 */
export type InquiryCategory = {
  value: string;
  label: string;
};

/** "이럴 땐 이렇게" 안내 카드 */
export type SupportGuide = {
  icon: ComponentType<SVGProps<SVGSVGElement>>;
  title: string;
  description: string;
  /** 바로가기 링크 */
  action: { label: string; href: string };
};
