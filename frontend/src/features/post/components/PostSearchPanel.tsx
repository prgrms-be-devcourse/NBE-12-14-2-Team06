'use client';

import Image from 'next/image';
import type { FormEvent } from 'react';
import { cn } from '@/lib/cn';
import { PAY_OPTIONS, PERIOD_OPTIONS, REGION_OPTIONS, SORT_OPTIONS } from '../model';
import type { PostFilters } from '../types';

type Option = { value: string; label: string };

type FilterSelectProps = {
  id: string;
  label: string;
  value: string;
  options: Option[];
  onChange: (value: string) => void;
};

/**
 * 라벨이 위에 붙은 선택 상자 (Figma 필터 선택 박스 160×55)
 * 폭은 190px 으로 넓혔습니다 — 160px 에서는 좌우 여백(22 + 40)을 빼면 글자 자리가 98px 뿐이라
 * "10,000원 이상" 같은 가장 긴 선택지가 잘렸습니다.
 */
function FilterSelect({ id, label, value, options, onChange }: FilterSelectProps) {
  const changed = value !== options[0].value;
  return (
    <div className="flex w-[190px] flex-col gap-2">
      <label htmlFor={id} className="text-base leading-5 font-semibold text-brand">
        {label}
      </label>
      <div className="relative h-[55px]">
        <select
          id={id}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          className={cn(
            'h-full w-full cursor-pointer appearance-none rounded-[20px] border border-line-soft bg-white pr-10 pl-[22px] text-lg leading-[18px] font-semibold shadow-card',
            changed ? 'text-brand' : 'text-brand-muted',
          )}
        >
          {options.map((option) => (
            <option key={option.value} value={option.value} className="text-brand">
              {option.label}
            </option>
          ))}
        </select>
        <Image
          src="/icons/select-chevron.svg"
          alt=""
          width={13.0667}
          height={7.23333}
          className="pointer-events-none absolute top-1/2 right-[22px] -translate-y-1/2"
        />
      </div>
    </div>
  );
}

type Props = {
  filters: PostFilters;
  keywordInput: string;
  onKeywordInputChange: (value: string) => void;
  onSearch: () => void;
  onFilterChange: (patch: Partial<PostFilters>) => void;
  onReset: () => void;
};

/** Figma 537:3025 — 검색창 + 필터 5개 + 초기화 */
export default function PostSearchPanel({
  filters,
  keywordInput,
  onKeywordInputChange,
  onSearch,
  onFilterChange,
  onReset,
}: Props) {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch();
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex w-full max-w-[1122px] flex-col justify-center gap-[30px] rounded-[30px] border border-line bg-line-soft px-5 py-[30px] lg:min-h-[247px] lg:px-[35px]"
    >
      <div className="mx-auto flex w-full max-w-[1049px] flex-col gap-[15px] sm:flex-row">
        <label className="flex h-[55px] min-w-0 flex-1 items-center gap-5 rounded-[30px] border border-line bg-white px-6 lg:px-[35px]">
          <Image src="/icons/search.svg" alt="" width={18} height={18} className="size-4 shrink-0" />
          <input
            type="search"
            value={keywordInput}
            onChange={(event) => onKeywordInputChange(event.target.value)}
            placeholder="병원명, 지역, 공고 제목 등으로 검색해보세요."
            aria-label="공고 검색"
            className="min-w-0 flex-1 bg-transparent text-base leading-[18px] font-semibold text-brand placeholder:text-brand-muted focus:outline-none"
          />
        </label>
        <button
          type="submit"
          className="h-[55px] rounded-[30px] bg-brand px-[30px] text-base leading-[18px] font-semibold text-white transition-colors hover:bg-brand-hover sm:w-[171px]"
        >
          검색하기
        </button>
      </div>

      {/* 검색창과 같은 폭(1049px) 안에서 필터 4개 + 초기화를 양끝에 붙이고 사이 간격을 균일하게 나눕니다. */}
      <div className="mx-auto flex w-full max-w-[1049px] flex-wrap justify-between gap-x-[17px] gap-y-4">
        <FilterSelect
          id="post-region"
          label="지역"
          value={filters.region}
          options={REGION_OPTIONS}
          onChange={(region) => onFilterChange({ region })}
        />
        <FilterSelect
          id="post-period"
          label="날짜"
          value={filters.period}
          options={PERIOD_OPTIONS}
          onChange={(period) => onFilterChange({ period: period as PostFilters['period'] })}
        />
        <FilterSelect
          id="post-pay"
          label="시급"
          value={filters.pay}
          options={PAY_OPTIONS}
          onChange={(pay) => onFilterChange({ pay: pay as PostFilters['pay'] })}
        />
        <FilterSelect
          id="post-sort"
          label="정렬"
          value={filters.sort}
          options={SORT_OPTIONS}
          onChange={(sort) => onFilterChange({ sort: sort as PostFilters['sort'] })}
        />
        <div className="flex flex-col gap-2">
          <span aria-hidden="true" className="h-5" />
          <button
            type="button"
            onClick={onReset}
            // 크기(171×55)와 모서리는 위의 "검색하기" 버튼과 같게 맞췄습니다.
            className="flex h-[55px] w-[171px] items-center justify-center gap-[15px] rounded-[30px] border border-line-soft bg-white text-lg leading-[18px] font-semibold text-brand shadow-card transition-colors hover:bg-line-soft"
          >
            <Image src="/icons/reset.svg" alt="" width={14.75} height={14.75} className="size-[13px]" />
            초기화
          </button>
        </div>
      </div>
    </form>
  );
}
