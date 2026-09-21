'use client';

import { AppShell } from '@/components/layout';
import SupportHero from './SupportHero';
import ContactSection from './ContactSection';
import InquirySection from './InquirySection';

/** 고객센터 — 대표 전화·대표 이메일 안내와 1:1 문의 접수 */
export default function SupportPage() {
  return (
    <AppShell>
      <SupportHero />
      <ContactSection />
      <InquirySection />
    </AppShell>
  );
}
