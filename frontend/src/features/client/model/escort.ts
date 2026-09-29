import { PROGRESS_ORDER, type EscortProgress } from '@/features/application';
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

/**
 * 백엔드 EscortProgress(GET .../progress 로 실제 조회) → 화면 5단계.
 * PROGRESS_ORDER: NOT_STARTED, DEPARTED, TO_HOSPITAL, AT_HOSPITAL, TO_HOME, ARRIVED_HOME.
 * 병원 도착(AT_HOSPITAL)부터 GOING_HOME 이 true 로 바뀌어야 해서 arrived/finishing 을 따로 둡니다.
 */
const PROGRESS_TO_STAGE: Record<EscortProgress, ClientEscortStage> = {
  NOT_STARTED: 'ready',
  DEPARTED: 'ongoing',
  TO_HOSPITAL: 'ongoing',
  AT_HOSPITAL: 'arrived',
  TO_HOME: 'finishing',
  ARRIVED_HOME: 'done',
};

/**
 * 실제 API 로 만든 의뢰인 동행 현황.
 * ⚠️ 지도의 "최근 업데이트"·실시간 위치는 아직 API 가 없어 MapCard 의 정적 이미지 그대로입니다.
 */
export function toClientEscortCase(
  post: PostDto,
  applicationId: number,
  manager: Manager,
  transport: string,
  progress: EscortProgress,
  reviewed: boolean,
): ClientEscortCase {
  const stage = PROGRESS_TO_STAGE[progress];
  const doneCount = PROGRESS_ORDER.indexOf(progress) + 1;
  return {
    applicationId,
    postId: post.id,
    stage,
    postCompleted: post.postStatus === '동행 완료',
    reviewed,
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
    timeline: TIMELINE_BASE.map((step, index) => ({ ...step, done: index < doneCount })),
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
