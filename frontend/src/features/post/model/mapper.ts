import { daysUntil, formatAgo, formatDateTime, formatMonthDayTime, formatTime, parseDateTime } from '../lib/date';
import type { PostBadge, PostDetail, PostDto, PostSummary } from '../types';

/** 백엔드 PostStatus.OPEN 의 설명 문구 (PostDto.postStatus 는 한글 문구로 내려옵니다.) */
const OPEN_STATUS = '모집 중';
const ONE_DAY = 24 * 60 * 60 * 1000;

function toBadge(dto: PostDto): PostBadge {
  if (dto.postStatus !== OPEN_STATUS) return 'closed';
  if (daysUntil(dto.recruitEndAt) === 0) return 'closing';
  if (Date.now() - parseDateTime(dto.createdAt).getTime() < ONE_DAY) return 'new';
  return 'open';
}

/**
 * 백엔드 PostDto → 목록 카드용 PostSummary
 * - district: 백엔드에 구/군 필드가 없어서 병원 주소("인천 남동구 남동대로 774")의 두 번째 단어를 씁니다.
 * - description: 공고 내용(content)을 줄바꿈 기준으로 나눕니다.
 */
export function toPostSummary(dto: PostDto): PostSummary {
  return {
    id: dto.id,
    title: dto.title,
    hospitalName: dto.hospitalName,
    region: dto.region,
    district: dto.hospitalAddress.split(' ')[1] ?? '',
    postedAgo: formatAgo(dto.createdAt),
    startsInDays: daysUntil(dto.escortStartAt),
    startTime: formatTime(dto.escortStartAt),
    hours: dto.escortHours,
    hourlyPay: dto.hourlyPay,
    description: dto.content.split('\n').filter((line) => line.trim() !== ''),
    badge: toBadge(dto),
  };
}

/** 백엔드 PostDto → 상세 화면용 PostDetail */
export function toPostDetail(dto: PostDto): PostDetail {
  return {
    ...toPostSummary(dto),
    clientId: dto.client_id,
    postedAt: formatDateTime(dto.createdAt),
    hospitalAddress: dto.hospitalAddress,
    pickupAddress: dto.pickupAddress,
    details: dto.content.split('\n').filter((line) => line.trim() !== ''),
    patientNote: dto.patientNote ? dto.patientNote.split('\n').filter((line) => line.trim() !== '') : [],
    reportRequired: dto.reportRequired,
    recruitPeriod: `${formatMonthDayTime(dto.recruitStartAt)} ~ ${formatMonthDayTime(dto.recruitEndAt)}`,
  };
}
