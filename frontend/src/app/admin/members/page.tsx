'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /admin/members (관리자 회원 관리) */
const AdminMembersPage = dynamic(() => import('@/features/admin').then((m) => m.AdminMembersPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <AdminMembersPage />;
}
