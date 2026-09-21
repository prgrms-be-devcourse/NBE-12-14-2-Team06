'use client';

import dynamic from 'next/dynamic';
import { AppShell } from '@/components/layout';
import { MOCK_USER } from '@/lib/mockSession';

const PostListPage = dynamic(
    () => import('@/features/post').then((m) => m.PostListPage),
    {
        ssr: false,
        loading: () => <div className="min-h-screen bg-white" />,
    },
);

export default function Page() {
    return (
        <AppShell user={MOCK_USER}>
            <PostListPage detailBasePath="/escort/posts" />
        </AppShell>
    );
}