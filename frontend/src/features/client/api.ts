import { fetchApplicants, fetchEscortProfile, fetchProgress } from '@/features/application';
import { fetchMyPostsRaw, fetchPostRaw } from '@/features/post';
import { fetchUserReviews } from '@/features/review';
import { fetchRidesByPost, formatTransport } from '@/features/ride';
import { api, apiPatch } from '@/lib/api';
import { toClientEscortCase } from './model/escort';
import { toManager, topReviewTagLabels } from './model/mapper';
import { toClientPost } from './model/posts';
import type { ClientEscortCase, ClientPost } from './types';

/**
 * 의뢰인 화면(동행 현황·보고서·리뷰)이 함께 쓰는 동행 한 건.
 * 공고·매니저·이동수단을 한 번에 모읍니다.
 *
 * ⚠️ postId 는 화면이 쿼리(?postId=)로 받아 넘깁니다. 백엔드에 "신청 상세 조회"(GET /applications/{id})가
 *    없어서 applicationId 만으로는 공고를 찾을 수 없기 때문입니다. 그 API 가 생기면 쿼리 의존을 걷어내세요.
 */
export async function fetchClientEscortCase(postId: number, applicationId: number): Promise<ClientEscortCase> {
  const [post, profile, rides, progress] = await Promise.all([
    fetchPostRaw(postId),
    fetchEscortProfile(applicationId),
    fetchRidesByPost(postId),
    fetchProgress(applicationId),
  ]);

  // 리뷰는 매니저 카드의 태그 계산용이라, 실패해도 나머지 화면은 그대로 보여줍니다.
  const reviews = await fetchUserReviews(profile.escortId).catch(() => []);
  const manager = toManager(profile, topReviewTagLabels(reviews));

  return toClientEscortCase(post, applicationId, manager, formatTransport(rides), progress.progress);
}

/**
 * 결제를 요청하기 전에 orderId·금액을 서버 세션에 저장합니다.
 * 결제 과정에서 악의적으로 결제 금액이 바뀌는 것을 승인 단계에서 검증하기 위한 용도입니다.
 *
 * ⚠️ 백엔드 SaveAmountRequest(String orderId, String amount) 가 둘 다 String 이고
 *    승인 때 session.getAttribute("amount") 를 String 으로 캐스팅하므로 금액도 문자열로 보냅니다.
 */
export async function fetchSaveAmount(orderId: string, amount: number) {
    await api<void>('/api/v1/payments/save-amount', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderId, amount: String(amount) }),
    });
}

/** 결제 승인 요청/응답 (백엔드 PaymentConfirmRequest·PaymentConfirmResponse) */
export type PaymentConfirm = {
    orderId: string;
    paymentKey: string;
    amount: string;
};

/**
 * 토스에서 successUrl 로 돌아온 뒤 서버에 결제 승인을 요청합니다.
 * 서버가 세션에 저장해 둔 금액과 대조한 다음 토스 승인 API 를 호출합니다.
 */
export async function fetchConfirmPayment(paymentId: number, request: PaymentConfirm) {
    return api<PaymentConfirm>(`/api/v1/payments/${paymentId}/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    });
}

/**
 * 동행 완료 처리 — PATCH /api/v1/posts/{postId}/escortComplete (공고를 쓴 의뢰인 본인만)
 *
 * 서버가 진행 로그의 출발·귀가 완료 시각으로 공고의 실제 동행 시간을 채우고 상태를 "동행 완료"로 바꾼 뒤,
 * 동행 매니저의 완료 건수 증가와 정산·재결제까지 이어서 처리합니다.
 * 동행인이 아직 "귀가 완료"를 찍지 않았으면 404 "귀가완료 기록이 없습니다" 로 거절합니다.
 */
export function completeEscort(postId: number): Promise<void> {
  return apiPatch<void>(`/api/v1/posts/${postId}/escortComplete`);
}

/** 지원자 목록까지 확인해야 하는 상태 (매칭 완료 이후). 모집 중인 공고는 조회할 필요가 없습니다. */
const MATCHED_OR_LATER = new Set(['매칭 완료', '동행 진행 중', '동행 완료']);

/**
 * 내가 작성한 공고 목록 — GET /api/v1/posts/me 로 결제 상태와 무관하게 전부 받아옵니다
 * (공개 목록 GET /api/v1/posts 는 결제 완료된 공고만 내려줘서, 대기/부분취소 상태인 내 공고까지
 * 보여주려면 이 전용 API 를 써야 합니다).
 *
 * ⚠️ page=0, size=100 으로만 불러오므로, 공고가 100건을 넘으면 뒤쪽이 누락될 수 있습니다.
 */
export async function fetchMyPosts(): Promise<ClientPost[]> {
  const { posts } = await fetchMyPostsRaw(0, 100);

  return Promise.all(
    posts.map(async (post) => {
      if (!MATCHED_OR_LATER.has(post.postStatus)) return toClientPost(post);

      const applicants = await fetchApplicants(post.id, 0, 100).catch(() => []);
      const accepted = applicants.find((item) => item.status === 'ACCEPTED');
      return toClientPost(post, accepted ? { applicationId: accepted.applicationId, escortName: accepted.escortName } : undefined);
    }),
  );
}
