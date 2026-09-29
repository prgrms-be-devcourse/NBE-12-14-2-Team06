'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /admin/members/[memberId] (관리자 회원 상세) */
const AdminMemberDetailPage = dynamic(
  () => import('@/features/admin').then((m) => m.AdminMemberDetailPage),
  {
    ssr: false,
    loading: () => <div className="min-h-screen bg-white" />,
  },
);

export default function Page() {
  return <AdminMemberDetailPage />;
}
