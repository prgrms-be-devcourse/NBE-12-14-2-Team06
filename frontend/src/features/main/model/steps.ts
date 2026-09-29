import type { UsageStep } from '../types';

/** 요청자(어르신·보호자) 플로우 */
export const REQUESTER_STEPS: UsageStep[] = [
  { num: '01', title: '동행 요청하기', description: '원하는 날짜, 병원, 내용을 올려보세요' },
  { num: '02', title: '매칭되기', description: '조건에 맞는 매니저와 연결돼요' },
  { num: '03', title: '함께 병원 ', highlight: '가지', description: '약속된 시간에 만나 안전하게 병원에 가요' },
];

/** 동행 매니저 플로우 */
export const MANAGER_STEPS: UsageStep[] = [
  { num: '01', title: '동행 지원하기', description: '원하는 날짜에 원하는 지역을 선택해보세요' },
  { num: '02', title: '매칭되기', description: '선택한 동행자와 연결돼요' },
  { num: '03', title: '함께 병원 ', highlight: '가지', description: '약속된 시간에 만나 안전하게 병원에 가요' },
];
