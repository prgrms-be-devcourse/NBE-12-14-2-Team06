/** 화면3 — 가지가 만드는 특별한 변화 */
export type ChangeItem = {
  image: string;
  imageAlt: string;
  title: string;
  description: [string, string];
};

/** 화면4 — 이런 분들께 추천해요 (탭) */
export type Persona = {
  id: string;
  tabTitle: string;
  tabDesc: string[];
  headline: string;
  body: [string, string];
  image: string;
  imageAlt: string;
};

/** 화면5 — 이렇게 이용해요 */
export type UsageStep = {
  num: string;
  title: string;
  /** title 뒤에 흐린 색으로 붙는 강조어 (예: "가지") */
  highlight?: string;
  description: string;
};
