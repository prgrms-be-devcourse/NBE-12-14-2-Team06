import type { PostDetail, PostSummary } from '../types';

/*
 * ⚠️ 모의 데이터입니다. 백엔드 목록 API(GET /api/v1/posts)와 연결하면 이 파일은 필요 없어집니다.
 *    앞의 6개는 Figma 화면(188:1289)의 문구, 뒤의 6개는 2페이지 확인용으로 만든 예시입니다.
 */
export const POSTS: PostSummary[] = [
  {
    id: 1, title: '정기 검진 동행 지원자 모집', hospitalName: '서울대학교병원', region: '서울특별시', district: '종로구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '2시간 전', startsInDays: 2, startTime: '오전 9:00', hours: 3, hourlyPay: 15000, badge: 'new',
    description: ['부모님 정기 검진으로 병원에 함께 가주실 동행 매니저분을 찾습니다.', '접수부터 진료, 수납까지 함께 이동합니다.'],
  },
  {
    id: 2, title: '허리 MRI 검사 동행', hospitalName: '강남세브란스병원', region: '서울특별시', district: '강남구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '5시간 전', startsInDays: 0, startTime: '오후 1:30', hours: 2, hourlyPay: 16000, badge: 'closing',
    description: ['허리 MRI 검사로 병원 내 이동이 필요합니다.', '검사 전후 접수와 수납을 함께 도와주세요.'],
  },
  {
    id: 3, title: '외래 진료 동행(내과)', hospitalName: '삼성서울병원', region: '서울특별시', district: '강남구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '1일 전', startsInDays: 3, startTime: '오전 10:00', hours: 2, hourlyPay: 15000, badge: 'open',
    description: ['내과 외래 진료를 위한 동행입니다.', '병원 내 이동과 진료 대기를 도와주세요.'],
  },
  {
    id: 4, title: '치과 진료 동행', hospitalName: '연세대학교 치과 병원', region: '서울특별시', district: '서대문구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '2일 전', startsInDays: 6, startTime: '오후 2:00', hours: 1, hourlyPay: 14000, badge: 'open',
    description: ['치과 진료 시 접수와 진료실 이동을 함께 도와주실 분을 찾습니다.'],
  },
  {
    id: 5, title: '수술 전 검사 동행', hospitalName: '고려대학교 안암병원', region: '서울특별시', district: '성북구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '2일 전', startsInDays: 5, startTime: '오전 8:30', hours: 4, hourlyPay: 17000, badge: 'open',
    description: ['수술 전 필요한 여러 검사를 진행합니다.', '병원 내 이동과 접수를 함께 도와주세요.'],
  },
  {
    id: 6, title: '건강검진 동행', hospitalName: '서울아산병원', region: '서울특별시', district: '송파구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '1일 전', startsInDays: 7, startTime: '오전 7:30', hours: 5, hourlyPay: 16000, badge: 'open',
    description: ['종합 건강검진을 함께 진행합니다.', '검진 전후 안내 및 병원 내 이동을 도와주세요.'],
  },
  {
    id: 7, title: '안과 정기 진료 동행', hospitalName: '분당서울대학교병원', region: '경기도', district: '성남시',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '3일 전', startsInDays: 9, startTime: '오전 11:00', hours: 2, hourlyPay: 15000, badge: 'open',
    description: ['안과 정기 진료에 함께 가주실 분을 찾습니다.', '진료 후 처방전 수령까지 도와주세요.'],
  },
  {
    id: 8, title: '물리치료 동행', hospitalName: '아주대학교병원', region: '경기도', district: '수원시',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '3일 전', startsInDays: 12, startTime: '오후 3:00', hours: 3, hourlyPay: 16000, badge: 'open',
    description: ['물리치료 센터까지 이동을 도와주실 분을 찾습니다.', '치료 중 대기 시간에 함께해 주세요.'],
  },
  {
    id: 9, title: '심장 초음파 검사 동행', hospitalName: '부산대학교병원', region: '부산광역시', district: '서구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '4일 전', startsInDays: 10, startTime: '오전 9:30', hours: 3, hourlyPay: 17000, badge: 'open',
    description: ['심장 초음파 검사가 있습니다.', '접수와 검사실 이동을 함께 도와주세요.'],
  },
  {
    id: 10, title: '정형외과 외래 진료 지원', hospitalName: '서울대학교병원', region: '서울특별시', district: '종로구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '4일 전', startsInDays: 14, startTime: '오전 9:00', hours: 4, hourlyPay: 15000, badge: 'open',
    description: ['거동이 불편하신 어르신의 외래 진료를 함께해 주세요.', '병원 내 이동과 접수를 도와드립니다.'],
  },
  {
    id: 11, title: '소아과 진료 동행', hospitalName: '이화여자대학교 목동병원', region: '서울특별시', district: '양천구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '5일 전', startsInDays: 20, startTime: '오후 3:00', hours: 2, hourlyPay: 14000, badge: 'open',
    description: ['아이와 함께하는 소아과 진료에 동행할 분을 찾습니다.', '병원 이동과 접수를 도와주세요.'],
  },
  {
    id: 12, title: '재활 치료 동행', hospitalName: '경희대학교병원', region: '서울특별시', district: '동대문구',
    clientId: 'client01', postStatus: '모집 중',
    postedAgo: '6일 전', startsInDays: 25, startTime: '오전 10:30', hours: 3, hourlyPay: 16000, badge: 'open',
    description: ['재활 치료 일정에 맞춰 이동을 도와주실 분을 찾습니다.'],
  },
];

/** 상세 화면 확인용. 1번 공고는 Figma(225:1146) 문구이고, 나머지는 요약에서 만든 기본값입니다. */
export function getPostDetail(id: number): PostDetail | undefined {
  const summary = POSTS.find((post) => post.id === id);
  if (!summary) return undefined;

  return {
    ...summary,
    postedAt: '2026.09.12   18:12',
    hospitalAddress: `${summary.region} ${summary.district}`,
    pickupAddress: `${summary.region} ${summary.district}`,
    details: [
      '허리 통증으로 강남세브란스병원에서 MRI 검사를 받을 예정입니다.',
      '접수부터 검사 후 결과 안내까지 전반적인 일정에 함께 동행해주실 분을 찾습니다.',
      '병원 내 이동이 많지 않고, 대기 시간이 있을 수 있습니다.',
      '편안하고 책임감 있게 도와주실 분의 지원을 기다립니다.',
    ],
    patientNote: ['휠체어 이용 없음', '기타 특이사항 없음'],
    reportRequired: true,
    recruitPeriod: '10월 2일(금) 오전 9:00 ~ 10월 5일(월) 오후 6:00',
    // 모의 데이터는 모두 "모집 중"이라 모집 시작 전으로 둡니다. (의뢰인 화면에서 수정·삭제가 다 보입니다)
    recruitStarted: false,
  };
}
