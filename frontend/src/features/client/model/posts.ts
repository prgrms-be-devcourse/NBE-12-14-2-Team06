import type { PostSummary } from '@/features/post';
import { daysFromNow, formatMonthDay } from '../lib/date';
import type { ClientPost, ClientPostStatus } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 의뢰인_내가 작성한 공고 562:14622 문구).
 *    작성한 공고 목록 API 는 아직 없습니다. (GET /api/v1/posts 는 결제 완료된 전체 공고만 돌려줍니다.)
 *    "상세보기"는 공고 상세 모의 데이터(post/model/posts.ts)의 같은 번호 공고를 보여줍니다.
 */
export const CLIENT_POSTS: ClientPost[] = [
  {
    id: 1, title: '정형외과 진료 동행', hospitalName: '서울성모병원', location: '서울 서초구',
    dateLabel: '9월 22일(화)', timeLabel: '오전 9:00', durationLabel: '약 3시간', payLabel: '시급 14,000원',
    description: ['무릎 통증으로 정형외과 진료를 받을 예정입니다.', '접수부터 진료, 검사, 귀가까지 함께 해주실 분을 찾습니다.'],
    status: 'open',
  },
  {
    id: 2, title: '내과 정기 검진 동행', hospitalName: '삼성서울병원', location: '서울 강남구',
    dateLabel: '9월 25일(금)', timeLabel: '오후 2:00', durationLabel: '약 4시간', payLabel: '시급 15,000원',
    description: ['부모님 정기 검진으로 내과 진료와 혈액 검사가 예정되어 있습니다.', '차분하고 꼼꼼한 분이면 좋겠습니다.'],
    status: 'open',
  },
  {
    id: 3, title: '안과 진료 동행', hospitalName: '서울성모병원', location: '서울 송파구',
    dateLabel: '9월 22일(화)', timeLabel: '오전 9:00', durationLabel: '약 3시간', payLabel: '시급 14,000원',
    description: ['무릎 통증으로 정형외과 진료를 받을 예정입니다.', '접수부터 진료, 검사, 귀가까지 함께 해주실 분을 찾습니다.'],
    status: 'matched', managerName: '양정원 매니저', applicationId: 1,
  },
  {
    id: 4, title: '안과 진료 동행', hospitalName: '서울성모병원', location: '서울 송파구',
    dateLabel: '9월 22일(화)', timeLabel: '오전 9:00', durationLabel: '약 3시간', payLabel: '시급 16,000원',
    description: ['부모님 두 분의 종합 건강검진으로 병원 동행을 의뢰했습니다.', '접수부터 검사사, 식사 안내까지 도와주셔서 감사합니다.'],
    status: 'inProgress', managerName: '김채원 매니저', applicationId: 2,
  },
  {
    id: 5, title: '수술 전 검사 동행', hospitalName: '삼성서울병원', location: '서울 강남구',
    dateLabel: '9월 22일(화)', timeLabel: '오전 9:05', durationLabel: '약 3시간', payLabel: '시급 15,000원',
    description: ['수술 전 필요한 혈액 검사와 X-ray 촬영이 예정되어 있습니다.', '검사 결과 안내까지 함께해 주실 분을 찾습니다.'],
    status: 'completed', managerName: '나알바 매니저', applicationId: 5,
  },
  {
    id: 6, title: '치과 정기 진료 동행', hospitalName: '연세대학교 치과병원', location: '서울 서대문구',
    dateLabel: '10월 2일(금)', timeLabel: '오후 1:00', durationLabel: '약 2시간', payLabel: '시급 14,000원',
    description: ['치과 정기 진료 시 접수와 진료실 이동을 도와주실 분을 찾습니다.'],
    status: 'open',
  },
];

/** 상태 탭 (Figma: 전체 · 대기 중 · 매칭 완료 · 진행 중 · 완료) */
export const STATUS_TABS: { value: 'all' | ClientPostStatus; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'open', label: '대기 중' },
  { value: 'matched', label: '매칭 완료' },
  { value: 'inProgress', label: '진행 중' },
  { value: 'completed', label: '완료' },
];

/** 카드 라벨 (Figma 라벨: 모집 중=보라, 매칭 완료=초록, 진행 중=파랑). 취소됨·마감 기한 초과는 탭은 없지만 라벨은 보여줘야 해서 회색으로 추가했습니다. */
export const STATUS_LABEL: Record<ClientPostStatus, { text: string; tone: 'purple' | 'green' | 'blue' | 'strong' | 'gray' }> = {
  open: { text: '모집 중', tone: 'purple' },
  matched: { text: '매칭 완료', tone: 'green' },
  inProgress: { text: '진행 중', tone: 'blue' },
  completed: { text: '완료', tone: 'strong' },
  canceled: { text: '취소됨', tone: 'gray' },
  expired: { text: '마감 기한 초과', tone: 'gray' },
};

/** 백엔드 PostDto.postStatus(한글 문구) → 화면 상태값 */
export function toClientPostStatus(postStatus: string): ClientPostStatus {
  switch (postStatus) {
    case '모집 중': return 'open';
    case '매칭 완료': return 'matched';
    case '동행 진행 중': return 'inProgress';
    case '동행 완료': return 'completed';
    case '취소됨': return 'canceled';
    case '마감 기한 초과': return 'expired';
    default: return 'open';
  }
}

/**
 * 백엔드 PostSummary(내 공고 목록에서 걸러낸 것) → 카드용 ClientPost.
 * accepted 는 매칭 완료 이후 상태에서만 넘어옵니다(모집 중인 공고는 지원자 조회를 하지 않으므로 undefined).
 */
export function toClientPost(summary: PostSummary, accepted?: { applicationId: number; escortName: string }): ClientPost {
  return {
    id: summary.id,
    title: summary.title,
    hospitalName: summary.hospitalName,
    location: `${summary.region} ${summary.district}`,
    dateLabel: formatMonthDay(daysFromNow(summary.startsInDays)),
    timeLabel: summary.startTime,
    durationLabel: `약 ${summary.hours}시간`,
    payLabel: `시급 ${summary.hourlyPay.toLocaleString()}원`,
    description: summary.description,
    status: toClientPostStatus(summary.postStatus),
    managerName: accepted?.escortName,
    applicationId: accepted?.applicationId,
  };
}

export const SORT_OPTIONS = [
  { value: 'latest', label: '최신 순' },
  { value: 'oldest', label: '오래된 순' },
] as const;

export type ClientPostSort = (typeof SORT_OPTIONS)[number]['value'];

/** 탭 · 검색어 · 정렬을 적용한 목록 */
export function filterClientPosts(
  posts: ClientPost[],
  status: 'all' | ClientPostStatus,
  keyword: string,
  sort: ClientPostSort,
): ClientPost[] {
  const word = keyword.trim();
  const result = posts.filter(
    (post) =>
      (status === 'all' || post.status === status) &&
      (!word || [post.title, post.hospitalName, post.location].some((text) => text.includes(word))),
  );
  // 모의 데이터는 최신 순으로 적혀 있습니다. (API 는 정렬해서 내려주면 이 부분이 필요 없어집니다.)
  return sort === 'latest' ? result : result.reverse();
}

export function getClientPost(id: number): ClientPost | undefined {
  return CLIENT_POSTS.find((post) => post.id === id);
}
