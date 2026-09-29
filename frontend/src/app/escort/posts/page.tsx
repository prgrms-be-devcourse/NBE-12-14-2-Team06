'use client';

import { Suspense } from 'react';
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

const LOADING_FALLBACK = (
    <AppShell>
        <section className="bg-white py-[100px] text-center">
            <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
    </AppShell>
);

/** useRequireAuth 가 next 파라미터를 만들려고 내부에서 useSearchParams() 를 씁니다.
 * useSearchParams() 는 빌드 시점 미리 그리기(prerender)에서 쿼리 값을 알 수 없어
 * 반드시 <Suspense> 경계 안에서만 쓸 수 있어서, 이 훅을 부르는 부분을 따로 뺐습니다. */
function Guard() {
    const { loading, user } = useRequireAuth('ESCORT');

    if (loading) return LOADING_FALLBACK;
    if (!user) return null;

    return (
        <AppShell>
            <PostListPage detailBasePath="/escort/posts" />
        </AppShell>
    );
}

export default function Page() {
    return (
        <Suspense fallback={LOADING_FALLBACK}>
            <Guard />
        </Suspense>
    );
}