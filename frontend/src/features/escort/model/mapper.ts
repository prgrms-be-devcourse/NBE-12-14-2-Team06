import { PROGRESS_ORDER, type ClientProfileDto, type EscortProgress, type MyApplicationDto } from '@/features/application';
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
 * 내 지원 한 건(MyApplicationDto) + 그 공고(PostDto) + 이동 정보 + 의뢰인·보호자 정보
 * (GET .../client-profile) + 실제 진행 단계(GET .../progress) → 동행 현황 화면.
 *
 * postStatus 는 ENUM 이름("IN_PROGRESS")입니다 — 한글을 내려주는 PostDto.postStatus 와 다릅니다.
 * doneCount 는 PROGRESS_ORDER 안에서 progress 의 위치 + 1 입니다. NOT_STARTED(index 0) 도 "동행
 * 시작 전까지는 완료"로 쳐서 1 이고, ARRIVED_HOME(index 5) 이면 6 = PROGRESS_ORDER.length 로 전부
 * 완료됩니다 — 예전처럼 postStatus 로 추측하지 않고, 백엔드가 실제로 기록한 단계를 그대로 씁니다.
 */
export function toTrackingCase(
  application: MyApplicationDto,
  post: PostDto,
  transport: string,
  progress: EscortProgress,
  clientProfile: ClientProfileDto,
): TrackingCase {
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
    clientName: clientProfile.clientName,
    clientPhone: clientProfile.clientPhone,
    emergencyContactName: clientProfile.emergencyContactName,
    emergencyContactPhone: clientProfile.emergencyContactPhone,
    careNote: clientProfile.careNote?.trim() || '없음',
    doneCount: PROGRESS_ORDER.indexOf(progress) + 1,
  };
}
