import type { PostDto } from '@/features/post';
import { formatDotDateTime, formatScheduleLabel } from '../lib/date';
import type { ClientEscortCase, ClientEscortStage, Manager } from '../types';

/*
 * 의뢰인 동행 현황·보고서·리뷰 화면이 함께 쓰는 변환부입니다.
 * (Figma 의뢰인_매칭 동행 현황 431:4289 · 431:4119 · 464:3497 · 506:2059 · 431:3953,
 *  보고서 조회 459:3004, 리뷰 작성 459:3023)
 * ⚠️ 타임라인 설명 문구는 디자인에서 흐리게 처리되어 읽히지 않는 부분은 임시로 적었습니다.
 */
const TIMELINE_BASE = [
  { label: '동행 시작 전', description: '동행 매니저와 매칭이 완료되었습니다.' },
  { label: '출발', description: '매니저가 출발 했습니다.' },
  { label: '병원 이동 중', description: '동행 매니저와 안전하게 병원으로 이동 중입니다.' },
  { label: '병원 도착', description: '병원에 도착하여 접수를 도와주고 있습니다.' },
  { label: '귀가 중', description: '진료를 마치고 안전하게 귀가를 돕고 있습니다.' },
  { label: '귀가 완료', description: '동행이 완료되었습니다.' },
];

/** 타임라인 6단계 중 3단계(ready/ongoing/done)마다 몇 번째까지 완료로 볼지 */
const STAGE_DONE_COUNT: Record<'ready' | 'ongoing' | 'done', number> = { ready: 1, ongoing: 3, done: 6 };

/**
 * 백엔드 PostDto.postStatus(한글) → 진행 단계 3종.
 * ⚠️ 동행 진행 단계(EscortProgress)를 "읽는" API 가 없어서(쓰기 PATCH만 있음) 공고 상태로 대신합니다.
 */
function toClientEscortStage(postStatus: string): 'ready' | 'ongoing' | 'done' {
  if (postStatus === '동행 진행 중') return 'ongoing';
  if (postStatus === '동행 완료') return 'done';
  return 'ready'; // '매칭 완료' 및 그 외 상태의 기본값
}

/**
 * 실제 API 로 만든 의뢰인 동행 현황.
 * ⚠️ 타임라인 단계별 "시각"과 지도의 "최근 업데이트"는 EscortProgress 를 읽는 API 가 없어 비워 둡니다
 *    (예전엔 모의 값을 넣었습니다). 지도의 실시간 위치도 API 가 없어 MapCard 의 정적 이미지입니다.
 */
export function toClientEscortCase(post: PostDto, applicationId: number, manager: Manager, transport: string): ClientEscortCase {
  const stage = toClientEscortStage(post.postStatus);
  return {
    applicationId,
    postId: post.id,
    stage,
    title: post.title,
    hospitalName: post.hospitalName,
    region: post.region,
    scheduleLabel: formatScheduleLabel(post.escortStartAt),
    durationLabel: `약 ${post.escortHours}시간`,
    payLabel: `시급 ${post.hourlyPay.toLocaleString()}원`,
    startAt: formatDotDateTime(post.escortStartAt),
    endAt: formatDotDateTime(post.escortEndAt),
    transport,
    meetingPlace: post.pickupAddress,
    pickupPoint: { name: '집', lat: post.pickupLat, lng: post.pickupLng },
    hospitalPoint: { name: post.hospitalName, lat: post.hospitalLat, lng: post.hospitalLng },
    manager,
    timeline: TIMELINE_BASE.map((step, index) => ({ ...step, done: index < STAGE_DONE_COUNT[stage] })),
  };
}

/** 진행 요약 카드의 단계별 문구 (Figma "진행 요약") */
export const STAGE_VIEW: Record<
  ClientEscortStage,
  { badge: { text: string; tone: 'green' | 'blue' | 'strong' }; notice: [string, string] }
> = {
  ready: {
    badge: { text: '매칭 완료', tone: 'green' },
    notice: ['동행이 시작되면 실시간 위치 공유가 활성화됩니다.', '동행 시작 후, 현재 위치와 이동 경로가 실시간으로 공유됩니다.'],
  },
  ongoing: {
    badge: { text: '진행 중', tone: 'blue' },
    notice: ['실시간 위치를 확인할 수 있습니다.', '동행 중 매니저의 위치가 실시간으로 공유되며, 동행 완료 시 위치 공유가 종료됩니다.'],
  },
  arrived: {
    badge: { text: '진행 중', tone: 'blue' },
    notice: ['실시간 위치를 확인할 수 있습니다.', '동행 중 매니저의 위치가 실시간으로 공유되며, 동행 완료 시 위치 공유가 종료됩니다.'],
  },
  finishing: {
    badge: { text: '진행 중', tone: 'blue' },
    notice: ['실시간 위치를 확인할 수 있습니다.', '동행 중 매니저의 위치가 실시간으로 공유되며, 동행 완료 시 위치 공유가 종료됩니다.'],
  },
  done: {
    badge: { text: '완료', tone: 'strong' },
    notice: ['위치 공유가 종료되었습니다.', '동행이 완료되어 실시간 위치 공유가 중지되었습니다.'],
  },
};
