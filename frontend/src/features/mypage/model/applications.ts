import type { LabelTone } from '@/features/post';
import type { ApplicationStatus } from '../types';

export type StatusTab = 'all' | ApplicationStatus;

/** 상태별 카드 라벨 색 · 탭 이름 */
export const STATUS_INFO: Record<ApplicationStatus, { label: string; tone: LabelTone }> = {
  pending: { label: '대기 중', tone: 'gray' },
  matched: { label: '매칭 완료', tone: 'green' },
  inProgress: { label: '진행 중', tone: 'blue' },
  completed: { label: '완료', tone: 'strong' },
  rejected: { label: '거절', tone: 'red' },
  canceled: { label: '지원 취소', tone: 'gray' },
};

/** 탭 순서 ("거절", "지원 취소"는 전체에서만 보입니다) */
export const STATUS_TABS: { value: StatusTab; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'pending', label: '대기 중' },
  { value: 'matched', label: '매칭 완료' },
  { value: 'inProgress', label: '진행 중' },
  { value: 'completed', label: '완료' },
];
