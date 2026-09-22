/**
 * application(지원) 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 */
export {
  applyToPost,
  fetchApplicants,
  fetchEscortProfile,
  acceptApplication,
  rejectApplication,
  cancelApplication,
  advanceProgress,
} from './api';
export { PROGRESS_ORDER } from './types';
export type {
  ApplicationStatus,
  EscortProgress,
  ApplicantDto,
  Applicant,
  EscortProfileDto,
  ApplicationApplyDto,
  ApplicationAcceptDto,
} from './types';
