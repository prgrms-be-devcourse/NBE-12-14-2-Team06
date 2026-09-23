'use client';

import dynamic from 'next/dynamic';
import { AppShell } from '@/components/layout';
import { useRequireAuth } from '@/features/auth';

const PostListPage = dynamic(
    () => import('@/features/post').then((m) => m.PostListPage),
    {
        ssr: false,
        loading: () => <div className="min-h-screen bg-white" />,
    },
);

export default function Page() {
    const { loading, user } = useRequireAuth('ESCORT');

    if (loading) {
        return (
            <AppShell>
                <section className="bg-white py-[100px] text-center">
                    <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
                </section>
            </AppShell>
        );
    }
    if (!user) return null;

    return (
        <AppShell>
            <PostListPage detailBasePath="/escort/posts" />
        </AppShell>
    );
}