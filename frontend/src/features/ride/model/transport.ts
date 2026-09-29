import type { RideDto } from '../types';

/** 공고 작성 폼의 이동수단 선택지(client/model/postForm.ts)와 같은 문구를 씁니다. */
const RIDE_SELECT_LABELS: Record<string, string> = { WALK: '도보', BUS: '대중교통', TAXI: '택시', OWN_CAR: '자가용' };

/** 갈 때(TO_HOSPITAL)·올 때(TO_HOME) 이동수단을 한 줄로 합칩니다. 아직 선택 전이면 "미정"으로 표시합니다. */
export function formatTransport(rides: RideDto[]): string {
  const label = (direction: 'TO_HOSPITAL' | 'TO_HOME') => {
    const ride = rides.find((item) => item.direction === direction);
    return ride?.selected ? (RIDE_SELECT_LABELS[ride.selected] ?? ride.selected) : '미정';
  };
  return `갈 때 ${label('TO_HOSPITAL')} · 올 때 ${label('TO_HOME')}`;
}
