/* ── 서버 DTO ─────────────────────────────────────────────── */

/**
 * GET /api/v1/applications/{applicationId}/location 응답 (동행 매니저의 가장 최근 위치 1건)
 * updatedAt 은 시간대가 붙은 ISO-8601("2026-10-10T10:15:30+09:00" 또는 "...Z") 이어야 시각이 어긋나지 않습니다.
 */
export type EscortLocationDto = {
  lat: number;
  lng: number;
  /** 위치 측정 오차(미터). 모르면 null */
  accuracy: number | null;
  updatedAt: string;
};

/** POST /api/v1/applications/{applicationId}/location 요청 본문 */
export type SendLocationRequest = {
  lat: number;
  lng: number;
  /** 위치 측정 오차(미터) */
  accuracy?: number;
};

/* ── 화면용 ───────────────────────────────────────────────── */

/** 위도·경도 한 점 */
export type LatLng = {
  lat: number;
  lng: number;
};

/** 의뢰인 화면에 보여줄 동행 매니저의 위치 */
export type EscortLocation = {
  position: LatLng;
  /** "오후 3:25:10" — 지도 "최근 업데이트"에 그대로 씁니다. */
  updatedAtLabel: string;
};

/**
 * 동행 매니저 기기의 위치 공유 상태
 * - idle: 공유 시간이 아님 / starting: 위치를 처음 받아오는 중 / sharing: 서버로 전송 중
 * - denied: 위치 권한 거부 / unsupported: 기기가 위치 기능을 지원하지 않음 / error: 전송·측정 실패
 */
export type ShareStatus = 'idle' | 'starting' | 'sharing' | 'denied' | 'unsupported' | 'error';
