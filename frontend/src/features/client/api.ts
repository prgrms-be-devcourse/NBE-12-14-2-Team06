import { api } from '@/lib/api';

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
