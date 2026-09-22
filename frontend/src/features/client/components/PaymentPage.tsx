'use client';

import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';

const METHODS = [
  { id: 'quick', label: '퀵계좌이체', badge: '3% 즉시 할인' },
  { id: 'card', label: '신용·체크카드' },
  { id: 'tosspay', label: '토스페이' },
  { id: 'payco', label: 'PAYCO' },
  { id: 'kakaopay', label: '카카오페이' },
  { id: 'naverpay', label: '네이버페이' },
];

const COUPON = 5000;
const METHOD_BASE = 'relative flex h-[62px] items-center justify-center border text-lg text-[#333d4b] transition-colors';

/**
 * 결제 방법 — Figma 의뢰인_결제 459:3067
 *
 * ⚠️ 디자인이 결제 위젯(HTML) 화면이라 같은 구성의 모양만 그렸습니다. 실제로는 결제되지 않습니다.
 * TODO: 토스페이먼츠 결제 위젯 SDK 로 교체하고, 결제 성공 후 서버에서 결제(Payment) 승인 처리
 */
export default function PaymentPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const amount = Number(searchParams.get('amount') ?? 49000);
  const pay = Number(searchParams.get('pay') ?? 14000);
  // 공고 작성 화면에서 실제로 등록한 공고 id (없으면 이전처럼 1번으로 대체)
  const postId = searchParams.get('postId') ?? '1';

  const [method, setMethod] = useState('quick');
  const [agreed, setAgreed] = useState(true);
  const [coupon, setCoupon] = useState(false);
  const [bannerOpen, setBannerOpen] = useState(true);

  const total = Math.max(0, amount - (coupon ? COUPON : 0));

  const handlePay = () => {
    // TODO: 토스페이먼츠 결제 위젯 SDK 로 교체하고, 결제 성공 후 서버에서 결제(Payment) 승인 처리
    const query = new URLSearchParams({ postId, pay: String(pay), amount: String(total) });
    router.push(`/client/posts/complete?${query.toString()}`);
  };

  return (
    <AppShell user={MOCK_CLIENT}>
      <section className="bg-white px-4 pt-[50px] pb-[90px]">
        <div className="mx-auto w-full max-w-[727px] text-[#191f28]">
          {bannerOpen && (
            <div className="flex h-[38px] items-center gap-2 rounded-lg bg-[#fdf5e3] px-3 text-[15px] font-medium text-[#d9580f]">
              <span aria-hidden="true" className="grid size-4 shrink-0 place-items-center rounded-full bg-[#f5a623] text-[11px] leading-none font-bold text-white">!</span>
              <span className="flex-1">테스트 환경이에요. 실제로 결제되지 않아요.</span>
              <button type="button" aria-label="안내 닫기" onClick={() => setBannerOpen(false)} className="grid size-5 place-items-center text-lg leading-none">
                ×
              </button>
            </div>
          )}

          <h1 className="mt-14 mb-3 text-2xl leading-8 font-bold">결제 방법</h1>
          <p className="mb-3 text-base font-medium text-[#4e5968]">
            결제 금액 <strong className="font-bold text-[#191f28]">{total.toLocaleString()}원</strong>
          </p>

          <div role="radiogroup" aria-label="결제 방법" className="grid grid-cols-1 gap-[9px] sm:grid-cols-3">
            {METHODS.map((item, index) => (
              <button
                key={item.id}
                type="button"
                role="radio"
                aria-checked={method === item.id}
                onClick={() => setMethod(item.id)}
                className={cn(
                  METHOD_BASE,
                  index === 0 && 'sm:col-span-3',
                  method === item.id ? 'border-[#191f28]' : 'border-[#e5e8eb] hover:border-[#b0b8c1]',
                  index === 0 ? 'font-bold' : 'font-medium',
                )}
              >
                {item.badge && (
                  <span className="absolute -top-2.5 -left-1 rounded-full bg-[#3182f6] px-2 py-0.5 text-[11px] leading-none font-bold text-white">{item.badge}</span>
                )}
                {item.label}
              </button>
            ))}
          </div>

          <p className="mt-[18px] flex h-[54px] items-center rounded-xl bg-[#f2f4f6] px-5 text-[17px] font-medium text-[#333d4b]">
            신한카드 최대 3개월 무이자 할부
          </p>

          <div className="mt-6 flex flex-col gap-2 text-[15px] text-[#4e5968]">
            <p>
              <strong className="font-bold text-[#191f28]">퀵계좌이체</strong> · 1000원 이상 결제 시 3% 즉시 할인
            </p>
            <p className="flex items-center gap-1">
              신용카드 무이자 할부 안내
              <Image src="/icons/chevron-right.svg" alt="" width={16} height={16} className="size-4" />
            </p>
          </div>

          <div className="mt-[52px] flex flex-col gap-[30px] text-[17px]">
            <label className="flex cursor-pointer items-center gap-2.5">
              <input type="checkbox" checked={agreed} onChange={(event) => setAgreed(event.target.checked)} className="size-[26px] shrink-0 rounded-md accent-[#3182f6]" />
              [필수] 결제 서비스 이용 약관, 개인정보 처리 동의
            </label>
            <label className="flex cursor-pointer items-center gap-2.5">
              <input type="checkbox" checked={coupon} onChange={(event) => setCoupon(event.target.checked)} className="size-[26px] shrink-0 rounded-md accent-[#3182f6]" />
              {COUPON.toLocaleString()}원 쿠폰 적용
            </label>
          </div>

          <button
            type="button"
            disabled={!agreed}
            onClick={handlePay}
            className="mx-auto mt-[26px] flex h-[45px] w-full max-w-[280px] items-center justify-center rounded-lg bg-[#3182f6] text-base font-medium text-white transition-colors hover:bg-[#1b64da] disabled:cursor-not-allowed disabled:bg-[#b0b8c1]"
          >
            결제하기
          </button>
        </div>
      </section>
    </AppShell>
  );
}
