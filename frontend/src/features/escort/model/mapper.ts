import type { MyApplicationDto } from '@/features/application';
import type { PostDto } from '@/features/post';
import { formatDotDate, formatTimeRange } from '../lib/date';
import type { ReportTarget } from '../types';

/**
 * 내 지원 한 건(MyApplicationDto) + 그 공고(PostDto) → 보고서 작성·조회 화면의 "기본 정보" 카드.
 *
 * ⚠️ 의뢰인명은 여기서 못 만듭니다. 두 응답 모두 의뢰인 이름이 없고, PostDto 의 client_id 는
 *    실명이 아니라 로그인 아이디입니다. 조회 화면은 ReportDto.clientName 으로 따로 채우고,
 *    작성 화면은 그 줄이 빠져 있습니다. MyApplicationResponse 에 clientName 이 생기면 여기로 모으세요.
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
