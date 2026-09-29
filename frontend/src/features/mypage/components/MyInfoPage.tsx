'use client';

import { AppShell } from '@/components/layout';
import { useRequireAuth, type CurrentUser } from '@/features/auth';
import type { MyPageRole } from '../types';
import ClientInfoView from './ClientInfoView';
import EscortInfoView from './EscortInfoView';
import MyPageShell from './MyPageShell';

const REQUIRED_ROLE: Record<MyPageRole, CurrentUser['role']> = {
  escort: 'ESCORT',
  client: 'CLIENT',
};

/**
 * 마이페이지 — 내 정보 (Figma 동행매니저_마이페이지 210:1079 · 의뢰인_마이페이지 210:746)
 *
 * - role='escort' ("/mypage"): 실제 API 로 연결했습니다 → EscortInfoView.tsx
 * - role='client' ("/client"): 실제 API 로 연결했습니다 → ClientInfoView.tsx
 */
export default function MyInfoPage({ role = 'escort' }: { role?: MyPageRole }) {
  const { loading, user } = useRequireAuth(REQUIRED_ROLE[role]);

  if (loading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  return (
    <MyPageShell role={role}>
      {role === 'escort' ? <EscortInfoView /> : <ClientInfoView />}
    </MyPageShell>
  );
}
