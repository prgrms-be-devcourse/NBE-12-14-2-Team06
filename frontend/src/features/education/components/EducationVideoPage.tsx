'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { useRequireAuth } from '@/features/auth';
import { MyPageShell } from '@/features/mypage';
// 배럴(@/features/mypage)이 이 함수를 내보내지 않아서 api 모듈을 직접 가져옵니다.
import { fetchMyEscortProfile } from '@/features/mypage/api';
import { StatusLabel } from '@/features/post';
import { fetchEducationVideo } from '../api';
import { formatDuration } from '../lib/time';
import type { EducationVideoDto } from '../types';
import EducationPlayer from './EducationPlayer';

/**
 * 마이페이지 — 교육 영상 시청 (/mypage/education/[videoId])
 *
 * GET /api/v1/education-videos/{videoId} 로 영상과 이어보기 위치를 받고,
 * 재생 중에는 EducationPlayer 가 시청 기록(하트비트)을 보냅니다.
 * 교육 이수 여부는 동행 매니저 프로필(GET /users/profile/escort)의 verified 로 확인합니다.
 */
export default function EducationVideoPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const params = useParams<{ videoId: string }>();
  const videoId = Number(params.videoId);
  const [result, setResult] = useState<{ videoId: number; video?: EducationVideoDto; verified?: boolean; error?: string }>();

  useEffect(() => {
    if (!Number.isInteger(videoId) || videoId <= 0) return;
    let ignore = false;
    Promise.all([fetchEducationVideo(videoId), fetchMyEscortProfile()])
      .then(([video, profile]) => !ignore && setResult({ videoId, video, verified: profile.verified }))
      .catch((error: Error) => !ignore && setResult({ videoId, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [videoId]);

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

  const current = result?.videoId === videoId ? result : undefined;
  const invalidId = !Number.isInteger(videoId) || videoId <= 0;
  const video = current?.video;

  return (
    <MyPageShell>
      <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
        <div className="flex w-full max-w-[910px] flex-col gap-5">
          <Link
            href="/mypage/education"
            className="flex h-[45px] w-[155px] items-center justify-center gap-[6.48px] rounded-[24.3px] border border-line bg-white text-base font-semibold text-brand transition-colors hover:bg-line-soft"
          >
            {/* 아래를 향한 꺾쇠를 90° 돌려 왼쪽 화살표로 씁니다. */}
            <Image src="/icons/back-chevron.svg" alt="" width={11.465} height={6.4075} className="rotate-90" />
            목록으로
          </Link>

          {video && (
            <div className="flex flex-col gap-3 text-brand">
              <div className="flex flex-wrap items-center gap-1.5">
                <StatusLabel tone={video.required ? 'purple' : 'gray'}>{video.required ? '필수' : '선택'}</StatusLabel>
                <span className="text-xs leading-4 font-medium text-brand-muted">영상 길이 {formatDuration(video.durationSec)}</span>
              </div>
              <h2 className="text-[28px] leading-[34px] font-extrabold lg:text-4xl lg:leading-10">{video.title}</h2>
            </div>
          )}
        </div>

        {video ? (
          // 영상이 바뀌면 재생기(하트비트 상태)를 새로 만듭니다.
          <EducationPlayer key={video.videoId} video={video} verified={current?.verified ?? false} />
        ) : (
          <p className="text-base font-semibold text-brand-muted">
            {invalidId
              ? '존재하지 않는 교육 영상입니다.'
              : current?.error
                ? `불러오지 못했습니다. (${current.error})`
                : '불러오는 중입니다.'}
          </p>
        )}
      </div>
    </MyPageShell>
  );
}
