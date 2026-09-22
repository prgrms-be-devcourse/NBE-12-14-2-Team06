import { fetchApplicants } from '@/features/application';
import { fetchPosts, type PostFilters } from '@/features/post';
import { fetchProfile } from '@/features/user';
import { api } from '@/lib/api';
import { toClientPost } from './model/posts';
import type { ClientPost } from './types';

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

const PAGE_SIZE = 100;
const NO_FILTER: Omit<PostFilters, 'openOnly'> = { keyword: '', region: 'all', period: 'all', pay: 'all', sort: 'latest' };

/** 지원자 목록까지 확인해야 하는 상태 (매칭 완료 이후). 모집 중인 공고는 조회할 필요가 없습니다. */
const MATCHED_OR_LATER = new Set(['매칭 완료', '동행 진행 중', '동행 완료']);

/**
 * 내가 작성한 공고 목록.
 *
 * ⚠️ 서버에 "내 공고만" 조회하는 API 가 없어서, 전체 목록을 받아 화면에서 client_id 로 걸러냅니다.
 *    임시 방편이며 다음 한계가 있습니다.
 *    - 결제(Payment)가 DONE 인 공고만 GET /api/v1/posts 가 내려줍니다. 결제 전 공고는 여기 나타나지 않습니다.
 *    - openOnly=true/false 를 각각 size=100 으로만 불러오므로, 공고가 100건을 넘으면 뒤쪽이 누락될 수 있습니다.
 *    - 서버에 "내 공고 목록" API(예: GET /api/v1/posts/me)가 생기면 이 필터링은 지우고 그걸 쓰면 됩니다.
 */
export async function fetchMyPosts(): Promise<ClientPost[]> {
  const profile = await fetchProfile();

  const [open, notOpen] = await Promise.all([
    fetchPosts({ ...NO_FILTER, openOnly: true }, 0, PAGE_SIZE),
    fetchPosts({ ...NO_FILTER, openOnly: false }, 0, PAGE_SIZE),
  ]);

  const mine = [...open.posts, ...notOpen.posts]
    .filter((post) => post.clientId === profile.username)
    // PostSummary 에는 생성 시각이 없어, id 가 클수록 최근에 만든 공고라고 보고 정렬합니다.
    .sort((a, b) => b.id - a.id);

  return Promise.all(
    mine.map(async (post) => {
      if (!MATCHED_OR_LATER.has(post.postStatus)) return toClientPost(post);

      const applicants = await fetchApplicants(post.id, 0, 100).catch(() => []);
      const accepted = applicants.find((item) => item.status === 'ACCEPTED');
      return toClientPost(post, accepted ? { applicationId: accepted.applicationId, escortName: accepted.escortName } : undefined);
    }),
  );
}
