import type { LabelTone, PostStatusKey } from '../types';

/**
 * 백엔드 PostDto.postStatus(한글 문구) → 화면 상태값.
 * 모르는 문구가 오면 'open' 으로 봅니다. (백엔드에 상태가 추가되면 여기에 같이 넣어야 합니다)
 */
export function toPostStatusKey(postStatus: string): PostStatusKey {
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
 * 상태 라벨 (Figma 라벨: 모집 중=보라, 매칭 완료=초록, 진행 중=파랑).
 * 취소됨·마감 기한 초과는 상태 탭은 없지만 라벨은 보여줘야 해서 회색으로 넣었습니다.
 */
export const POST_STATUS_LABEL: Record<PostStatusKey, { text: string; tone: LabelTone }> = {
  open: { text: '모집 중', tone: 'purple' },
  matched: { text: '매칭 완료', tone: 'green' },
  inProgress: { text: '진행 중', tone: 'blue' },
  completed: { text: '동행 완료', tone: 'strong' },
  canceled: { text: '취소됨', tone: 'gray' },
  expired: { text: '마감 기한 초과', tone: 'gray' },
};

/**
 * postStatus 원문을 바로 라벨로. 내 공고 목록 카드 · 공고 상세 · 지원자 확인이 모두 이 표 하나를 씁니다.
 * (이미 PostStatusKey 로 바꿔 둔 값이 있으면 POST_STATUS_LABEL 을 바로 쓰세요.)
 */
export function postStatusLabel(postStatus: string): { text: string; tone: LabelTone } {
  return POST_STATUS_LABEL[toPostStatusKey(postStatus)];
}
