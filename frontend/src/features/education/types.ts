/**
 * 교육 영상 + 내 시청 진행 상황 (백엔드 EducationVideoResponse)
 * GET /api/v1/education-videos · GET /api/v1/education-videos/{videoId}
 */
export type EducationVideoDto = {
  videoId: number;
  title: string;
  /** 영상 주소. 시드 데이터는 프론트 public 폴더의 "/videos/sample_video.mp4" 입니다. */
  url: string;
  durationSec: number;
  /** 필수 영상 여부. 필수 영상을 모두 시청해야 교육 이수(verified) 처리됩니다. */
  required: boolean;
  /** 서버가 정상 시청으로 인정한 재생 위치 (이어보기 위치) */
  maxWatchedSec: number;
  completed: boolean;
};

/** 시청 기록(하트비트) 응답 (백엔드 WatchProgressLogCreateResponse) — POST /api/v1/education-videos/{videoId}/watchlogs */
export type WatchLogDto = {
  maxWatchedSec: number;
  completed: boolean;
  /** 교육 이수 여부 (동행 매니저 프로필의 verified) */
  verified: boolean;
};

/** 화면에서 쓰는 영상 시청 상태 */
export type VideoWatchStatus = 'completed' | 'watching' | 'notStarted';
