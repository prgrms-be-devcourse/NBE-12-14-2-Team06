'use client';

import Link from 'next/link';
import { useRef } from 'react';
import { formatClock } from '../lib/time';
import { useWatchTracker } from '../lib/useWatchTracker';
import { watchPercent } from '../model/status';
import type { EducationVideoDto } from '../types';
import WatchProgressBar from './WatchProgressBar';

const RULES = [
  '재생 중 5초마다 시청 기록이 자동으로 저장되며, 다음에 들어오면 이어서 볼 수 있습니다.',
  '아직 시청하지 않은 구간으로 건너뛰거나 배속으로 시청하면 인정되지 않습니다.',
  '다른 화면(탭)으로 이동하면 영상이 일시정지됩니다.',
];

type Props = {
  video: EducationVideoDto;
  /** 교육 이수 여부 (동행 매니저 프로필의 verified). 이수했으면 기록 없이 자유롭게 봅니다. */
  verified: boolean;
};

/** 교육 영상 재생기 + 시청 진행 상황. 영상 정보를 다 불러온 뒤에만 그립니다. */
export default function EducationPlayer({ video, verified }: Props) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const tracker = useWatchTracker(videoRef, {
    videoId: video.videoId,
    initialMaxWatchedSec: video.maxWatchedSec,
    initialCompleted: video.completed,
    initialVerified: verified,
  });

  const percent = watchPercent({ ...video, maxWatchedSec: tracker.maxWatchedSec, completed: tracker.completed });
  // 방금 시청을 마쳐 하트비트 응답으로 완료를 받은 경우에만 이수 결과를 안내합니다.
  const justCompleted = tracker.completed && !video.completed;
  // 이미 이수한 동행 매니저는 서버가 기록을 저장하지 않아서 진행률이 더 오르지 않습니다.
  const replayOnly = verified && !tracker.completed;

  return (
    <div className="flex w-full max-w-[910px] flex-col gap-5">
      <div className="overflow-hidden rounded-[30px] bg-black shadow-card">
        <video
          ref={videoRef}
          src={video.url}
          controls
          playsInline
          preload="metadata"
          disablePictureInPicture
          controlsList="nodownload noplaybackrate noremoteplayback"
          onContextMenu={(event) => event.preventDefault()}
          className="aspect-video w-full"
        >
          브라우저가 영상 재생을 지원하지 않습니다.
        </video>
      </div>

      <section
        aria-label="시청 진행 상황"
        className="flex flex-col gap-3 rounded-[30px] border border-line-soft bg-white px-8 py-6 shadow-card"
      >
        <WatchProgressBar percent={percent} label="시청 진행률" />
        <p className="text-xs leading-4 font-medium text-brand-muted">
          {tracker.completed
            ? '시청을 완료한 영상입니다. 원하는 구간을 자유롭게 다시 볼 수 있습니다.'
            : replayOnly
              ? '교육 이수를 완료해 시청 기록이 더 저장되지 않습니다. 원하는 구간을 자유롭게 볼 수 있습니다.'
              : `인정된 시청 위치 ${formatClock(tracker.maxWatchedSec)} / ${formatClock(video.durationSec)}`}
        </p>
        {tracker.notice && !tracker.completed && !replayOnly && (
          <p role="status" className="rounded-[15px] bg-line-soft px-4 py-2.5 text-xs leading-4 font-semibold text-brand">
            {tracker.notice}
          </p>
        )}
        {tracker.error && (
          <p role="alert" className="rounded-[15px] bg-[#ffe3e3] px-4 py-2.5 text-xs leading-4 font-semibold text-[#b91d1d]">
            시청 기록을 저장하지 못했습니다. ({tracker.error})
          </p>
        )}
      </section>

      {justCompleted && (
        <section
          role="status"
          className="flex flex-col items-center gap-3 rounded-[30px] border border-[#e6ffe5] bg-[#f4fff4] px-8 py-6 text-center"
        >
          <p className="text-lg leading-6 font-bold text-[#209d37]">
            {tracker.verified ? '교육 이수가 완료되었습니다!' : '영상 시청을 완료했습니다!'}
          </p>
          <p className="text-xs leading-4 font-medium text-brand">
            {tracker.verified ? '이제 공고에 지원할 수 있습니다.' : '남은 필수 영상을 모두 시청하면 교육 이수가 완료됩니다.'}
          </p>
          <Link
            href={tracker.verified ? '/posts' : '/mypage/education'}
            className="flex h-[27px] items-center rounded-[30px] bg-brand px-7 text-xs leading-4 font-semibold text-white transition-colors hover:bg-brand-hover"
          >
            {tracker.verified ? '공고 보러 가기' : '교육 영상 목록'}
          </Link>
        </section>
      )}

      <section aria-label="시청 안내" className="rounded-[30px] bg-line-soft px-8 py-6">
        <h3 className="mb-3 text-base leading-5 font-semibold text-brand">시청 안내</h3>
        <ul className="flex list-disc flex-col gap-1.5 pl-5 text-xs leading-5 font-medium text-brand">
          {RULES.map((rule) => (
            <li key={rule}>{rule}</li>
          ))}
        </ul>
      </section>
    </div>
  );
}
