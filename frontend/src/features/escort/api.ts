import { fetchMyApplications } from '@/features/application';
import { fetchPostRaw } from '@/features/post';
import { toReportTarget } from './model/mapper';
import type { ReportTarget } from './types';

/**
 * 보고서 작성 화면의 기본 정보 — GET /api/v1/applications/me 에서 해당 동행 건을 찾고,
 * 환자 특이사항만 공고(GET /api/v1/posts/{postId})에서 가져옵니다.
 *
 * "내 동행 건 하나만 조회" 하는 API 가 없어서 내 지원 목록을 받아 걸러 씁니다.
 * 목록에 없으면(= 내 동행 건이 아니면) undefined 를 돌려줍니다.
 */
export async function fetchReportTarget(applicationId: number): Promise<ReportTarget | undefined> {
  const applications = await fetchMyApplications();
  const application = applications.find((item) => item.applicationId === applicationId);
  if (!application) return undefined;

  const post = await fetchPostRaw(application.postId);
  return toReportTarget(application, post);
}
