'use client';

import type { MyPageRole } from '../types';
import ClientInfoView from './ClientInfoView';
import EscortInfoView from './EscortInfoView';
import MyPageShell from './MyPageShell';

/**
 * 마이페이지 — 내 정보 (Figma 동행매니저_마이페이지 210:1079 · 의뢰인_마이페이지 210:746)
 *
 * - role='escort' ("/mypage"): 실제 API 로 연결했습니다 → EscortInfoView.tsx
 * - role='client' ("/client"): 실제 API 로 연결했습니다 → ClientInfoView.tsx
 */
export default function MyInfoPage({ role = 'escort' }: { role?: MyPageRole }) {
  return (
    <MyPageShell role={role}>
      {role === 'escort' ? <EscortInfoView /> : <ClientInfoView />}
    </MyPageShell>
  );
}
