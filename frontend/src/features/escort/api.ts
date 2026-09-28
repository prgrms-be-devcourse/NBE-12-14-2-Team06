import { fetchClientProfile, fetchMyApplications, fetchProgress, type MyApplicationDto } from '@/features/application';
import { fetchPostRaw } from '@/features/post';
import { fetchRidesByPost, formatTransport } from '@/features/ride';
import { toReportTarget, toTrackingCase } from './model/mapper';
import type { ReportTarget, TrackingCase } from './types';

/**
 * 내 지원 목록에서 동행 건 하나를 찾습니다.
 * "내 동행 건 하나만 조회" 하는 API 가 없어서 목록(GET /api/v1/applications/me)을 받아 거릅니다.
 * 목록에 없으면(= 내 동행 건이 아니면) undefined 를 돌려줍니다.
 */
async function findMyApplication(applicationId: number): Promise<MyApplicationDto | undefined> {
  const applications = await fetchMyApplications();
  return applications.find((item) => item.applicationId === applicationId);
}

/**
 * 보고서 작성·조회 화면의 기본 정보.
 * 환자 특이사항만 공고(GET /api/v1/posts/{postId})에서 가져옵니다.
 */
export async function fetchReportTarget(applicationId: number): Promise<ReportTarget | undefined> {
  const application = await findMyApplication(applicationId);
  if (!application) return undefined;

  const post = await fetchPostRaw(application.postId);
  return toReportTarget(application, post);
}

/**
 * 동행 현황 화면의 동행 정보.
 * 환자 특이사항은 공고에서, 이동수단은 이동 정보(GET /api/v1/rides/posts/{postId})에서 가져옵니다.
 */
export async function fetchTrackingCase(applicationId: number): Promise<TrackingCase | undefined> {
  const application = await findMyApplication(applicationId);
  if (!application) return undefined;

  const [post, rides, progress, clientProfile] = await Promise.all([
    fetchPostRaw(application.postId),
    fetchRidesByPost(application.postId),
    fetchProgress(applicationId),
    fetchClientProfile(applicationId),
  ]);
  return toTrackingCase(application, post, formatTransport(rides), progress.progress, clientProfile);
}
