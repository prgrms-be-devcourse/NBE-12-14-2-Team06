import Image from 'next/image';
import { Fragment } from 'react';

type Item = { icon: string; label: string; value: string };

/** 위쪽 요약 카드 (Figma "정산 금액 · 정산 대기 · 정산 완료", "리뷰 · 리뷰 평점") */
export default function StatBar({ items }: { items: Item[] }) {
  return (
    <ul className="flex w-full max-w-[910px] flex-wrap items-center justify-center gap-x-[55px] gap-y-5 rounded-[30px] border border-line-soft bg-white px-8 py-6 shadow-card lg:min-h-[107px] lg:flex-nowrap lg:gap-x-[35px]">
      {items.map((item, index) => (
        <Fragment key={item.label}>
          {index > 0 && <li aria-hidden="true" className="hidden h-[70px] w-px -my-2 bg-[#e6e8ec] opacity-50 lg:block" />}
          <li className="flex items-center gap-5">
            <Image src={item.icon} alt="" width={50} height={50} className="size-[50px] shrink-0" />
            <div className="flex flex-col gap-3.5 text-brand">
              <span className="text-base leading-4 font-semibold">{item.label}</span>
              <span className="text-2xl leading-6 font-bold">{item.value}</span>
            </div>
          </li>
        </Fragment>
      ))}
    </ul>
  );
}
