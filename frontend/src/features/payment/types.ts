/** 백엔드 결제 처리 상태 (PaymentStatus) */
export type PaymentStatus =
  | 'READY'
  | 'IN_PROGRESS'
  | 'DONE'
  | 'CANCELED'
  | 'PARTIAL_CANCELED'
  | 'ABORTED'
  | 'EXPIRED';

/** 백엔드 결제 응답 (PaymentResponse) — 결제 조회 API 가 돌려주는 모양 그대로 */
export type PaymentDto = {
  id: number;
  /** 결제해야 하는(또는 결제한) 금액 */
  amount: number;
  /** 결제 시점의 시급 */
  hourlyPaySnapshot: number;
  /** 결제 기준 동행 시간. 백엔드는 BigDecimal 이지만 JSON 에서는 숫자 */
  hours: number;
  /** 토스 주문 번호. 아직 결제 전(READY)이면 없습니다. */
  orderId: string | null;
  paymentStatus: PaymentStatus;
  approvedAt: string | null;
  canceledAt: string | null;
  cancelReason: string | null;
};
