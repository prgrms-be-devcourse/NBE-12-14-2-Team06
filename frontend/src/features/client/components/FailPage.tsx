'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import './PaymentPage.css';

/**
 * 결제 실패 — 토스 failUrl 착지 지점
 *
 * 토스가 결제창에서 실패했을 때(code·message)와,
 * 승인 단계에서 서버가 거절했을 때(SuccessPage 가 리다이렉트) 둘 다 여기로 옵니다.
 */
export default function FailPage() {
  const searchParams = useSearchParams();

  const code = searchParams.get('code') ?? 'UNKNOWN';
  const message = searchParams.get('message') ?? '알 수 없는 오류가 발생했습니다.';
  const postId = searchParams.get('postId') ?? '1';
  const paymentId = searchParams.get('paymentId') ?? '';
  const pay = searchParams.get('pay') ?? '0';
  const amount = searchParams.get('amount') ?? '0';

  // 실패했으니 결제 화면으로 되돌아가 같은 공고를 다시 결제할 수 있게 합니다.
  const retryHref = `/client/posts/new/payment?${new URLSearchParams({ postId, paymentId, pay, amount })}`;

  return (
    <div id="info" className="box_section" style={{ width: '600px' }}>
      <img width="100px" src="https://static.toss.im/lotties/error-spot-no-loop-space-apng.png" alt="에러 이미지" />
      <h2>결제를 실패했어요</h2>

      <div className="p-grid typography--p" style={{ marginTop: '50px' }}>
        <div className="p-grid-col text--left">
          <b>에러메시지</b>
        </div>
        <div className="p-grid-col text--right" id="message">
          {message}
        </div>
      </div>
      <div className="p-grid typography--p" style={{ marginTop: '10px' }}>
        <div className="p-grid-col text--left">
          <b>에러코드</b>
        </div>
        <div className="p-grid-col text--right" id="code">
          {code}
        </div>
      </div>

      <div className="p-grid-col">
        <Link href={retryHref}>
          <button type="button" className="button p-grid-col5">
            다시 결제하기
          </button>
        </Link>
        <Link href="/client/posts">
          <button type="button" className="button p-grid-col5" style={{ backgroundColor: '#e8f3ff', color: '#1b64da' }}>
            내 공고 목록으로
          </button>
        </Link>
      </div>
    </div>
  );
}
