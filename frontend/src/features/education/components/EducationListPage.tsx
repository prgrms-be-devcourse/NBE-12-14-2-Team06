'use client';

import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { MyPageShell } from '@/features/mypage';
// 배럴(@/features/mypage)이 이 함수를 내보내지 않아서 api 모듈을 직접 가져옵니다.
import { fetchMyEscortProfile } from '@/features/mypage/api';
import { CardButton, StatusLabel } from '@/features/post';
import { fetchEducationVideos } from '../api';
import { formatDuration } from '../lib/time';
import { WATCH_STATUS_INFO, watchPercent, watchStatus } from '../model/status';
import type { EducationVideoDto } from '../types';
import WatchProgressBar from './WatchProgressBar';

const HEADING = {
  title: '교육 영상',
  description: '필수 교육 영상을 모두 시청하면 교육 이수가 완료되고 공고에 지원할 수 있습니다.',
};

/** 영상 카드 아래 버튼 문구 */
const ACTION_LABEL = {
  completed: '다시 보기',
  watching: '이어 보기',
  notStarted: '시청하기',
} as const;

/**
 * 마이페이지 — 교육 영상 목록
 *
 * GET /api/v1/education-videos 로 영상과 내 진행 상황을 함께 받습니다.
 * 교육 이수 여부는 동행 매니저 프로필(GET /users/profile/escort)의 verified 를 그대로 씁니다.
 * (영상별 completed 로 계산하면 시드 데이터처럼 영상을 안 보고 이수 처리된 계정에서 어긋납니다)
 */
export default function EducationListPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const [state, setState] = useState<{ videos?: EducationVideoDto[]; verified?: boolean; error?: string }>();

  useEffect(() => {
    let ignore = false;
    Promise.all([fetchEducationVideos(), fetchMyEscortProfile()])
      .then(([videos, profile]) => !ignore && setState({ videos, verified: profile.verified }))
      .catch((error: Error) => !ignore && setState({ error: error.message }));
    return () => {
      ignore = true;
    };
  }, []);

  if (authLoading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  if (!state?.videos) {
    return (
      <MyPageShell>
        <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
          <SectionHeading title={HEADING.title} description={HEADING.description} className="mb-0" />
          <p className="text-base font-semibold text-brand-muted">{state?.error ? `불러오지 못했습니다. (${state.error})` : '불러오는 중입니다.'}</p>
        </div>
      </MyPageShell>
    );
  }

  const videos = state.videos;
  const required = videos.filter((video) => video.required);
  const requiredDone = required.filter((video) => video.completed).length;
  const educated = state.verified ?? false;

  return (
    <MyPageShell>
      <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
        <SectionHeading title={HEADING.title} description={HEADING.description} className="mb-0" />

        {/* 이수 현황 */}
        <section
          aria-label="교육 이수 현황"
          className="flex w-full max-w-[910px] flex-col items-center gap-4 rounded-[30px] border border-line-soft bg-white px-8 py-6 text-center shadow-card sm:flex-row sm:justify-between sm:text-left"
        >
          <div className="flex flex-col gap-2 text-brand">
            <div className="flex flex-wrap items-center justify-center gap-2.5 sm:justify-start">
              <span className="text-base leading-5 font-semibold">교육 이수 상태</span>
              <StatusLabel tone={educated ? 'green' : 'red'}>{educated ? '이수 완료' : '미이수'}</StatusLabel>
            </div>
            <p className="text-xs leading-4 font-medium">
              {educated ? '교육 이수가 완료되었습니다. 이제 공고에 지원할 수 있습니다.' : '필수 영상을 모두 시청해야 공고에 지원할 수 있습니다.'}
            </p>
          </div>
          <p className="shrink-0 text-2xl leading-6 font-bold text-brand">
            필수 {requiredDone} / {required.length}
          </p>
        </section>

        {videos.length > 0 ? (
          <ul className="grid w-full max-w-[910px] gap-4 sm:grid-cols-2">
            {videos.map((video) => {
              const status = watchStatus(video);
              const info = WATCH_STATUS_INFO[status];
              return (
                <li key={video.videoId}>
                  <article className="flex h-full flex-col gap-4 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
                    <div className="flex flex-wrap items-center gap-1.5">
                      <StatusLabel tone={video.required ? 'purple' : 'gray'}>{video.required ? '필수' : '선택'}</StatusLabel>
                      <StatusLabel tone={info.tone}>{info.label}</StatusLabel>
                    </div>
                    <div className="flex flex-col gap-1 text-brand">
                      <h3 className="text-lg leading-6 font-bold">{video.title}</h3>
                      <p className="text-xs leading-4 font-medium text-brand-muted">영상 길이 {formatDuration(video.durationSec)}</p>
                    </div>
                    <WatchProgressBar percent={watchPercent(video)} label="시청 진행률" />
                    <div className="mt-auto flex justify-end">
                      <CardButton variant={status === 'completed' ? 'ghost' : 'solid'} href={`/mypage/education/${video.videoId}`}>
                        {ACTION_LABEL[status]}
                      </CardButton>
                    </div>
                  </article>
                </li>
              );
            })}
          </ul>
        ) : (
          <div className="flex min-h-[130px] w-full max-w-[910px] flex-col items-center justify-center gap-2 rounded-[30px] border border-line bg-white px-6 py-10 text-center shadow-card">
            <p className="text-lg leading-6 font-semibold text-brand">등록된 교육 영상이 없습니다.</p>
          </div>
        )}
      </div>
    </MyPageShell>
  );
}
