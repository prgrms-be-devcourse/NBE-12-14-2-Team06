import type { TimelineStep } from '@/features/escort';
import type { PostDto } from '@/features/post';
import { formatDotDateTime, formatScheduleLabel } from '../lib/date';
import type { ClientEscortCase, ClientEscortStage, Manager } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 의뢰인_매칭 동행 현황 431:4289 · 431:4119 · 464:3497 · 506:2059 · 431:3953,
 *    보고서 조회 459:3004, 리뷰 작성 459:3023 문구).
 *    진행 상태는 백엔드 EscortProgress / 신청 진행 API(PATCH /api/v1/applications/{id}/progress)와 연결할 자리입니다.
 *    타임라인 설명 문구는 디자인에서 흐리게 처리되어 읽히지 않는 부분은 임시로 적었습니다.
 */
export const MANAGER: Manager = {
  name: '나알바',
  rating: 4.8,
  completedCount: 52,
  region: '서울 강남구',
  tags: ['태그1', '태그2', '태그3'],
  intro: ['늘 진심으로 함께하는 동행 매니저 나알바입니다.', '작은 부분도 놓치지 않고 세심하게 챙기겠습니다.'],
};

const TIMELINE_BASE = [
  { label: '동행 시작 전', description: '동행 매니저와 매칭이 완료되었습니다.' },
  { label: '출발', description: '매니저가 출발 했습니다.' },
  { label: '병원 이동 중', description: '동행 매니저와 안전하게 병원으로 이동 중입니다.' },
  { label: '병원 도착', description: '병원에 도착하여 접수를 도와주고 있습니다.' },
  { label: '귀가 중', description: '진료를 마치고 안전하게 귀가를 돕고 있습니다.' },
  { label: '귀가 완료', description: '동행이 완료되었습니다.' },
];

/** 완료한 단계 수만큼 done 으로 만든 타임라인. times 는 단계별 완료 시각 */
function timeline(doneCount: number, times: string[]): TimelineStep[] {
  return TIMELINE_BASE.map((step, index) => ({
    ...step,
    done: index < doneCount,
    time: index < doneCount && index > 0 ? times[index] : undefined,
  }));
}

const COMMON = {
  postId: 5,
  title: '수술 전 검사 동행',
  hospitalName: '삼성서울병원',
  region: '서울 강남구',
  scheduleLabel: '2026년 9월 22일(화) 오전 9:00',
  durationLabel: '약 3시간',
  payLabel: '15,000원',
  startAt: '2026.09.22(화) 오전 9:00',
  endAt: '2026.09.22(화) 오후 12:00',
  transport: '택시(우버)',
  meetingPlace: '김가지님 댁 1층 (서울특별시 강남구 OO아파트 OOO동)',
  updatedAt: '9:00',
  manager: MANAGER,
};

const TIMES = ['', '8:40', '9:15', '9:15', '12:00', '12:30'];

export const CLIENT_ESCORT_CASES: ClientEscortCase[] = [
  { ...COMMON, applicationId: 1, stage: 'ready', timeline: timeline(1, TIMES) },
  { ...COMMON, applicationId: 2, stage: 'ongoing', timeline: timeline(3, TIMES) },
  { ...COMMON, applicationId: 3, stage: 'arrived', updatedAt: '10:00', timeline: timeline(4, TIMES) },
  {
    ...COMMON,
    applicationId: 4,
    stage: 'finishing',
    updatedAt: '10:00',
    confirmedEndAt: '2026.09.22(화) 오후 12:30',
    timeline: timeline(6, TIMES),
  },
  {
    ...COMMON,
    applicationId: 5,
    stage: 'done',
    scheduleLabel: '2026년 9월 22일(화) 오전 9:05 ~ 오후 12:10',
    startAt: '2026.09.22(화) 오전 9:05',
    endAt: '2026.09.22(화) 오후 12:10',
    timeline: timeline(6, ['', '8:40', '9:15', '9:15', '12:00', '12:10']),
    report: {
      department: '혈액 검사',
      purpose: '수술 전 검사, 정기검진',
      summary: [
        '수술 전 필요한 혈액 검사와 X-ray 촬영 후 의사 상담을 진행했습니다.',
        '검사 결과는 이상 없었으며, 수술 일정 및 주의사항에 대해 안내받았습니다.',
      ],
      aiSummary: [
        '수술 전 필요한 혈액 검사와 X-ray 촬영을 모두 진행했습니다.',
        '검사 결과는 이상 소견 없었으며, 수술 일정 및 주의사항에 대한 안내를 받았습니다.',
        '대기 시간이 다소 길었지만, 전반적으로 진료가 원활하게 진행되었습니다.',
        '의뢰인께서는 수술에 대한 충분한 설명을 듣고, 다음 일정을 잘 이해하셨습니다.',
      ],
      notes: [
        '대기 시간이 예상보다 길어 약 30분 정도 더 대기했습니다.',
        '의뢰인께서 다소 피로해하셔서 중간에 휴식을 취했습니다.',
      ],
      photoCount: 4,
    },
  },
];

export function getClientEscortCase(applicationId: number): ClientEscortCase | undefined {
  return CLIENT_ESCORT_CASES.find((item) => item.applicationId === applicationId);
}

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
 * ⚠️ 타임라인 단계별 "시각"과 지도의 실시간 위치는 API 가 없어 모의 값을 그대로 씁니다.
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
    updatedAt: TIMES[STAGE_DONE_COUNT[stage] - 1] || '9:00',
    manager,
    timeline: timeline(STAGE_DONE_COUNT[stage], TIMES),
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
