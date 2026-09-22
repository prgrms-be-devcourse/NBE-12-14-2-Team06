import { api } from '@/lib/api';
import type { ReportDto } from './types';

/** 진료 보고서 작성 요청 본문. department 는 ENUM 이름(예: "ORTHOPEDICS")으로 보냅니다. */
export type WriteReportPayload = {
  department: string;
  purpose: string;
  originContent: string;
  notes: string;
};

/** POST 응답(ReportWriteResponse)은 updatedAt 이 없습니다. */
type ReportWriteResponse = Omit<ReportDto, 'updatedAt'>;

/** 진료 보고서 작성 — POST /api/v1/applications/{applicationId}/report */
export async function writeReport(applicationId: number, payload: WriteReportPayload): Promise<ReportWriteResponse> {
  return api<ReportWriteResponse>(`/api/v1/applications/${applicationId}/report`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

/** 진료 보고서 조회 — GET /api/v1/applications/{applicationId}/report */
export async function fetchReport(applicationId: number): Promise<ReportDto> {
  return api<ReportDto>(`/api/v1/applications/${applicationId}/report`);
}
