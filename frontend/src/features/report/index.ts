/**
 * report(진료 보고서) 도메인의 공개 API.
 * escort(동행인)·client(의뢰인) 두 화면이 같은 보고서를 보여주므로 어느 한쪽에 속하지 않고
 * 여기서 함께 쓴다. 이 도메인 바깥에서는 반드시 여기를 통해서만 import 합니다.
 */
export { fetchReport, isReportNotFoundError, writeReport } from './api';
export type { WriteReportPayload } from './api';
export { aiSummaryItems, parseAiSummary } from './model/aiSummary';
export type { ReportAiSummary } from './model/aiSummary';
export type { ReportDto } from './types';
