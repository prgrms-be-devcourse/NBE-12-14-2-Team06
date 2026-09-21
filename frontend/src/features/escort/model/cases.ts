import type { EscortCase, EscortStage, TimelineStep } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 188:1295 · 188:1296 · 188:1293 · 276:845 문구).
 *    진행 상태는 백엔드 EscortProgress / 신청 진행 API(PATCH /api/v1/applications/{id}/progress)와 연결할 자리입니다.
 *    타임라인 설명 문구는 디자인에서 흐리게 처리되어 읽히지 않아 임시로 적었습니다.
 */
const TIMELINE_BASE: Omit<TimelineStep, 'done' | 'time'>[] = [
  { label: '동행 시작 전', description: '의뢰인과 매칭이 완료되었습니다.' },
  { label: '출발', description: '출발지에서 출발했습니다.' },
  { label: '병원 이동 중', description: '병원으로 이동 중입니다.' },
  { label: '병원 도착', description: '병원에 도착했습니다.' },
  { label: '귀가 중', description: '귀가 이동 중입니다.' },
  { label: '귀가 완료', description: '귀가를 완료했습니다.' },
];

const TIMES = ['8:50', '9:00', '9:10', '9:40', '12:00', '12:10'];

/** 완료한 단계 수만큼 done 으로 만든 타임라인 */
function timeline(doneCount: number): TimelineStep[] {
  return TIMELINE_BASE.map((step, index) => ({
    ...step,
    done: index < doneCount,
    time: index < doneCount && index > 0 ? TIMES[index] : undefined,
  }));
}

const COMMON = {
  title: '수술 전 검사 동행',
  hospitalName: '삼성서울병원',
  dateLabel: '2026.09.22(화)',
  timeLabel: '오전 9:00',
  workTime: '9:00 ~ 12:10',
  clientName: '김가지',
  clientPhone: '010-0000-0000',
  hospitalAddress: '서울특별시 강남구',
  note: '보호자 동행X',
  transport: '택시(우버)',
};

export const ESCORT_CASES: EscortCase[] = [
  {
    ...COMMON,
    applicationId: 3,
    postId: 5,
    stage: 'ready',
    startAt: '2026.09.22(화) 오전 9:00',
    endAt: '2026.09.22(화) 오후 12:00',
    timeline: timeline(1),
  },
  {
    ...COMMON,
    applicationId: 4,
    postId: 5,
    stage: 'ongoing',
    startAt: '2026.09.22(화) 오전 9:00',
    endAt: '2026.09.22(화) 오후 12:00',
    timeline: timeline(3),
  },
  {
    ...COMMON,
    applicationId: 5,
    postId: 8,
    stage: 'done',
    startAt: '2026.09.22(화) 오전 9:05',
    endAt: '2026.09.22(화) 오후 12:10',
    timeline: timeline(6),
    report: {
      department: '혈액 검사',
      purpose: '수술 전 검사, 정기검진',
      summary: [
        '수술 전 필요한 혈액 검사와 X-ray 촬영 후 의사 상담을 진행했습니다.',
        '검사 결과는 이상 없었으며, 수술 일정 및 주의사항에 대해 안내받았습니다.',
      ],
      notes: [
        '대기 시간이 예상보다 길어 약 30분 정도 더 대기했습니다.',
        '의뢰인께서 다소 피로해하셔서 중간에 휴식을 취했습니다.',
      ],
      photoCount: 4,
      writer: '나알바(동행 매니저)',
      submittedAt: '2026.09.22(화) 오후 12:30',
    },
  },
];

export function getEscortCase(applicationId: number): EscortCase | undefined {
  return ESCORT_CASES.find((item) => item.applicationId === applicationId);
}

/** 단계별 화면 문구·버튼 (Figma "진행 요약" 카드) */
export const STAGE_INFO: Record<
  EscortStage,
  { badge: string; badgeTone: 'blue' | 'strong'; guide: string[]; primary: string; third: string }
> = {
  ready: {
    badge: '진행 중',
    badgeTone: 'blue',
    guide: ['동행 시작 전입니다.', '일정과 의뢰 정보를 확인해주세요.'],
    primary: '동행 시작',
    third: '공고 상세보기',
  },
  ongoing: {
    badge: '진행 중',
    badgeTone: 'blue',
    guide: ['현재 동행이 진행 중입니다.', '안전하게 동행을 진행해주세요.'],
    primary: '병원 도착',
    third: '공고 상세보기',
  },
  done: {
    badge: '완료',
    badgeTone: 'strong',
    guide: ['동행이 정상적으로 완료되었습니다.', '진료 내용을 정리하여 보고서를 작성해주세요.'],
    primary: '보고서 작성',
    third: '정산 요청하기',
  },
};
