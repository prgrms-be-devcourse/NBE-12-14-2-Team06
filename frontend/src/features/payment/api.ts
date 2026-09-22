import { api } from '@/lib/api';
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
