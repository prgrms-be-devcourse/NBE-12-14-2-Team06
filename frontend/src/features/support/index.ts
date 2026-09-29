/**
 * support(고객센터) 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 *   ✅ import { SupportPage } from '@/features/support';
 *   ❌ import SupportPage from '@/features/support/components/SupportPage';
 */
export { default as SupportPage } from './components/SupportPage';
export { COMPANY, OPERATING_HOURS, telHref } from './model';
export type { ContactChannel, InquiryCategory, SupportGuide } from './types';
