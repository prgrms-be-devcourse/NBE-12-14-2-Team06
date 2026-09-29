/**
 * Report.aiSummary 는 백엔드가 Claude 응답을 그대로 저장한 JSON 문자열입니다.
 * 동행인 화면(escort)과 의뢰인 화면(client)에서 함께 씁니다.
 */
export type ReportAiSummary = {
  visitPurpose: string | null;
  diagnosis: string | null;
  prescription: string | null;
  nextVisit: string | null;
  caregiverNote: string | null;
};

const LABELS: Record<keyof ReportAiSummary, string> = {
  visitPurpose: '방문 목적',
  diagnosis: '의사 소견',
  prescription: '처방 내역',
  nextVisit: '다음 방문 일정',
  caregiverNote: '보호자가 챙겨야 할 사항',
};

/**
 * aiSummary(JSON 문자열)를 파싱합니다.
 * null 이거나 형식이 깨져 있으면(요약 실패, 응답 변형 등) null 을 돌려주고 화면은 죽지 않습니다.
 */
export function parseAiSummary(aiSummary: string | null): ReportAiSummary | null {
  if (!aiSummary) return null;
  try {
    const parsed = JSON.parse(aiSummary) as Partial<ReportAiSummary>;
    return {
      visitPurpose: parsed.visitPurpose ?? null,
      diagnosis: parsed.diagnosis ?? null,
      prescription: parsed.prescription ?? null,
      nextVisit: parsed.nextVisit ?? null,
      caregiverNote: parsed.caregiverNote ?? null,
    };
  } catch {
    return null;
  }
}

/** null 이 아닌 항목만 "라벨 + 값" 목록으로 바꿉니다. */
export function aiSummaryItems(summary: ReportAiSummary): { label: string; value: string }[] {
  return (Object.keys(LABELS) as (keyof ReportAiSummary)[])
    .map((key) => ({ label: LABELS[key], value: summary[key] }))
    .filter((item): item is { label: string; value: string } => !!item.value);
}
