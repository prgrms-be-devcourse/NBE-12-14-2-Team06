'use client';

import './PaymentPage.css';
import {
  loadTossPayments,
  type TossPaymentsWidgets,
} from "@tosspayments/tosspayments-sdk";
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from "react";
import Link from 'next/link';
import { useRequireAuth } from '@/features/auth';
import { fetchSaveAmount } from '../api';

// SDK 가 Amount 타입을 export 하지 않으므로 setAmount 의 파라미터에서 가져옵니다.
type Amount = Parameters<TossPaymentsWidgets["setAmount"]>[0];

const clientKey = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm";

function generateRandomString() {
  return btoa(Math.random().toString()).slice(0, 20);
}

/** SDK 의 customerMobilePhone 은 `-` 없는 숫자 8~15자만 받습니다. (DB 는 010-1000-0001 형태로 저장) */
function toDigits(phoneNum: string) {
  return phoneNum.replace(/\D/g, '');
}

/**
 * 결제 방법 — Figma 의뢰인_결제 459:3067
 *
 * ⚠️ 디자인이 결제 위젯(HTML) 화면이라 같은 구성의 모양만 그렸습니다. 실제로는 결제되지 않습니다.
 * TODO: 토스페이먼츠 결제 위젯 SDK 로 교체하고, 결제 성공 후 서버에서 결제(Payment) 승인 처리
 */
