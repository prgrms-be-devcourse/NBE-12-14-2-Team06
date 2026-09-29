/** 백엔드 ReviewTag ENUM 이름 (entity/ReviewTag.java 와 1:1) */
export type ReviewTagName =
  | 'KIND'
  | 'PUNCTUAL'
  | 'DETAILED_REPORT'
  | 'GOOD_COMMUNICATION'
  | 'CAREFUL'
  | 'LATE'
  | 'POOR_COMMUNICATION'
  | 'UNKIND'
  | 'INSUFFICIENT_REPORT';

/**
 * 세부 평가 태그 한 개.
 * value·label·positive 는 백엔드 ReviewTag 의 이름·description·positive 와 정확히 일치해야 합니다.
 */
export type ReviewTagOption = { value: ReviewTagName; label: string; positive: boolean };

/** 화면의 3줄 배치. 줄 나눔은 Figma 기준이라 긍정/부정 순서와는 다릅니다. */
export const REVIEW_TAG_ROWS: ReviewTagOption[][] = [
  [
    { value: 'KIND', label: '친절해요', positive: true },
    { value: 'PUNCTUAL', label: '시간을 잘 지켜요', positive: true },
    { value: 'DETAILED_REPORT', label: '보고서가 꼼꼼해요', positive: true },
    { value: 'GOOD_COMMUNICATION', label: '소통이 잘 돼요', positive: true },
  ],
  [
    { value: 'CAREFUL', label: '어르신을 세심하게 챙겨요', positive: true },
    { value: 'LATE', label: '시간 약속이 아쉬워요', positive: false },
    { value: 'POOR_COMMUNICATION', label: '소통이 잘 안 됐어요', positive: false },
  ],
  [
    { value: 'UNKIND', label: '응대가 아쉬웠어요', positive: false },
    { value: 'INSUFFICIENT_REPORT', label: '보고서 내용이 부족해요', positive: false },
  ],
];

const TAG_INFO = new Map<string, ReviewTagOption>(REVIEW_TAG_ROWS.flat().map((tag) => [tag.value, tag]));

/**
 * ENUM 이름 → 화면 문구·긍정 여부. 태그 칩의 색은 positive 로 가릅니다(긍정 파랑 · 부정 회색).
 * 백엔드에 태그가 새로 생겨 위 표에 없으면 이름을 그대로 보여주고 부정(회색)으로 둡니다.
 */
export function reviewTagInfo(value: string): { label: string; positive: boolean } {
  return TAG_INFO.get(value) ?? { label: value, positive: false };
}

/** 한 번에 선택할 수 있는 태그 수 (백엔드 ReviewWriteRequest.tags 검증과 일치) */
export const MAX_TAGS = 5;
