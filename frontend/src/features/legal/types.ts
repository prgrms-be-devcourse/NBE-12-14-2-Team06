/** 개인정보 처리방침 본문을 이루는 조각 */
export type PolicyBlock =
  | { type: 'p'; text: string }
  | { type: 'ul'; items: string[] }
  /** 표. head 는 머리글, rows 는 줄마다 칸 배열 */
  | { type: 'table'; head: string[]; rows: string[][] }
  /** 강조 상자 (참고·유의 사항) */
  | { type: 'note'; text: string };

/** 처리방침의 한 항목 (1. 개인정보의 처리 목적 …) */
export type PolicyArticle = {
  id: string;
  title: string;
  blocks: PolicyBlock[];
};

/** 개정 이력 한 줄 */
export type PolicyVersion = {
  /** "1차" */
  label: string;
  /** 시행일 "2026.09.21" */
  date: string;
  summary: string;
};
