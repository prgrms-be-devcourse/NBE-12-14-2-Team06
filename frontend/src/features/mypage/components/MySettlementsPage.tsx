'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { cn } from '@/lib/cn';
import { fetchSettlements, requestSettlement } from '../api';
import type { SettlementDto, SettlementStatus } from '../types';
import DateRangeFilter from './DateRangeFilter';
import EmptyState from './EmptyState';
import MyPageShell from './MyPageShell';
import StatBar from './StatBar';

type Tab = 'all' | SettlementStatus;

const TABS: { value: Tab; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'PENDING', label: '정산 대기' },
  { value: 'COMPLETED', label: '정산 완료' },
];

const BADGE: Record<SettlementStatus, { label: string; className: string }> = {
  PENDING: { label: '정산 대기', className: 'bg-[#e6e8ec] text-footer' },
  COMPLETED: { label: '정산 완료', className: 'bg-[#d3d5da] text-[#62656d]' },
  FAILED: { label: '정산 실패', className: 'bg-[#ffe3e3] text-[#b91d1d]' },
};

const BUTTON = 'flex h-[37px] flex-1 items-center justify-center rounded-[17px] text-sm leading-[18px] font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60';

/** "2026-09-22T09:00:00" → "2026.09.22" */
function formatDate(value: string): string {
  return value.slice(0, 10).replaceAll('-', '.');
}

/** date input(YYYY-MM-DD) → 그 날 00:00:00 / 23:59:59 */
function toDayStart(date: string): string {
  return `${date}T00:00:00`;
}
function toDayEnd(date: string): string {
  return `${date}T23:59:59`;
}

function SettlementCard({ settlement, onRequested }: { settlement: SettlementDto; onRequested: (updated: SettlementDto) => void }) {
  const [requesting, setRequesting] = useState(false);
  const [error, setError] = useState('');
  const badge = BADGE[settlement.status];

  const handleRequest = async () => {
    setRequesting(true);
    setError('');
    try {
      // TODO: 정산 요청 API(POST /api/v1/settlements/{settlementId})는 정산 대기 상태에서만 됩니다.
      await requestSettlement(settlement.id);
      onRequested({ ...settlement, status: 'COMPLETED', settledAt: new Date().toISOString() });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '정산 요청에 실패했습니다.');
    } finally {
      setRequesting(false);
    }
  };

  const rows: { term: string; value: string; tone?: string }[] = [
    { term: '동행일', value: formatDate(settlement.post.escortStartAt) },
    { term: '소요 시간', value: `약 ${settlement.post.escortHours}시간` },
    { term: '정산 완료일', value: settlement.settledAt ? formatDate(settlement.settledAt) : '-' },
    { term: '정산금액', value: `${settlement.payoutAmount.toLocaleString()}원` },
  ];

  // 노쇼 패널티는 차감이 있을 때만 보여줍니다. payoutAmount 는 이미 차감된 금액입니다.
  if (settlement.penaltyAmount > 0) {
    rows.push({
      term: '패널티',
      value: `-${settlement.penaltyAmount.toLocaleString()}원`,
      tone: 'text-[#b91d1d]',
    });
  }

  return (
    <article className="flex min-h-[271px] flex-col gap-2.5 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
      <span className={cn('inline-flex h-6 w-fit items-center rounded-full px-4 text-sm leading-[18px] font-semibold', badge.className)}>
        {badge.label}
      </span>
      <div className="flex items-center gap-5">
        <Image src="/icons/image-placeholder.svg" alt="" width={60} height={60} className="size-[60px] shrink-0" />
        <div className="min-w-0">
          <p className="truncate text-base leading-4 font-semibold text-brand">{settlement.post.title}</p>
          <p className="mt-2.5 flex items-center gap-2 text-xs leading-4 font-medium text-brand">
            {settlement.post.hospitalName}
            <span aria-hidden="true" className="h-3 w-px bg-[#e6e8ec]" />
            {settlement.post.region}
          </p>
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-x-6 gap-y-2 py-2 text-xs leading-4 text-brand">
        {rows.map(({ term, value, tone }) => (
          <div key={term} className="flex gap-3">
            <dt className="w-[72px] shrink-0 font-semibold">{term}</dt>
            <dd className={cn('font-medium', tone)}>{value}</dd>
          </div>
        ))}
      </dl>
      {error && <p role="alert" className="text-xs font-medium text-[#b91d1d]">{error}</p>}
      <div className="mt-auto flex gap-[10px]">
        <Link href={`/posts/${settlement.post.id}`} className={cn(BUTTON, 'border border-line bg-white text-brand hover:bg-line-soft')}>
          상세보기
        </Link>
        {settlement.status === 'PENDING' && (
          <button type="button" onClick={handleRequest} disabled={requesting} className={cn(BUTTON, 'bg-brand text-white hover:bg-brand-hover')}>
            {requesting ? '요청 중…' : '정산 요청'}
          </button>
        )}
      </div>
    </article>
  );
}

