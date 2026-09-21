'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /posts/[postId] (공통 공고 상세) */
const PostDetailPage = dynamic(
    () => import('@/features/post').then((m) => m.PostDetailPage),
    {
      ssr: false,
      loading: () => <div className="min-h-screen bg-white" />,
    },
);

export default function Page() {
  return <PostDetailPage viewer="common" />;
}
