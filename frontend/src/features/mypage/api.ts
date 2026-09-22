import type { CurrentUser } from '@/features/auth';
import { api, apiPatch, apiPost, apiPut, type SpringPage } from '@/lib/api';
import type { EscortProfileDto, ReviewDto, SettlementDto, SettlementStatus } from './types';

/** 기본 정보 수정 — PATCH /api/v1/users/profile. 백엔드가 수정할 때마다 비밀번호 재확인을 요구합니다. */
export type ProfileUpdateRequest = {
  password: string;
  email: string;
  name: string;
  /** "YYYY-MM-DD" */
  birthDate: string;
  phoneNum: string;
  region: string;
};

export function updateMyProfile(request: ProfileUpdateRequest): Promise<CurrentUser> {
  return apiPatch<CurrentUser>('/api/v1/users/profile', request);
}

/** 동행 매니저 추가 정보 — GET /api/v1/users/profile/escort (계좌 정보 포함, 본인만) */
export function fetchMyEscortProfile(): Promise<EscortProfileDto> {
  return api<EscortProfileDto>('/api/v1/users/profile/escort');
}

export type EscortProfileWriteRequest = {
  intro: string;
  bankName: string;
  accountHolder: string;
  accountNumber: string;
};

/** 동행 매니저 추가 정보 생성 — POST (아직 만든 적 없을 때) */
export function createMyEscortProfile(request: EscortProfileWriteRequest): Promise<EscortProfileDto> {
  return apiPost<EscortProfileDto>('/api/v1/users/profile/escort', request);
}

/** 동행 매니저 추가 정보 수정 — PUT (이미 만들어져 있을 때) */
export function updateMyEscortProfile(request: EscortProfileWriteRequest): Promise<EscortProfileDto> {
  return apiPut<EscortProfileDto>('/api/v1/users/profile/escort', request);
}

/** 정산 목록 — GET /api/v1/settlements (기간·페이지·정렬은 서버가 처리) */
export async function fetchSettlements(params: {
  status?: SettlementStatus;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}): Promise<{ settlements: SettlementDto[]; totalElements: number; totalPages: number }> {
  const query = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 50),
    sort: 'DESC',
  });
  if (params.startDate) query.set('startDate', params.startDate);
  if (params.endDate) query.set('endDate', params.endDate);

  const data = await api<SpringPage<SettlementDto>>(`/api/v1/settlements?${query.toString()}`);
  const settlements = params.status ? data.content.filter((item) => item.status === params.status) : data.content;
  return { settlements, totalElements: data.totalElements, totalPages: data.totalPages };
}

/** 정산 요청 — POST /api/v1/settlements/{settlementId} (정산 대기 상태에서만) */
export function requestSettlement(settlementId: number): Promise<void> {
  return apiPost<void>(`/api/v1/settlements/${settlementId}`);
}

/** 받은 리뷰 목록 — GET /api/v1/users/{userId}/reviews (동행 매니저만 대상) */
export function fetchMyReviews(userId: number): Promise<ReviewDto[]> {
  return api<ReviewDto[]>(`/api/v1/users/${userId}/reviews`);
}
