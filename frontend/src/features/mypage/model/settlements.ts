import type { Review, Settlement } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 522:3149 · 562:14068 문구).
 *    정산·리뷰 목록을 주는 API 는 백엔드에 별도 확인이 필요합니다 (domain/settlement, domain/review 존재).
 */
export const SETTLEMENTS: Settlement[] = [
  {
    id: 1, applicationId: 2, postId: 10, status: 'waiting', title: '정형외과 외래 진료 동행 지원', hospitalName: '서울대학교 병원', location: '서울 종로구',
    dateLabel: '2026.10.28 (수)', dueLabel: '2026.10.29 (목)', dueDate: '2026-10-29', durationLabel: '약 4시간', amount: 60000,
  },
  {
    id: 2, applicationId: 3, postId: 3, status: 'done', title: '내과 정기검진 동행', hospitalName: '강남세브란스병원', location: '서울 서대문구',
    dateLabel: '2026.10.20 (화)', dueLabel: '2026.10.21 (수)', dueDate: '2026-10-21', durationLabel: '약 2시간', amount: 34000,
  },
  {
    id: 3, applicationId: 4, postId: 5, status: 'done', title: '수술 전 검사 동행', hospitalName: '삼성서울병원', location: '서울 강남구',
    dateLabel: '2026.09.22 (화)', dueLabel: '2026.09.23 (수)', dueDate: '2026-09-23', durationLabel: '약 3시간', amount: 45000,
  },
  {
    id: 4, applicationId: 5, postId: 8, status: 'done', title: '안과 치료 동행', hospitalName: '가톨릭대학교 서울성모병원', location: '서울 서초구',
    dateLabel: '2026.02.09 (월)', dueLabel: '2026.02.10 (화)', dueDate: '2026-02-10', durationLabel: '약 2시간', amount: 100000,
  },
];

/** 위쪽 요약 (Figma 문구 그대로. 정산 금액은 목록 합계와 달리 전체 기간 합계입니다) */
export const SETTLEMENT_SUMMARY = { total: 643000, waiting: 1, done: 3 };

export const REVIEWS: Review[] = [
  {
    id: 1, title: '내과 정기검진 동행', hospitalName: '강남세브란스병원', location: '서울 서대문구', date: '2026-10-21', rating: 4,
    positives: ['친절해요', '시간을 잘 지켜요', '보고서가 꼼꼼해요'],
    negatives: ['소통이 잘 안됐어요', '응대가 아쉬웠어요'],
    comment: '매니저분께서 미리 와계셔서 빠르게 진료 볼 수 있었습니다. 그런데 매니저를 자주 안해보셨는지 미숙한 부분이 조금 있었습니다!',
  },
  {
    id: 2, title: '수술 전 검사 동행', hospitalName: '삼성서울병원', location: '서울 강남구', date: '2026-09-23', rating: 3,
    positives: ['친절해요', '시간을 잘 지켜요'],
    negatives: ['소통이 잘 안됐어요', '응대가 아쉬웠어요', '보고서 내용이 부족해요'],
    comment: '친절하시지만 매니저로서의 역할은 제대로 못하신 것 같아 아쉽습니다!',
  },
  {
    id: 3, title: '안과 치료 동행', hospitalName: '가톨릭대학교 서울성모병원', location: '서울 서초구', date: '2026-02-10', rating: 5,
    positives: ['친절해요', '시간을 잘 지켜요', '보고서가 꼼꼼해요', '소통이 잘 돼요', '어르신을 세심하게 챙겨요'],
    negatives: [],
    comment: '다음번에도 동행 매니저 지원 부탁드리겠습니다!',
  },
];