/**
 * 마이페이지 — 정산 목록 (Figma 522:3149 전체 · 525:3977 대기 · 525:4251 완료 · 562:13878 빈 상태)
 *
 * GET /api/v1/settlements 로 조회하고, 정산 요청은 POST /api/v1/settlements/{settlementId} 로 보냅니다.
 * ⚠️ "동행 보기" 버튼은 이 응답에 신청(application) 번호가 없어서 뺐습니다.
 */
export default function MySettlementsPage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const [tab, setTab] = useState<Tab>('all');
  const [fromInput, setFromInput] = useState('');
  const [toInput, setToInput] = useState('');
  const [range, setRange] = useState({ from: '', to: '' });

  const requestKey = JSON.stringify(range);
  const [result, setResult] = useState<{ key: string; settlements?: SettlementDto[]; error?: string }>();

  useEffect(() => {
    let ignore = false;
    fetchSettlements({
      startDate: range.from ? toDayStart(range.from) : undefined,
      endDate: range.to ? toDayEnd(range.to) : undefined,
      size: 100,
    })
      .then(({ settlements }) => !ignore && setResult({ key: requestKey, settlements }))
      .catch((error: Error) => !ignore && setResult({ key: requestKey, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [range, requestKey]);

  const loading = result?.key !== requestKey;
  const all = (!loading && result?.settlements) || [];
  const errorMessage = !loading ? result?.error : undefined;
  const settlements = tab === 'all' ? all : all.filter((item) => item.status === tab);

  const updateSettlement = (updated: SettlementDto) => {
    setResult((prev) => prev && { ...prev, settlements: prev.settlements?.map((item) => (item.id === updated.id ? updated : item)) });
  };

  const totalAmount = all.reduce((sum, item) => sum + item.payoutAmount, 0);
  const pendingCount = all.filter((item) => item.status === 'PENDING').length;
  const completedCount = all.filter((item) => item.status === 'COMPLETED').length;

  if (authLoading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

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
            className="flex h-[35px] w-full max-w-[360px] items-center rounded-[999px] border border-line-soft bg-white"
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
                        'h-[35px] flex-1 rounded-[999px] text-base font-semibold whitespace-nowrap transition-colors',
                        selected
                            ? 'bg-brand text-white'
                            : 'bg-transparent text-brand hover:bg-line-soft'
                    )}
                >
                  {item.label}
                </button>
            );
          })}
        </div>

        <StatBar
          items={[
            { icon: '/icons/mypage/stat-amount.svg', label: '정산 금액', value: `${totalAmount.toLocaleString()}원` },
            { icon: '/icons/mypage/stat-pending.svg', label: '정산 대기', value: `${pendingCount}건` },
            { icon: '/icons/mypage/stat-done.svg', label: '정산 완료', value: `${completedCount}건` },
          ]}
        />

        <div className="w-full max-w-[910px]">
          <DateRangeFilter
              from={fromInput}
              to={toInput}
              onFromChange={setFromInput}
              onToChange={setToInput}
              onSearch={() => setRange({ from: fromInput, to: toInput })}
          />
        </div>

        {loading ? (
          <p className="py-10 text-center text-base font-semibold text-brand-muted">불러오는 중입니다.</p>
        ) : errorMessage ? (
          <p role="alert" className="py-10 text-center text-base font-semibold text-brand-muted">불러오지 못했습니다. ({errorMessage})</p>
        ) : settlements.length > 0 ? (
          <ul className="grid w-full max-w-[910px] gap-4 sm:grid-cols-2">
            {settlements.map((settlement) => (
              <li key={settlement.id}>
                <SettlementCard settlement={settlement} onRequested={updateSettlement} />
              </li>
            ))}
          </ul>
        ) : (
            <div className="w-full max-w-[910px]">
              <EmptyState
                  title="아직 정산된 공고가 없습니다."
                  description="관심 있는 병원 동행 공고를 찾아 지원해보세요."
                  action={{ label: '공고 찾기', href: '/posts' }}
              />
            </div>
        )}
      </div>
    </MyPageShell>
  );
}
