'use client';

import Image from 'next/image';
import type { FormEvent } from 'react';

type Props = {
  from: string;
  to: string;
  onFromChange: (value: string) => void;
  onToChange: (value: string) => void;
  onSearch: () => void;
};

const DATE_INPUT = 'min-w-0 flex-1 bg-transparent text-sm leading-[21px] font-medium text-brand focus:outline-none';

/** 기간 검색 (Figma "Start Date → End Date" + 검색하기) */
export default function DateRangeFilter({ from, to, onFromChange, onToChange, onSearch }: Props) {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch();
  };

  return (
    <form onSubmit={handleSubmit} className="flex w-full max-w-[910px] flex-wrap items-center gap-5">
      <div className="flex h-[35px] w-full max-w-[315px] items-center gap-2 rounded-[30px] border border-line bg-white px-[15px]">
        <input type="date" aria-label="시작일" value={from} max={to || undefined} onChange={(event) => onFromChange(event.target.value)} className={DATE_INPUT} />
        <Image src="/icons/mypage/range-arrow.svg" alt="~" width={6.48} height={5.67} className="shrink-0" />
        <input type="date" aria-label="종료일" value={to} min={from || undefined} onChange={(event) => onToChange(event.target.value)} className={DATE_INPUT} />
        <Image src="/icons/mypage/calendar.svg" alt="" width={15} height={15} className="shrink-0" />
      </div>
      <button
        type="submit"
        className="h-[35px] w-[130px] rounded-[30px] bg-brand text-sm leading-[21px] font-semibold text-white transition-colors hover:bg-brand-hover"
      >
        검색하기
      </button>
    </form>
  );
}
