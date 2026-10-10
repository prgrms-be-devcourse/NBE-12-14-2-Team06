import { api, apiDelete } from '@/lib/api';
import type { PaymentDto } from './types';

/**
 * 공고에 남아 있는 미결제(READY) 건 조회 — GET /api/v1/payments/posts/{postId}
 *
 * 동행이 완료되면 백엔드(PaymentService.validPayment)가 실제 동행 시간으로 금액을 다시 계산합니다.
 * 처음 결제한 금액보다 많으면 차액만큼 READY 상태의 결제를 새로 만들어 두는데, 그게 여기로 내려옵니다.
 * (적게 나왔으면 백엔드가 알아서 부분 취소하므로 결제할 게 남지 않습니다.)
 *
 * ⚠️ 추가 결제가 필요 없으면 data 가 null 입니다. 404 가 아니라 200 + null 이라 catch 가 아니라 null 검사로 분기합니다.
 * ⚠️ 최초 결제를 아직 안 한 공고도 READY 건이 있어 여기에 잡힙니다.
 *    "추가 결제"인지는 공고 상태(동행 완료)로 판단해야 합니다. 본보기: features/post/components/PostDetailPage.tsx
 *
 * 결제 세션 저장(save-amount)·승인(confirm)은 토스 위젯 화면에 붙어 있어서
 * features/client/api.ts 에 있습니다.
 */
export function fetchPendingPayment(postId: number): Promise<PaymentDto | null> {
  return api<PaymentDto | null>(`/api/v1/payments/posts/${postId}`);
}

/**
 * 내 결제 내역 전체 — GET /api/v1/payments (로그인 쿠키 필요)
 *
 * ⚠️ 상태를 가리지 않고 전부 내려옵니다. 취소(CANCELED)·삭제(DELETED)된 건도 섞여 있고,
 *    한 공고에 결제가 여러 개일 수 있습니다(취소하면 새 결제가 생기는 구조 — Payment.cancelPayment).
 *    "아직 안 낸 결제"만 필요하면 fetchUnpaidByPost 를 쓰세요.
 */
export function fetchMyPayments(): Promise<PaymentDto[]> {
  return api<PaymentDto[]>('/api/v1/payments');
}

/**
 * 공고 번호 → 아직 내지 않은(READY) 결제. 공고 목록에 "미결제" 표시를 붙일 때 씁니다.
 * 공고마다 GET /api/v1/payments/posts/{postId} 를 부르지 않고 목록 한 번으로 끝내기 위한 함수입니다.
 *
 * 한 공고에 READY 가 여러 개면 가장 나중에 만들어진 것(id 가 큰 것) 하나만 남깁니다 — 결제를 취소하면
 * 백엔드가 새 READY 결제를 만들기 때문에, 공고의 "지금 내야 하는 금액"은 항상 마지막 건입니다.
 *
 * ⚠️ READY 만 봅니다. 결제 중(IN_PROGRESS)으로 바꾸는 코드(PaymentPersistenceService.confirmUpdateStatus)가
 *    아직 어디서도 호출되지 않아 실제로 그 상태가 되는 결제는 없습니다. 호출되기 시작하면 여기도 같이 넓혀야 합니다.
 */
export async function fetchUnpaidByPost(): Promise<Map<number, PaymentDto>> {
  const payments = await fetchMyPayments();
  const unpaid = new Map<number, PaymentDto>();

  for (const payment of payments) {
    if (payment.paymentStatus !== 'READY' || payment.postId == null) continue;
    const previous = unpaid.get(payment.postId);
    if (!previous || previous.id < payment.id) unpaid.set(payment.postId, payment);
  }
  return unpaid;
}

/**
 * 공고 번호 → 그 공고의 결제 전부. 최근에 만들어진 것(id 가 큰 것)이 앞에 옵니다.
 * 공고 상세의 "결제 정보" 카드가 결제 이력을 그대로 늘어놓는 데 씁니다.
 *
 * 공고별 조회(GET /api/v1/payments/posts/{postId})는 아직 내지 않은 READY 건만 돌려주고
 * 백엔드에 "공고의 결제 전체" 조회가 없어서, 내 결제 목록(상태 조건 없는 findAllByUserId)을 받아
 * 공고 번호로 추립니다. 상태는 걸러내지 않습니다 — 취소(CANCELED)·부분 취소·미결제까지 다 들어옵니다.
 *
 * 한 공고에 결제가 여러 개인 경우: 최초 결제 + 동행 시간이 늘어 생긴 추가 결제,
 * 그리고 결제를 취소하면 백엔드가 같은 금액의 READY 건을 새로 만들어 둡니다(Payment.cancelPayment).
 */
export async function fetchPaymentsByPost(postId: number): Promise<PaymentDto[]> {
  const payments = await fetchMyPayments();

  return payments
    .filter((payment) => payment.postId === postId)
    .sort((first, second) => second.id - first.id);
}

/**
 * 취소할 수 있는 결제인지 — 백엔드 PaymentService.cancel 과 같은 조건입니다.
 *
 * 결제가 성공한 상태(DONE)와 일부만 환불된 상태(PARTIAL_CANCELED)를 취소할 수 있습니다.
 * 부분 취소된 건은 남은 금액(amount)만큼 전액 취소됩니다.
 * 그 밖의 상태는 서버가 거절합니다 — 이미 취소됨 → InvalidException 44, 나머지 → 45.
 *
 * ⚠️ 공고 상태(매칭·동행 완료)로 막는 조건은 공고 쪽에서 따로 봅니다 — InvalidException 46.
 */
export function isCancelablePayment(payment: PaymentDto): boolean {
  return payment.paymentStatus === 'DONE' || payment.paymentStatus === 'PARTIAL_CANCELED';
}

/**
 * 결제 취소 — DELETE /api/v1/payments/{paymentId} (결제한 의뢰인 본인만, 취소 사유는 필수 본문)
 *
 * 백엔드가 토스 취소 API 를 부른 뒤 이 결제를 CANCELED 로 바꾸고,
 * 같은 금액의 READY 결제를 새로 만들어 둡니다(Payment.cancelPayment) — 즉 공고는 "미결제"로 되돌아가
 * 다시 결제할 수 있습니다. 공고 자체는 지워지지 않습니다.
 */
export function cancelPayment(paymentId: number, cancelReason: string): Promise<void> {
  return apiDelete<void>(`/api/v1/payments/${paymentId}`, { cancelReason });
}
