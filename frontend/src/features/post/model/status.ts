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

/**
 * 의뢰인이 공고를 수정할 수 있는지 — 백엔드 PostService.modify 와 같은 조건입니다.
 * 모집 중(OPEN) 이면서 "아직 모집이 시작되지 않았을 때"만 됩니다.
 * ⚠️ 모집 중이어도 모집 시작 시각이 지나면 서버가 거부합니다(InvalidException 8).
 *    상태만 보고 판단하면 안 됩니다.
 */
export function canEditPost(postStatus: string, recruitStarted: boolean): boolean {
  return toPostStatusKey(postStatus) === 'open' && !recruitStarted;
}

/**
 * 의뢰인이 공고를 삭제할 수 있는지 — 백엔드 PostService.delete 와 같은 조건입니다.
 * 모집 중(OPEN) 이거나 마감 기한 초과(EXPIRED) 일 때만 됩니다. (수정과 달리 시간 제한은 없습니다)
 */
export function canDeletePost(postStatus: string): boolean {
  const key = toPostStatusKey(postStatus);
  return key === 'open' || key === 'expired';
}
