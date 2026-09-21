'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useState } from 'react';
import { SectionHeading } from '@/components/ui';
import { cn } from '@/lib/cn';
import { SETTLEMENTS, SETTLEMENT_SUMMARY } from '../model';
import type { Settlement, SettlementStatus } from '../types';
import DateRangeFilter from './DateRangeFilter';
import EmptyState from './EmptyState';
import MyPageShell from './MyPageShell';
import StatBar from './StatBar';

type Tab = 'all' | SettlementStatus;

const TABS: { value: Tab; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'waiting', label: '정산 대기' },
  { value: 'done', label: '정산 완료' },
];

const BADGE: Record<SettlementStatus, string> = {
  waiting: 'bg-[#e6e8ec] text-footer',
  done: 'bg-[#d3d5da] text-[#62656d]',
};

const BUTTON = 'flex h-[37px] flex-1 items-center justify-center rounded-[17px] text-sm leading-[18px] font-semibold transition-colors';

function SettlementCard({ settlement }: { settlement: Settlement }) {
  const label = settlement.status === 'waiting' ? '정산 대기' : '정산 완료';
  return (
    <article className="flex min-h-[271px] flex-col gap-2.5 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
      <span className={cn('inline-flex h-6 w-fit items-center rounded-full px-4 text-sm leading-[18px] font-semibold', BADGE[settlement.status])}>
        {label}
      </span>
      <div className="flex items-center gap-5">
        <Image src="/icons/image-placeholder.svg" alt="" width={60} height={60} className="size-[60px] shrink-0" />
        <div className="min-w-0">
          <p className="truncate text-base leading-4 font-semibold text-brand">{settlement.title}</p>
          <p className="mt-2.5 flex items-center gap-2 text-xs leading-4 font-medium text-brand">
            {settlement.hospitalName}
            <span aria-hidden="true" className="h-3 w-px bg-[#e6e8ec]" />
            {settlement.location}
          </p>
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-x-6 gap-y-2 py-2 text-xs leading-4 text-brand">
        {[
          ['동행일', settlement.dateLabel],
          ['소요 시간', settlement.durationLabel],
          ['정산 예정일', settlement.dueLabel],
          ['정산금액', `${settlement.amount.toLocaleString()}원`],
        ].map(([term, value]) => (
          <div key={term} className="flex gap-3">
            <dt className="w-[72px] shrink-0 font-semibold">{term}</dt>
            <dd className="font-medium">{value}</dd>
          </div>
        ))}
      </dl>
      <div className="mt-auto flex gap-[10px]">
        <Link href={`/posts/${settlement.postId}`} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>
          상세보기
        </Link>
        {settlement.status === 'waiting' ? (
          // TODO: 정산 요청 API 연결
          <button type="button" className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>정산 요청</button>
        ) : (
          <Link href={`/escort/${settlement.applicationId}`} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
            동행 보기
          </Link>
        )}
      </div>
    </article>
  );
}

/**
 * 마이페이지 — 정산 목록 (Figma 522:3149 전체 · 525:3977 대기 · 525:4251 완료 · 562:13878 빈 상태)
 *
 * ⚠️ 모의 데이터(model/settlements.ts)를 보여줍니다. 정산 API 연결 전입니다.
 */
export default function MySettlementsPage() {
  const [tab, setTab] = useState<Tab>('all');
  const [fromInput, setFromInput] = useState('');
  const [toInput, setToInput] = useState('');
  const [range, setRange] = useState({ from: '', to: '' });

  const settlements = SETTLEMENTS.filter(
    (item) =>
      (tab === 'all' || item.status === tab) &&
      (!range.from || item.dueDate >= range.from) &&
      (!range.to || item.dueDate <= range.to),
  );

  return (
    <MyPageShell>
      <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
        <SectionHeading
          title="정산 목록"
          description="동행을 마친 공고에 대한 정산 상태를 확인할 수 있습니다."
          className="mb-0"
        />

        <div
          role="tablist"
          aria-label="정산 상태"
          className="flex h-[35px] w-full max-w-[433px] items-center justify-center rounded-[50px] border border-line-soft bg-white px-[24px]"
        >
          {TABS.map((item) => {
            const selected = item.value === tab;
            return (
              <button
                key={item.value}
                type="button"
                role="tab"
                aria-selected={selected}
                onClick={() => setTab(item.value)}
                className={cn(
                  'h-[26px] flex-1 rounded-[30px] text-base leading-4 font-semibold whitespace-nowrap transition-colors',
                  selected ? 'bg-brand text-white' : 'text-brand hover:bg-line-soft',
                )}
              >
                {item.label}
              </button>
            );
          })}
        </div>

        <StatBar
          items={[
            { icon: '/icons/mypage/stat-amount.svg', label: '정산 금액', value: `${SETTLEMENT_SUMMARY.total.toLocaleString()}원` },
            { icon: '/icons/mypage/stat-pending.svg', label: '정산 대기', value: `${SETTLEMENT_SUMMARY.waiting}건` },
            { icon: '/icons/mypage/stat-done.svg', label: '정산 완료', value: `${SETTLEMENT_SUMMARY.done}건` },
          ]}
        />

        <DateRangeFilter
          from={fromInput}
          to={toInput}
          onFromChange={setFromInput}
          onToChange={setToInput}
          onSearch={() => setRange({ from: fromInput, to: toInput })}
        />

        {settlements.length > 0 ? (
          <ul className="grid w-full max-w-[910px] gap-4 sm:grid-cols-2">
            {settlements.map((settlement) => (
              <li key={settlement.id}>
                <SettlementCard settlement={settlement} />
              </li>
            ))}
          </ul>
        ) : (
          <EmptyState
            title="아직 정산된 공고가 없습니다."
            description="관심 있는 병원 동행 공고를 찾아 지원해보세요."
            action={{ label: '공고 찾기', href: '/posts' }}
          />
        )}
      </div>
    </MyPageShell>
  );
}
