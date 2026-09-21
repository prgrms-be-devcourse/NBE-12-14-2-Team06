'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/[postId]/edit (공고 수정) */
const PostFormPage = dynamic(() => import('@/features/client').then((m) => m.PostFormPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PostFormPage />;
}
