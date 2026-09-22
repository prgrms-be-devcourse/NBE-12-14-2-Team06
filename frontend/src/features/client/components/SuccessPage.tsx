'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';
import { ApiError } from '@/lib/api';
import { fetchConfirmPayment, type PaymentConfirm } from '../api';
import './PaymentPage.css';

/** 승인 성공 화면을 보여 준 뒤 공고 등록 완료 화면으로 넘어가기까지의 시간 */
const REDIRECT_DELAY_MS = 3000;

/**
 * 결제 성공 — 토스 successUrl 착지 지점
 *
 * 토스가 붙여 주는 orderId·paymentKey·amount 로 서버에 승인을 요청하고,
 * 성공하면 결제 결과를 잠깐 보여 준 뒤 공고 등록 완료 화면으로 이동합니다.
 * paymentId·postId·pay 는 결제 화면이 successUrl 쿼리에 미리 실어 보낸 값입니다.
 * (paymentId 는 공고 등록 응답 PostWriteResponse 에서 내려옵니다.)
 */
export default function SuccessPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [confirmed, setConfirmed] = useState<PaymentConfirm | null>(null);

  const orderId = searchParams.get('orderId') ?? '';
  const paymentKey = searchParams.get('paymentKey') ?? '';
  const amount = searchParams.get('amount') ?? '0';
  const paymentId = Number(searchParams.get('paymentId'));
  const postId = searchParams.get('postId') ?? '1';
  const pay = searchParams.get('pay') ?? '0';

  const completeHref = `/client/posts/complete?${new URLSearchParams({ postId, pay, amount })}`;

  // 개발 모드(StrictMode)에서 effect 가 두 번 돌아 승인 API 가 중복 호출되는 것을 막습니다.
  const requestedRef = useRef(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!requestedRef.current) {
      requestedRef.current = true;

      // paymentId 가 없으면 승인 자체가 불가능하므로 서버를 부르지 않고 바로 실패 처리합니다.
      const confirm = Number.isInteger(paymentId) && paymentId > 0
        ? fetchConfirmPayment(paymentId, { orderId, paymentKey, amount })
        : Promise.reject(new Error('결제 정보(paymentId)가 없어 승인할 수 없습니다.'));

      confirm
        .then((data) => {
          setConfirmed(data);
          timerRef.current = setTimeout(() => router.replace(completeHref), REDIRECT_DELAY_MS);
        })
        .catch((error: unknown) => {
          const code = error instanceof ApiError ? error.statusCode : 'UNKNOWN';
          const message = error instanceof Error ? error.message : '결제 승인에 실패했습니다.';
          const query = new URLSearchParams({
            code,
            message,
            postId,
            paymentId: searchParams.get('paymentId') ?? '',
            pay,
            amount,
          });
          router.replace(`/client/posts/new/payment/fail?${query}`);
        });
    }

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [orderId, paymentKey, amount, paymentId, postId, pay, completeHref, searchParams, router]);

  return (
    <>
      <div className="box_section" style={{ width: '600px' }}>
        {/* Tailwind preflight 가 img 를 display:block 으로 만들어서, box_section 의 text-align 으로는
            가운데로 오지 않습니다. 좌우 여백을 auto 로 두어 직접 가운데 정렬합니다. */}
        <img
          width="100px"
          src="https://static.toss.im/illusts/check-blue-spot-ending-frame.png"
          alt=""
          className="mx-auto"
        />
        <h2>{confirmed ? '결제를 완료했어요' : '결제를 확인하고 있어요'}</h2>

        <div className="p-grid typography--p" style={{ marginTop: '50px' }}>
          <div className="p-grid-col text--left">
            <b>결제금액</b>
          </div>
          <div className="p-grid-col text--right" id="amount">
            {`${Number(amount).toLocaleString()}원`}
          </div>
        </div>
        <div className="p-grid typography--p" style={{ marginTop: '10px' }}>
          <div className="p-grid-col text--left">
            <b>주문번호</b>
          </div>
          <div className="p-grid-col text--right" id="orderId">
            {orderId}
          </div>
        </div>
        <div className="p-grid typography--p" style={{ marginTop: '10px' }}>
          <div className="p-grid-col text--left">
            <b>paymentKey</b>
          </div>
          <div className="p-grid-col text--right" id="paymentKey" style={{ whiteSpace: 'initial', width: '250px' }}>
            {paymentKey}
          </div>
        </div>

        <div className="p-grid-col">
          <p className="typography--p" style={{ marginTop: '30px', color: '#4e5968' }}>
            {confirmed ? '잠시 후 공고 등록 완료 화면으로 이동합니다.' : '승인 결과를 기다리는 중입니다.'}
          </p>
          <button
            type="button"
            className="button"
            disabled={!confirmed}
            onClick={() => router.replace(completeHref)}
          >
            공고 등록 완료로 이동
          </button>
        </div>
      </div>

      <div className="box_section" style={{ width: '600px', textAlign: 'left' }}>
        <b>Response Data :</b>
        <div id="response" style={{ whiteSpace: 'initial' }}>
          {confirmed && <pre>{JSON.stringify(confirmed, null, 4)}</pre>}
        </div>
      </div>
    </>
  );
}
