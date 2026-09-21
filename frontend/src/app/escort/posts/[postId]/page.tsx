'use client';

import dynamic from 'next/dynamic';

/** CSR 진입점 — /escort/posts/[postId] (동행매니저 공고 상세) */
const PostDetailPage = dynamic(
    () => import('@/features/post').then((m) => m.PostDetailPage),
    {
        ssr: false,
        loading: () => <div className="min-h-screen bg-white" />,
    },
);

export default function Page() {
    return <PostDetailPage viewer="escort" />;
}