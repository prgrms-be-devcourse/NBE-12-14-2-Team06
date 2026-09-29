import type { Applicant } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 의뢰인_지원자 목록 381:3563 문구).
 *    지원자 목록은 백엔드 ApplicationController(공고별 지원 목록)와 연결할 자리입니다.
 *    별점·완료 동행 수·태그는 백엔드에 아직 없습니다.
 */
export const APPLICANTS: Applicant[] = [
  {
    id: 1, name: '김알바', rating: 4.8, completedCount: 12, region: '서울 송파구', tags: ['태그1', '태그2', '태그3'],
    intro: ['따뜻한 마음으로 정성껏 동행하겠습니다.', '처음 가시는 병원도 걱정 없도록, 친절하고 꼼꼼하게 도와드릴게요.'],
  },
  {
    id: 2, name: '박알바', rating: 4.9, completedCount: 21, region: '서울 은평구', tags: ['태그1', '태그2', '태그3'],
    intro: ['환자분의 입장에서 생각하는 동행이 되겠습니다.', '병원 동행 경험이 많아 절차에 대해 잘 알고 있어요.'],
  },
  {
    id: 3, name: '나알바', rating: 4.8, completedCount: 52, region: '서울 강남구', tags: ['태그1', '태그2', '태그3'],
    intro: ['늘 진심으로 함께하는 동행 매니저 나알바입니다.', '작은 부분도 놓치지 않고 세심하게 챙기겠습니다.'],
  },
  {
    id: 4, name: '이알바', rating: 5.0, completedCount: 3, region: '서울 마포구', tags: ['태그1', '태그2', '태그3'],
    intro: ['안정하고 편안한 동행을 약속드립니다.', '병원 이동부터 진료 후 귀가까지 세심하게 도와드립니다.'],
  },
];

export function getApplicants(postId: number): Applicant[] {
  // TODO: 공고별 지원 목록 API 로 교체. 지금은 어느 공고든 같은 목록을 보여줍니다.
  void postId;
  return APPLICANTS;
}
