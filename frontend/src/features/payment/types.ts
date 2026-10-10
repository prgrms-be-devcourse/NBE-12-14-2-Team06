/**
 * 백엔드 결제 처리 상태 (PaymentStatus) — 백엔드 enum 과 값·개수를 그대로 맞춥니다.
 *
 * READY            아직 내지 않은 결제 (공고 등록 직후·결제를 취소한 뒤 새로 생긴 건)
 * IN_PROGRESS      결제 승인 중. PaymentPersistenceService.confirmUpdateStatus 가 쓰지만 호출되는 곳이 없어 실제로는 안 보입니다.
 * DONE             결제 완료
 * CANCELED         전액 취소
 * PARTIAL_CANCELED 부분 취소 (실제 동행 시간이 짧아져 차액을 환불한 경우)
 * DELETED          공고가 삭제돼 스케줄러가 취소할 예정인 결제
 */
export type PaymentStatus =
  | 'READY'
  | 'IN_PROGRESS'
  | 'DONE'
  | 'CANCELED'
  | 'PARTIAL_CANCELED'
  | 'DELETED';

/** 백엔드 결제 응답 (PaymentResponse) — 결제 조회 API 가 돌려주는 모양 그대로 */
export type PaymentDto = {
  id: number;
  /**
   * 이 결제가 달린 공고 번호. 결제 목록(GET /api/v1/payments)을 공고별로 나눌 때 씁니다.
   * 백엔드 Payment.post 에 nullable=false 가 없어 null 이 올 수 있습니다.
   */
  postId: number | null;
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
