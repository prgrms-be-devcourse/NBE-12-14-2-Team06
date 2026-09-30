import { api, type ApiError } from '@/lib/api';
import type { ReportDto } from './types';

/** 진료 보고서 작성 요청 본문. department 는 ENUM 이름(예: "ORTHOPEDICS")으로 보냅니다. */
export type WriteReportPayload = {
  department: string;
  purpose: string;
  originContent: string;
  notes: string;
};

/** 진료 보고서 작성(동행인 전용) — POST /api/v1/applications/{applicationId}/report. 작성 응답도 조회와 같은 ReportDto 입니다. */
export async function writeReport(applicationId: number, payload: WriteReportPayload): Promise<ReportDto> {
  return api<ReportDto>(`/api/v1/applications/${applicationId}/report`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

/** 진료 보고서 조회(의뢰인·동행인 모두 가능) — GET /api/v1/applications/{applicationId}/report */
export async function fetchReport(applicationId: number): Promise<ReportDto> {
  return api<ReportDto>(`/api/v1/applications/${applicationId}/report`);
}

/** 아직 보고서가 작성되지 않았을 때(ReportService.get) 백엔드가 던지는 상태코드. 존재하지 않는 동행 건(404-24)과 구분하는 데 씁니다. */
const REPORT_NOT_FOUND_STATUS = '404-52';

/** fetchReport 실패가 "보고서 미작성"인지 판단합니다. */
export function isReportNotFoundError(error: unknown): boolean {
  return (error as Partial<ApiError>)?.statusCode === REPORT_NOT_FOUND_STATUS;
}
