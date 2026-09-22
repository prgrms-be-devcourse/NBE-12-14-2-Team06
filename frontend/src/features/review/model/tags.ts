/** 세부 평가 태그 한 개. value 는 백엔드 ReviewTag ENUM 이름과 정확히 일치해야 합니다. */
export type ReviewTagOption = { value: string; label: string };

/** 화면의 3줄 배치. label 은 백엔드 ReviewTag.description 과 그대로 대응됩니다. */
export const REVIEW_TAG_ROWS: ReviewTagOption[][] = [
  [
    { value: 'KIND', label: '친절해요' },
    { value: 'PUNCTUAL', label: '시간을 잘 지켜요' },
    { value: 'DETAILED_REPORT', label: '보고서가 꼼꼼해요' },
    { value: 'GOOD_COMMUNICATION', label: '소통이 잘 돼요' },
  ],
  [
    { value: 'CAREFUL', label: '어르신을 세심하게 챙겨요' },
    { value: 'LATE', label: '시간 약속이 아쉬워요' },
    { value: 'POOR_COMMUNICATION', label: '소통이 잘 안 됐어요' },
  ],
  [
    { value: 'UNKIND', label: '응대가 아쉬웠어요' },
    { value: 'INSUFFICIENT_REPORT', label: '보고서 내용이 부족해요' },
  ],
];

/** 한 번에 선택할 수 있는 태그 수 (백엔드 ReviewWriteRequest.tags 검증과 일치) */
export const MAX_TAGS = 5;
