'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /posts (공고 목록) */
const PostListPage = dynamic(() => import('@/features/post').then((m) => m.PostListPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PostListPage />;
}
