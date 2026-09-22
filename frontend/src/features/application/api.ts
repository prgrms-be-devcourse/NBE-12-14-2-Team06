import { api, apiPatch, apiPost, type SpringPage } from '@/lib/api';
import { PROGRESS_ORDER } from './types';
import type {
  ApplicantDto,
  ApplicationAcceptDto,
  ApplicationApplyDto,
  EscortProfileDto,
  EscortProgress,
} from './types';

/**
 * 공고에 지원하기 — POST /api/v1/applications/{postId}
 * 동행 매니저(ESCORT)로 로그인해야 하고, 모집 중인 공고에만, 같은 공고엔 한 번만 지원할 수 있습니다.
 */
export function applyToPost(postId: number): Promise<ApplicationApplyDto> {
  return apiPost<ApplicationApplyDto>(`/api/v1/applications/${postId}`);
}

/**
 * 공고의 지원자 목록 조회 — GET /api/v1/applications/posts/{postId}
 * 그 공고를 작성한 의뢰인 본인만 볼 수 있습니다.
 */
export async function fetchApplicants(postId: number, page = 0, size = 20): Promise<ApplicantDto[]> {
  const data = await api<SpringPage<ApplicantDto>>(`/api/v1/applications/posts/${postId}?page=${page}&size=${size}`);
  return data.content;
}

/** 지원자 프로필 조회 — GET /api/v1/applications/{applicationId}/escort-profile (그 공고의 작성자만) */
export function fetchEscortProfile(applicationId: number): Promise<EscortProfileDto> {
  return api<EscortProfileDto>(`/api/v1/applications/${applicationId}/escort-profile`);
}

/** 지원 승인 — PATCH /api/v1/applications/{applicationId}/accept (같은 공고의 나머지 대기 지원은 서버가 자동으로 거절 처리합니다) */
export function acceptApplication(applicationId: number): Promise<ApplicationAcceptDto> {
  return apiPatch<ApplicationAcceptDto>(`/api/v1/applications/${applicationId}/accept`);
}

/** 지원 거절 — PATCH /api/v1/applications/{applicationId}/reject */
export function rejectApplication(applicationId: number): Promise<void> {
  return apiPatch<void>(`/api/v1/applications/${applicationId}/reject`);
}

/** 지원 취소(동행 매니저 본인) — PATCH /api/v1/applications/{applicationId}/cancel */
export function cancelApplication(applicationId: number): Promise<void> {
  return apiPatch<void>(`/api/v1/applications/${applicationId}/cancel`);
}

/**
 * 동행 진행 상태를 한 단계 다음으로 — PATCH /api/v1/applications/{applicationId}/progress
 * 서버가 "현재 상태의 바로 다음 단계"만 허용하므로, 여기서 다음 값을 계산해서 보냅니다.
 * 지원을 승인받은 동행 매니저 본인만 바꿀 수 있습니다.
 */
export function advanceProgress(applicationId: number, currentProgress: EscortProgress): Promise<void> {
  const nextIndex = PROGRESS_ORDER.indexOf(currentProgress) + 1;
  const next = PROGRESS_ORDER[nextIndex];
  if (!next) throw new Error('이미 동행이 완료되었습니다.');
  return apiPatch<void>(`/api/v1/applications/${applicationId}/progress`, { progress: next });
}
