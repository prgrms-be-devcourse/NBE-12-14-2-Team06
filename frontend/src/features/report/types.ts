/**
 * 백엔드 진료 보고서 응답 (ReportDto) — 작성·조회 API 가 돌려주는 모양 그대로.
 * department 는 응답에서 한글 과목명("정형외과")으로 내려옵니다. 요청 시에는 ENUM 이름을 보내야 합니다
 * (escort 도메인의 model/departments.ts — 보고서 작성은 동행인만 하므로 escort 쪽에 둡니다).
 */
export type ReportDto = {
  id: number;
  applicationId: number;
  title: string;
  department: string;
  purpose: string;
  originContent: string;
  notes: string | null;
  aiSummary: string | null;
  summarizedAt: string | null;
  createdAt: string;
  updatedAt: string;
};
