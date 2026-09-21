'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/complete (공고 등록 완료) */
const PostCompletePage = dynamic(() => import('@/features/client').then((m) => m.PostCompletePage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PostCompletePage />;
}
