import { ChatIcon, ClockIcon, MailIcon, PhoneIcon, ShieldCheckIcon, SirenIcon } from '@/components/ui';
import type { ContactChannel, InquiryCategory, SupportGuide } from '../types';

/**
 * 회사 대표 연락처.
 *
 * ⚠️ 아직 확정된 값이 없어 임시 값을 넣어두었습니다.
 *    실제 번호·주소가 정해지면 이 상수만 바꾸면 화면 전체에 반영됩니다.
 */
export const COMPANY = {
  name: '주식회사 가지',
  /** 대표 이메일 — 고객센터에 반드시 노출됩니다. */
  email: 'gaji@devcourse.com',
  /** 대표 전화 — 고객센터에 반드시 노출됩니다. */
  phone: '1234-1234',
  /** 동행 중 긴급 상황 전용 회선 */
  emergencyPhone: '1234-1235',
  businessNumber: '123-45-67890',
} as const;

/** 전화번호에서 tel: 링크용 숫자만 남깁니다. */
export const telHref = (phone: string) => `tel:${phone.replace(/[^0-9+]/g, '')}`;

export const OPERATING_HOURS = {
  weekday: '평일 09:00 - 18:00',
  lunch: '점심시간 12:30 - 13:30',
  holiday: '주말·공휴일 휴무',
} as const;

/** 상단 연락 수단 3종 (전화 · 이메일 · 운영 시간) */
export const CONTACT_CHANNELS: ContactChannel[] = [
  {
    icon: PhoneIcon,
    title: '대표 전화',
    value: COMPANY.phone,
    description: `${OPERATING_HOURS.weekday} · ${OPERATING_HOURS.holiday}`,
    href: telHref(COMPANY.phone),
  },
  {
    icon: MailIcon,
    title: '대표 이메일',
    value: COMPANY.email,
    description: '접수 후 영업일 기준 1일 이내에 답변드려요',
    href: `mailto:${COMPANY.email}`,
  },
  {
    icon: ClockIcon,
    title: '운영 시간',
    value: '09:00 - 18:00',
    description: `${OPERATING_HOURS.lunch} · ${OPERATING_HOURS.holiday}`,
  },
];

/** "이럴 땐 이렇게" 안내 (FAQ 게시판 대신 바로가기 중심) */
export const SUPPORT_GUIDES: SupportGuide[] = [
  {
    icon: SirenIcon,
    title: '동행 중 긴급 상황이라면',
    description: '지금 동행이 진행 중인데 사고·건강 이상 등 도움이 급하다면 긴급 회선으로 바로 전화해주세요.',
    action: { label: `${COMPANY.emergencyPhone} 전화하기`, href: telHref(COMPANY.emergencyPhone) },
  },
  {
    icon: ShieldCheckIcon,
    title: '매칭·결제가 궁금하다면',
    description: '공고 상태와 결제 내역은 마이페이지에서 바로 확인할 수 있어요. 확인 후에도 문제가 있으면 문의해주세요.',
    action: { label: '마이페이지 가기', href: '/mypage' },
  },
  {
    icon: ChatIcon,
    title: '동행 매니저를 신고하고 싶다면',
    description: '부적절한 언행이나 약속 불이행이 있었다면 해당 동행 화면에서 신고를 접수해주세요. 24시간 내 확인합니다.',
    action: { label: '내 동행 내역 보기', href: '/mypage/applications' },
  },
];

export const INQUIRY_CATEGORIES: InquiryCategory[] = [
  { value: 'ACCOUNT', label: '회원가입 · 계정' },
  { value: 'MATCHING', label: '공고 등록 · 매칭' },
  { value: 'PAYMENT', label: '결제 · 정산 · 환불' },
  { value: 'REPORT', label: '신고 · 분쟁' },
  { value: 'PARTNERSHIP', label: '제휴 · 제안' },
  { value: 'ETC', label: '기타 문의' },
];
