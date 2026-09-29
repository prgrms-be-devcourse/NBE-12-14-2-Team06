import { api, apiPost, type ApiError } from '@/lib/api';
import type { EducationVideoDto, WatchLogDto } from './types';

/** 교육 영상 목록 + 내 진행 상황 — GET /api/v1/education-videos (동행 매니저 전용) */
export function fetchEducationVideos(): Promise<EducationVideoDto[]> {
  return api<EducationVideoDto[]>('/api/v1/education-videos');
}

/** 교육 영상 한 건 + 내 진행 상황 — GET /api/v1/education-videos/{videoId} (동행 매니저 전용) */
export function fetchEducationVideo(videoId: number): Promise<EducationVideoDto> {
  return api<EducationVideoDto>(`/api/v1/education-videos/${videoId}`);
}

/**
 * 시청 기록(하트비트) — POST /api/v1/education-videos/{videoId}/watchlogs
 * 재생 시작 시, 재생 중 5초마다, 영상이 끝났을 때 현재 재생 위치를 보냅니다.
 *
 * ⚠️ 서버는 "직전 기록 이후 흐른 시간(최대 15초) × 1.2 + 2초" 까지만 앞으로 간 위치를 인정합니다.
 *    건너뛰기·배속으로 그보다 멀리 가면 거부되고, 응답의 maxWatchedSec 는 그대로 남습니다.
 *    이미 교육을 이수(verified)했다면 기록하지 않고 현재 상태만 돌려줍니다.
 */
export function recordWatchLog(videoId: number, positionSec: number, { keepalive = false } = {}): Promise<WatchLogDto> {
  // keepalive: 탭을 닫거나 새로고침하는 순간 보내는 마지막 기록도 페이지가 사라진 뒤까지 끝까지 전송되게 합니다.
  if (keepalive) {
    return api<WatchLogDto>(`/api/v1/education-videos/${videoId}/watchlogs`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ positionSec }),
      keepalive: true,
    });
  }
  return apiPost<WatchLogDto>(`/api/v1/education-videos/${videoId}/watchlogs`, { positionSec });
}

/** 교육을 이수하지 않은 동행 매니저가 지원했을 때(ApplicationService.apply) 백엔드가 던지는 상태코드 */
const EDUCATION_REQUIRED_STATUS = '403';

/** 공고 지원(applyToPost) 실패가 "교육 미이수" 때문인지 판단합니다. 교육 영상 화면으로 안내할 때 씁니다. */
export function isEducationRequiredError(error: unknown): boolean {
  return (error as Partial<ApiError>)?.statusCode === EDUCATION_REQUIRED_STATUS;
}
