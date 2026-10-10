import { ApiError, api, apiPost } from '@/lib/api';
import type { EscortLocationDto, SendLocationRequest } from './types';

/**
 * 동행 매니저의 현재 위치 전송 — POST /api/v1/applications/{applicationId}/location
 * 승인(ACCEPTED)된 지원의 동행 매니저 본인이, 동행이 진행 중(IN_PROGRESS)일 때만 보낼 수 있습니다.
 * 서버에는 가장 최근 위치 1건만 남고, 보낼 때마다 덮어씁니다.
 */
export function sendLocation(applicationId: number, body: SendLocationRequest): Promise<void> {
  return apiPost<void>(`/api/v1/applications/${applicationId}/location`, body);
}

/**
 * 동행 매니저의 가장 최근 위치 조회 — GET /api/v1/applications/{applicationId}/location
 * 해당 공고의 의뢰인·동행 매니저 본인·관리자만 볼 수 있습니다.
 * 아직 위치가 올라오지 않았으면(404 이거나 data 가 null) null 을 돌려줍니다.
 */
export async function fetchLocation(applicationId: number): Promise<EscortLocationDto | null> {
  try {
    const data = await api<EscortLocationDto | null>(`/api/v1/applications/${applicationId}/location`);
    return data ?? null;
  } catch (error) {
    if (error instanceof ApiError && error.statusCode.startsWith('404')) return null;
    throw error;
  }
}
