import type { MyApplicationDto } from '@/features/application';
import type { PostDto } from '@/features/post';
import { formatDotDate, formatTimeRange } from '../lib/date';
import type { ReportTarget } from '../types';

/**
 * 내 지원 한 건(MyApplicationDto) + 그 공고(PostDto) → 보고서 작성 화면의 "기본 정보" 카드.
 *
 * ⚠️ 의뢰인명은 어느 응답에도 없습니다. PostDto 의 client_id 는 실명이 아니라 로그인 아이디라
 *    화면에 쓰지 않고, 카드에서 그 줄을 뺐습니다. 의뢰인 이름을 내려주는 API 가 생기면 되살리세요.
 */
export function toReportTarget(application: MyApplicationDto, post: PostDto): ReportTarget {
  return {
    applicationId: application.applicationId,
    postId: application.postId,
    title: application.title,
    hospitalName: application.hospitalName,
    dateLabel: formatDotDate(application.escortStartAt),
    workTime: formatTimeRange(application.escortStartAt, application.escortEndAt),
    hospitalAddress: application.hospitalAddress,
    // 환자 특이사항은 의뢰인이 안 썼을 수 있습니다(null).
    note: post.patientNote?.trim() || '없음',
  };
}
