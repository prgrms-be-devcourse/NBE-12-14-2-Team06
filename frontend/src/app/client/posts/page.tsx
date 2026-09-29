'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts (내가 작성한 공고) */
const ClientPostsPage = dynamic(() => import('@/features/client').then((m) => m.ClientPostsPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <ClientPostsPage />;
}
