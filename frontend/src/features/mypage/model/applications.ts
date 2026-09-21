import type { LabelTone } from '@/features/post';
import type { Application, ApplicationStatus } from '../types';

/*
 * ⚠️ 모의 데이터입니다 (Figma 525:5182 문구).
 *    "내가 신청한 공고" 목록을 주는 백엔드 API 는 아직 없습니다 (지원·취소·수락·거절·진행 API 만 있음).
 */
export const APPLICATIONS: Application[] = [
  {
    id: 1, postId: 11, title: '소아과 진료 동행', hospitalName: '이화여자대학교 목동병원', location: '서울 양천구',
    dateLabel: '2026.11.16.(금)', timeLabel: '오후 15:00', durationLabel: '약 2시간', payLabel: '시급 14,000원', status: 'pending',
    description: ['아이와 함께하는 소아과 진료에 동행할 분을 찾습니다.', '병원 이동과 접수를 도와주세요.'],
  },
  {
    id: 2, postId: 10, title: '정형외과 외래 진료 동행 지원', hospitalName: '서울대학교병원', location: '서울 종로구',
    dateLabel: '2026.10.28.(수)', timeLabel: '오전 9:00', durationLabel: '약 4시간', payLabel: '시급 15,000원', status: 'pending',
    description: ['거동이 불편하신 어르신의 외래 진료를 함께해 주세요.', '병원 내 이동과 접수를 도와드립니다.'],
  },
  {
    id: 3, postId: 3, title: '내과 정기검진 동행', hospitalName: '강남세브란스병원', location: '서울 서대문구',
    dateLabel: '2026.10.20.(화)', timeLabel: '오후 14:00', durationLabel: '약 2시간', payLabel: '시급 17,000원', status: 'matched',
    description: ['정기검진을 위해 병원 방문 시 동행이 필요합니다.', '병원 이동과 검사실 이동, 대기 동행을 부탁드립니다.'],
  },
  {
    id: 4, postId: 5, title: '수술 전 검사 동행', hospitalName: '삼성서울병원', location: '서울 강남구',
    dateLabel: '2026.09.22.(화)', timeLabel: '오전 09:00', durationLabel: '약 3시간', payLabel: '시급 15,000원', status: 'inProgress',
    description: ['수술 전 검사 일정에 동행하여 이동과 접수를 도와주세요.', '보호자분이 부득이하게 참석이 어려운 상황입니다.'],
  },
  {
    id: 5, postId: 8, title: '안과 치료 동행', hospitalName: '가톨릭대학교 서울성모병원', location: '서울 서초구',
    dateLabel: '2026.02.09.(월)', timeLabel: '오후 13:00', durationLabel: '약 2시간', payLabel: '50,000원', status: 'completed',
    description: ['안과 치료를 위한 병원 방문에 동행하였습니다.', '접수부터 진료까지 완료했습니다.'],
  },
  {
    id: 6, postId: 4, title: '피부과 진료 동행', hospitalName: '고려대학교 안암병원', location: '서울 성북구',
    dateLabel: '2025.12.24.(수)', timeLabel: '오전 11:00', durationLabel: '약 3시간', payLabel: '시급 15,000원', status: 'rejected',
    description: ['피부과 진료를 위한 이동을 도와줄 동행 매니저를 찾습니다.'],
  },
];

export type StatusTab = 'all' | ApplicationStatus;

/** 상태별 카드 라벨 색 · 탭 이름 */
export const STATUS_INFO: Record<ApplicationStatus, { label: string; tone: LabelTone }> = {
  pending: { label: '대기 중', tone: 'gray' },
  matched: { label: '매칭 완료', tone: 'green' },
  inProgress: { label: '진행 중', tone: 'blue' },
  completed: { label: '완료', tone: 'strong' },
  rejected: { label: '거절', tone: 'red' },
};

/** 탭 순서 ("거절"은 전체에서만 보입니다) */
export const STATUS_TABS: { value: StatusTab; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'pending', label: '대기 중' },
  { value: 'matched', label: '매칭 완료' },
  { value: 'inProgress', label: '진행 중' },
  { value: 'completed', label: '완료' },
];
