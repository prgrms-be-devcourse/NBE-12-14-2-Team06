import type { ActivityStat, MyProfile } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 210:1079 문구).
 *    내 정보 API(GET /api/v1/users/profile)는 username·email·name·role·gender·birthDate·phoneNum·region 을 줍니다.
 *    자기소개·최근 활동 요약은 백엔드에 아직 없습니다.
 */
export const MY_PROFILE: MyProfile = {
  name: '나알바',
  roleLabel: '동행 매니저',
  username: 'GAJI_TEAM006',
  email: 'GAJI_TEAM006@gmail.com',
  phone: '010-6666-6666',
  address: '서울특별시 강남구',
  birthDate: '2000.01.01',
  gender: '남',
  intro: ['늘 진심으로 함께하는 동행 매니저 최하늘입니다.', '작은 부분도 놓치지 않고 세심하게 챙기겠습니다.'],
};

export const ACTIVITY_STATS: ActivityStat[] = [
  { label: '등록한 공고', count: 5, icon: '/icons/activity-post.svg' },
  { label: '매칭된 공고', count: 2, icon: '/icons/activity-star.svg' },
  { label: '진행 중', count: 1, icon: '/icons/activity-bell.svg' },
  { label: '받은 리뷰', count: 7, icon: '/icons/activity-chat.svg' },
];

/*
 * 의뢰인 마이페이지 (Figma 의뢰인_마이페이지 210:746)
 * ClientProfile 의 보호자 정보(emergencyContactName·emergencyContactPhone)와 특이사항(careNote)입니다.
 */
export const CLIENT_PROFILE: MyProfile = {
  name: '김가지',
  roleLabel: '의뢰인',
  username: 'GAJI_TEAM06',
  email: 'GAJI_TEAM06@gmail.com',
  phone: '010-0000-0000',
  address: '서울특별시 강남구',
  birthDate: '1950.01.01',
  gender: '선택 안 함',
  guardian: { name: '김보호', phone: '010-1111-1111', careNote: '거동이 불편하여 휠체어 이용이 필요합니다.' },
};

export const CLIENT_ACTIVITY_STATS: ActivityStat[] = [
  { label: '등록한 공고', count: 3, icon: '/icons/activity-post.svg' },
  { label: '매칭된 공고', count: 5, icon: '/icons/activity-star.svg' },
  { label: '진행 중', count: 1, icon: '/icons/activity-bell.svg' },
  { label: '작성한 리뷰', count: 5, icon: '/icons/activity-chat.svg' },
];
