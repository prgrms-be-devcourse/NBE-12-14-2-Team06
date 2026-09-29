/** 이동 방향. 갈 때(병원으로)·올 때(집으로) */
export type RideDirection = 'TO_HOSPITAL' | 'TO_HOME';

/** 이동수단 선택 상태 */
export type RideStatus = 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED';

/** 이동수단. 선택 전이면 null */
export type RideSelect = 'WALK' | 'BUS' | 'TAXI' | 'OWN_CAR';

/** 백엔드 이동 응답 (RideResponse) — GET /api/v1/rides/posts/{postId} 목록의 한 항목 */
export type RideDto = {
  id: number;
  direction: RideDirection;
  status: RideStatus;
  selected: RideSelect | null;
  originLat: number | null;
  originLnt: number | null;
  originAddr: string | null;
  destLat: number | null;
  destLnt: number | null;
  destAddr: string | null;
};
