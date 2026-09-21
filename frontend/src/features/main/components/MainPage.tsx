'use client';

import { AppShell } from '@/components/layout';
import HeroSection from './HeroSection';
import AboutSection from './AboutSection';
import ChangeSection from './ChangeSection';
import PersonaSection from './PersonaSection';
import StepSection from './StepSection';

/** 메인(랜딩) 페이지 — Figma 공통_메인 564:18017 */
export default function MainPage() {
  return (
    <AppShell>
      <HeroSection />
      <AboutSection />
      <ChangeSection />
      <PersonaSection />
      <StepSection />
    </AppShell>
  );
}
