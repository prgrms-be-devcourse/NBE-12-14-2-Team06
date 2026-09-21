'use client';

import Image from 'next/image';
import { useState } from 'react';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { MOCK_USER } from '@/lib/mockSession';
import { DEFAULT_FILTERS, PAY_OPTIONS, PERIOD_OPTIONS, POSTS, filterPosts, optionLabel } from '../model';
import { daysFromNow, formatMonthDay } from '../lib/date';
import type { LabelTone, PostBadge, PostFilters } from '../types';
import CardButton from './CardButton';
import Pagination from './Pagination';
import PostCard from './PostCard';
import PostSearchPanel from './PostSearchPanel';

const PAGE_SIZE = 6;

const BADGE: Record<PostBadge, { text: string; tone: LabelTone }> = {
  new: { text: '신규', tone: 'green' },
  closing: { text: '오늘 마감', tone: 'red' },
  open: { text: '모집 중', tone: 'blue' },
};

/**
 * 공고 목록 겸 메인 — Figma 동행 매니저_공고 목록 겸 메인페이지 188:1289
 *
 * ⚠️ 모의 데이터(model/posts.ts)를 화면에서 걸러 보여줍니다. API 연결은 아직 하지 않았습니다.
 */
export default function PostListPage() {
  const [filters, setFilters] = useState<PostFilters>(DEFAULT_FILTERS);
  const [keywordInput, setKeywordInput] = useState('');
  const [page, setPage] = useState(0);

  const updateFilters = (patch: Partial<PostFilters>) => {
    setFilters((prev) => ({ ...prev, ...patch }));
    setPage(0);
  };

  const reset = () => {
    setFilters(DEFAULT_FILTERS);
    setKeywordInput('');
    setPage(0);
  };

  const results = filterPosts(POSTS, filters);
  const pageCount = Math.max(1, Math.ceil(results.length / PAGE_SIZE));
  const visible = results.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  // 기본값과 다른 조건은 "선택된 필터" 칩으로 보여줍니다.
  const chips: { key: string; label: string; clear: Partial<PostFilters> }[] = [];
  if (filters.keyword) {
    chips.push({ key: 'keyword', label: `“${filters.keyword}”`, clear: { keyword: '' } });
  }
  if (filters.region !== 'all') {
    chips.push({ key: 'region', label: filters.region, clear: { region: 'all' } });
  }
  if (filters.period !== 'all') {
    chips.push({ key: 'period', label: optionLabel(PERIOD_OPTIONS, filters.period), clear: { period: 'all' } });
  }
  if (filters.pay !== 'all') {
    chips.push({ key: 'pay', label: optionLabel(PAY_OPTIONS, filters.pay), clear: { pay: 'all' } });
  }

  return (
    <AppShell user={MOCK_USER}>
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <SectionHeading
            title="병원 동행 공고를 찾아보세요!"
            description="지금 나에게 맞는 병원 동행 공고를 확인하고 지원할 수 있어요."
            className="mb-8 lg:mb-6"
            descriptionClassName="leading-6 lg:leading-[30px]"
          />

          <PostSearchPanel
            filters={filters}
            keywordInput={keywordInput}
            onKeywordInputChange={setKeywordInput}
            onSearch={() => updateFilters({ keyword: keywordInput })}
            onFilterChange={updateFilters}
            onReset={reset}
          />

          <div className="mt-[50px] flex w-full max-w-[1124px] flex-col gap-2.5">
            <div className="flex flex-wrap items-center gap-2">
              <p className="text-base leading-6 font-semibold text-brand">
                총 {results.length}개의 공고가 있습니다.
              </p>
              {chips.map((chip) => (
                <span
                  key={chip.key}
                  className="inline-flex items-center gap-2.5 rounded-full border border-[#e6e8ec] bg-[#f5f5f5] px-[17px] py-1.5 text-base leading-[22px] font-semibold text-brand"
                >
                  {chip.label}
                  <button
                    type="button"
                    aria-label={`${chip.label} 조건 해제`}
                    onClick={() => updateFilters(chip.clear)}
                    className="grid size-4 place-items-center"
                  >
                    <Image src="/icons/chip-close.svg" alt="" width={7.5} height={7.5} className="size-[6px]" />
                  </button>
                </span>
              ))}
            </div>

            {visible.length > 0 ? (
              <ul className="grid gap-x-4 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
                {visible.map((post) => (
                  <li key={post.id} className="flex justify-center">
                    <PostCard
                      title={post.title}
                      hospitalName={post.hospitalName}
                      location={`${post.region.slice(0, 2)} ${post.district}`}
                      label={BADGE[post.badge]}
                      postedAgo={post.postedAgo}
                      dateLabel={formatMonthDay(daysFromNow(post.startsInDays))}
                      timeLabel={post.startTime}
                      durationLabel={`약 ${post.hours}시간`}
                      payLabel={`시급 ${post.hourlyPay.toLocaleString()}원`}
                      description={post.description}
                    >
                      <CardButton href={`/posts/${post.id}`}>상세보기</CardButton>
                      {/* TODO: 지원 API(POST /api/v1/applications/{postId}) 연결 */}
                      <CardButton variant="solid">지원하기</CardButton>
                    </PostCard>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="py-20 text-center text-base font-semibold text-brand-muted">
                조건에 맞는 공고가 없습니다.
              </p>
            )}
          </div>

          <div className="mt-[50px]">
            <Pagination page={page} pageCount={pageCount} onPageChange={setPage} />
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
