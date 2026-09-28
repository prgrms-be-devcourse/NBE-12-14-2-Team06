import type { EscortStage } from '../types';

/**
 * 동행 진행 6단계의 라벨·설명 (백엔드 EscortProgress 순서와 1:1로 맞춥니다).
 * ⚠️ 설명 문구는 Figma 에서 흐리게 처리되어 읽히지 않아 임시로 적었습니다.
 */
export const TIMELINE_BASE: { label: string; description: string }[] = [
  { label: '동행 시작 전', description: '의뢰인과 매칭이 완료되었습니다.' },
  { label: '출발', description: '출발지에서 출발했습니다.' },
  { label: '병원 이동 중', description: '병원으로 이동 중입니다.' },
  { label: '병원 도착', description: '병원에 도착했습니다.' },
  { label: '귀가 중', description: '귀가 이동 중입니다.' },
  { label: '귀가 완료', description: '귀가를 완료했습니다.' },
];

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
