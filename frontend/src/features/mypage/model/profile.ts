import type { ActivityStat, MyProfile } from '../types';

/*
 * ⚠️ 의뢰인 마이페이지(role='client', "/client")용 모의 데이터입니다. 이번 작업 범위(/mypage)가 아니라 그대로 둡니다.
 *    동행 매니저 쪽(role='escort', "/mypage")은 실제 API로 연결했습니다 (MyInfoPage.tsx 참고).
 *
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
