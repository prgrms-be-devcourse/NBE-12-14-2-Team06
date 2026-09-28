import { PROGRESS_ORDER, type MyApplicationDto } from '@/features/application';
import type { PostDto } from '@/features/post';
import { formatDateTime, formatDotDate, formatTime, formatTimeRange } from '../lib/date';
import type { ReportTarget, TrackingCase } from '../types';

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

/**
 * 완료한 진행 단계 수를 공고 상태로 추정합니다.
 *
 * ⚠️ 동행 진행 단계(EscortProgress)를 읽는 API 가 없습니다(쓰기 PATCH 만 있음).
 *    "동행 진행 중"은 출발(DEPARTED)까지만 확실하므로 2로 잡습니다 — 실제로는 병원 도착·귀가 중일 수
 *    있어, 그때 "다음 단계로" 버튼을 누르면 서버가 순서 오류로 거절합니다(화면에 그 메시지를 띄웁니다).
 *    GET .../progress 가 생기면 이 함수는 사라져야 합니다.
 */
function toDoneCount(postStatus: string): number {
  if (postStatus === 'COMPLETED') return PROGRESS_ORDER.length;
  if (postStatus === 'IN_PROGRESS') return 2;
  return 1; // MATCHED = 동행 시작 전까지 완료
}

/**
 * 내 지원 한 건(MyApplicationDto) + 그 공고(PostDto) + 이동 정보 → 동행 현황 화면.
 *
 * ⚠️ 의뢰인명·연락처·보호자 정보는 어느 응답에도 없어서 화면에서 뺐습니다.
 *    postStatus 는 ENUM 이름("IN_PROGRESS")입니다 — 한글을 내려주는 PostDto.postStatus 와 다릅니다.
 */
export function toTrackingCase(application: MyApplicationDto, post: PostDto, transport: string): TrackingCase {
  return {
    applicationId: application.applicationId,
    postId: application.postId,
    title: application.title,
    hospitalName: application.hospitalName,
    hospitalAddress: application.hospitalAddress,
    dateLabel: formatDotDate(application.escortStartAt),
    timeLabel: formatTime(application.escortStartAt),
    startAt: formatDateTime(application.escortStartAt),
    endAt: formatDateTime(application.escortEndAt),
    transport,
    note: post.patientNote?.trim() || '없음',
    doneCount: toDoneCount(application.postStatus),
  };
}
