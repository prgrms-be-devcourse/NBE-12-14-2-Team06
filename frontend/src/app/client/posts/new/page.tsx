'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /client/posts/new (공고 작성) */
const PostFormPage = dynamic(() => import('@/features/client').then((m) => m.PostFormPage), {
  ssr: false,
  loading: () => <div className="min-h-screen bg-white" />,
});

export default function Page() {
  return <PostFormPage />;
}
