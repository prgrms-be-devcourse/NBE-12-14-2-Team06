import type { LabelTone } from '@/features/post';
import type { EducationVideoDto, VideoWatchStatus } from '../types';

export const WATCH_STATUS_INFO: Record<VideoWatchStatus, { label: string; tone: LabelTone }> = {
  completed: { label: '시청 완료', tone: 'green' },
  watching: { label: '시청 중', tone: 'blue' },
  notStarted: { label: '미시청', tone: 'gray' },
};

export function watchStatus(video: Pick<EducationVideoDto, 'completed' | 'maxWatchedSec'>): VideoWatchStatus {
  if (video.completed) return 'completed';
  return video.maxWatchedSec > 0 ? 'watching' : 'notStarted';
}

/** 시청 진행률 (0 ~ 100, 정수) */
export function watchPercent(video: Pick<EducationVideoDto, 'completed' | 'maxWatchedSec' | 'durationSec'>): number {
  if (video.completed) return 100;
  if (video.durationSec <= 0) return 0;
  return Math.min(100, Math.floor((video.maxWatchedSec / video.durationSec) * 100));
}

/** 필수 영상을 모두 봤는지 = 백엔드가 교육 이수(verified) 처리하는 조건 */
export function isEducationCompleted(videos: EducationVideoDto[]): boolean {
  return videos.filter((video) => video.required).every((video) => video.completed);
}