export default function PaymentPage() {
  // 결제 API 는 모두 로그인이 필요하므로(SecurityConfig: /api/** authenticated)
  // 로그인 사용자를 확인한 뒤에야 위젯을 띄웁니다.
  const { user, loading: userLoading, unauthenticated, error: userError } = useRequireAuth('CLIENT');
  const searchParams = useSearchParams();
  // 공고 작성 화면에서 넘어온 값 — 결제 후 완료 화면까지 그대로 들고 가야 합니다.
  const postId = searchParams.get('postId') ?? '1';
  const paymentId = searchParams.get('paymentId') ?? '';
  const pay = searchParams.get('pay') ?? '0';
  // flow=extra : 동행 완료 후 차액을 내는 추가 결제 (공고 상세에서 들어옵니다).
  // 없으면 공고 등록 직후의 최초 결제입니다. 승인 후 어디로 돌아갈지가 달라집니다.
  const flow = searchParams.get('flow') ?? '';
  const isExtra = flow === 'extra';
  const [amount, setAmount] = useState<Amount>({
    currency: "KRW",
    value: Number(searchParams.get('amount') ?? 49000),
  });
  const [ready, setReady] = useState(false);
  const [widgets, setWidgets] = useState<TossPaymentsWidgets | null>(null);

  useEffect(() => {
    // customerKey 는 "이 고객"을 가리키는 고정 식별자여야 하므로 로그인 사용자가 정해진 뒤에 초기화합니다.
    if (user == null) {
      return;
    }

    async function fetchPaymentWidgets(customerKey: string) {
      try {
        // ------  SDK 초기화 ------
        // @docs https://docs.tosspayments.com/sdk/v2/js#토스페이먼츠-초기화
        const tossPayments = await loadTossPayments(clientKey);

        // 회원 결제
        // @docs https://docs.tosspayments.com/sdk/v2/js#tosspaymentswidgets
        const widgets = tossPayments.widgets({
          customerKey,
        });

        setWidgets(widgets);
      } catch (error) {
        console.error("Error fetching payment widget:", error);
      }
    }

    fetchPaymentWidgets(user.username);
  }, [user]);

  useEffect(() => {
    async function renderPaymentWidgets() {
      if (widgets == null) {
        return;
      }

      // ------  주문서의 결제 금액 설정 ------
      // TODO: 위젯의 결제금액을 결제하려는 금액으로 초기화하세요.
      // TODO: renderPaymentMethods, renderAgreement, requestPayment 보다 반드시 선행되어야 합니다.
      await widgets.setAmount(amount);

      // ------  결제 UI 렌더링 ------
      // @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrenderpaymentmethods
      await widgets.renderPaymentMethods({
        selector: "#payment-method",
        variantKey: "DEFAULT",
      });

      // ------  이용약관 UI 렌더링 ------
      // @docs https://docs.tosspayments.com/reference/widget-sdk#renderagreement선택자-옵션
      await widgets.renderAgreement({
        selector: "#agreement",
        variantKey: "AGREEMENT",
      });

      setReady(true);
    }

    renderPaymentWidgets();
  }, [widgets]);

  const updateAmount = async (amount: Amount) => {
    setAmount(amount);
    await widgets?.setAmount(amount);
  };

  if (userLoading) {
    return (
      <div className="wrapper">
        <div className="box_section">
          <p className="typography--p">결제 정보를 확인하고 있어요.</p>
        </div>
      </div>
    );
  }

  // 결제·승인 API 가 전부 로그인을 요구하므로, 결제창을 띄우기 전에 여기서 막습니다.
  // (결제수단을 고르고 카드 인증까지 한 뒤에 401 로 실패하는 상황을 피하기 위함입니다.)
  if (unauthenticated || user == null) {
    return (
      <div className="wrapper">
        <div className="box_section">
          <h2>로그인이 필요해요</h2>
          <p className="typography--p" style={{ marginTop: '20px', color: '#4e5968' }}>
            {userError ?? '결제를 진행하려면 로그인해 주세요.'}
          </p>
          <Link href="/login">
            <button type="button" className="button" style={{ marginTop: '30px' }}>
              로그인하기
            </button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="wrapper">
      <div className="box_section">
        {/* 결제 UI */}
        <div id="payment-method" />
        {/* 이용약관 UI */}
        <div id="agreement" />
        {/* 쿠폰 체크박스 — 추가 결제는 백엔드가 계산한 차액을 그대로 내야 해서 금액을 바꿀 수 없게 숨깁니다. */}
        {!isExtra && (
          <div style={{ paddingLeft: "24px" }}>
            <div className="checkable typography--p">
              <label
                htmlFor="coupon-box"
                className="checkable__label typography--regular"
              >
                <input
                  id="coupon-box"
                  className="checkable__input"
                  type="checkbox"
                  aria-checked="true"
                  disabled={!ready}
                  // ------  주문서의 결제 금액이 변경되었을 경우 결제 금액 업데이트 ------
                  // @docs https://docs.tosspayments.com/sdk/v2/js#widgetssetamount
                  onChange={async (event) => {
                    await updateAmount({
                      currency: amount.currency,
                      value: event.target.checked
                        ? amount.value - 5000
                        : amount.value + 5000,
                    });
                  }}
                />
                <span className="checkable__label-text">5,000원 쿠폰 적용</span>
              </label>
            </div>
          </div>
        )}

        {/* 결제하기 버튼 */}
        <button
          className="button"
          style={{ marginTop: "30px" }}
          disabled={!ready}
          // ------ '결제하기' 버튼 누르면 결제창 띄우기 ------
          // @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrequestpayment
          onClick={async () => {
            if (widgets == null) {
              return;
            }

            try {
              const orderId = generateRandomString();

              // 결제를 요청하기 전에 orderId, amount를 서버 세션에 저장
              // 결제 과정에서 악의적으로 결제 금액이 바뀌는 것을 확인하는 용도입니다.
              // 실제 프로덕트에선 제거액세스 토큰 추가
              // document.cookie = "accessToken=eyJhbGciOiJIUzUxMiJ9.eyJpZCI6MSwicm9sZSI6IkNMSUVOVCIsInVzZXJuYW1lIjoidGVzdFVzZXJuYW1lIiwiaWF0IjoxNzg5ODkwMjY4LCJleHAiOjE3ODk4OTA4Njh9.S6Ky5v23U8zg3dyou8C-s5kNEB0F7G_JIPGG0HsTr3hoMiYQ1xGJ23_M7TnSOWOadI2_9W9dmikUnRX4jy09cA; Path=/;"
              await fetchSaveAmount(orderId, amount.value);

              // 토스는 successUrl/failUrl 의 쿼리스트링을 그대로 보존해서 리다이렉트합니다.
              // 리다이렉트 이후 승인(paymentId)과 완료 화면(postId·pay)에 필요한 값을 여기 실어 보냅니다.
              const redirect = new URLSearchParams({ postId, paymentId, pay, flow });

              await widgets.requestPayment({
                orderId: orderId,
                orderName: isExtra ? "동행 추가 결제" : "동행 공고 결제",
                successUrl: `${window.location.origin}/client/posts/new/payment/success?${redirect}`,
                failUrl: `${window.location.origin}/client/posts/new/payment/fail?${redirect}`,
                customerEmail: user.email,
                customerName: user.name,
                customerMobilePhone: toDigits(user.phoneNum),
              });
            } catch (error) {
              // 에러 처리하기
              console.error(error);
            }
          }}
        >
          결제하기
        </button>
      </div>
    </div>
  );
}
