import type { MemberDetail } from '../types';
import { MEMBERS } from './members';

/*
 * ⚠️ 모의 데이터입니다. 문구·숫자는 Figma 화면(571:20504)을 그대로 옮겼습니다.
 *    목록에 있는 값(아이디·이름·역할·이메일·전화번호·가입일)은 회원마다 다르지만,
 *    아래 항목은 아직 받아올 곳이 없어 모든 회원에게 같은 값을 보여줍니다.
 *    TODO: 회원 상세 API(GET /api/v1/admin/users/{userId}) 가 연결되면 이 파일은 필요 없어집니다.
 */
const SAMPLE_DETAIL: Omit<MemberDetail, keyof (typeof MEMBERS)[number]> = {
  status: '활동중',
  birthDate: '1950.01.01',
  gender: '선택 안 함',
  address: '서울특별시 강남구',
  guardianName: '김보호',
  guardianPhone: '010-1111-1111',
  careNote: '거동이 불편하여 휠체어 이용이 필요합니다.',
  recent: [
    { label: '최근 로그인', date: '2026.10.01' },
    { label: '최근 리뷰', detail: '친절해요!', date: '2026.02.09' },
    { label: '최근 매칭', detail: '박알바 회원과 매칭 완료', date: '2026.10.01' },
  ],
  activity: [
    { label: '등록한 공고', count: 3, icon: '/icons/activity-post.svg' },
    { label: '매칭된 공고', count: 5, icon: '/icons/activity-star.svg' },
    { label: '진행 중', count: 1, icon: '/icons/activity-bell.svg' },
    { label: '작성한 리뷰', count: 5, icon: '/icons/activity-chat.svg' },
  ],
};

/** 회원 ID("user001")로 상세 정보를 찾습니다. 없으면 undefined 입니다. */
export function getMemberDetail(id: string): MemberDetail | undefined {
  const member = MEMBERS.find((item) => item.id === id);
  return member && { ...member, ...SAMPLE_DETAIL };
}
