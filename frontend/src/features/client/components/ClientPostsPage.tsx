'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useState } from 'react';
import { SectionHeading } from '@/components/ui';
import { MyPageShell } from '@/features/mypage';
import { Pagination } from '@/features/post';
import { cn } from '@/lib/cn';
import { CLIENT_POSTS, SORT_OPTIONS, STATUS_TABS, filterClientPosts, type ClientPostSort } from '../model/posts';
import type { ClientPostStatus } from '../types';
import ClientPostCard from './ClientPostCard';

const PAGE_SIZE = 4;

/**
 * 내가 작성한 공고 — Figma 의뢰인_내가 작성한 공고 겸 메인페이지 562:14622
 *
 * ⚠️ 모의 데이터(model/posts.ts)를 화면에서 걸러 보여줍니다. API 연결은 아직 하지 않았습니다.
 */
export default function ClientPostsPage() {
  const [status, setStatus] = useState<'all' | ClientPostStatus>('all');
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState<ClientPostSort>('latest');
  const [page, setPage] = useState(0);

  const results = filterClientPosts(CLIENT_POSTS, status, keyword, sort);
  const pageCount = Math.max(1, Math.ceil(results.length / PAGE_SIZE));
  const visible = results.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  return (
    <MyPageShell role="client">
      <div className="flex w-full max-w-[1000px] flex-col items-center pt-8 lg:pt-[50px]">
        <SectionHeading
          title="내가 작성한 공고"
          description="작성한 병원 동행 요청을 확인하고, 지원자 관리 및 진행 상황을 확인할 수 있어요."
          className="mb-[30px] lg:mb-[25px]"
        />

        <div
          role="tablist"
          aria-label="공고 상태"
          className="flex w-full max-w-[735px] gap-2 overflow-x-auto rounded-[50px] bg-white ring-1 ring-line-soft lg:gap-[30px] lg:overflow-visible"
        >
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              type="button"
              role="tab"
              aria-selected={status === tab.value}
              onClick={() => {
                setStatus(tab.value);
                setPage(0);
              }}
              className={cn(
                'h-[35px] min-w-[80px] flex-1 rounded-[30px] px-4 text-base leading-[18px] font-semibold whitespace-nowrap transition-colors lg:w-[123px] lg:flex-none',
                status === tab.value ? 'bg-brand text-white' : 'text-brand hover:bg-line-soft',
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <form
          role="search"
          onSubmit={(event) => {
            event.preventDefault();
            setKeyword(keywordInput);
            setPage(0);
          }}
          className="mt-[50px] flex w-full max-w-[952px] flex-wrap items-center gap-3 rounded-[25px] border border-line bg-line-soft px-5 py-[25px] lg:h-[97.6px] lg:px-[30px] lg:py-0"
        >
          <label className="flex h-[47px] min-w-[220px] flex-1 items-center gap-4 rounded-[25px] border border-line bg-white px-[30px]">
            <Image src="/icons/search.svg" alt="" width={14} height={14} className="shrink-0" />
            <span className="sr-only">공고 검색</span>
            <input
              value={keywordInput}
              onChange={(event) => setKeywordInput(event.target.value)}
              placeholder="병원명, 지역, 공고 제목 등으로 검색해보세요."
              className="min-w-0 flex-1 bg-transparent text-sm text-brand outline-none placeholder:text-brand-muted"
            />
          </label>
          <button type="submit" className="h-[47px] w-[145px] rounded-[25px] bg-brand text-sm font-semibold text-white transition-colors hover:bg-brand-hover">
            검색하기
          </button>
          <div className="relative">
            <select
              aria-label="정렬"
              value={sort}
              onChange={(event) => setSort(event.target.value as ClientPostSort)}
              className="h-[47px] w-[136px] appearance-none rounded-[25px] border border-line-soft bg-white px-[19px] text-sm text-brand-muted shadow-card"
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <Image src="/icons/select-chevron.svg" alt="" width={10} height={5} className="pointer-events-none absolute top-1/2 right-[19px] -translate-y-1/2" />
          </div>
        </form>

        <div className="mt-[50px] flex w-full max-w-[907px] flex-col gap-2.5">
          <div className="flex flex-wrap items-end justify-between gap-3 pt-[5px]">
            <p className="text-xs leading-4 font-semibold text-brand">총 {results.length}개의 작성한 공고가 있습니다.</p>
            <Link
              href="/client/posts/new"
              className="flex h-[45px] items-center justify-center gap-[6.5px] rounded-[24px] bg-brand px-6 text-[13px] leading-[14.6px] font-semibold whitespace-nowrap text-white transition-colors hover:bg-brand-hover"
            >
              <Image src="/icons/client/plus.svg" alt="" width={9.5} height={9.5} className="size-2" />
              새 공고 작성하기
            </Link>
          </div>

          {visible.length > 0 ? (
            <ul className="grid gap-x-[13px] gap-y-4 lg:grid-cols-2">
              {visible.map((post) => (
                <li key={post.id} className="flex justify-center">
                  <ClientPostCard post={post} />
                </li>
              ))}
            </ul>
          ) : (
            <p className="py-20 text-center text-base font-semibold text-brand-muted">조건에 맞는 공고가 없습니다.</p>
          )}
        </div>

        {pageCount > 1 && (
          <div className="mt-[50px]">
            <Pagination page={page} pageCount={pageCount} onPageChange={setPage} />
          </div>
        )}
      </div>
    </MyPageShell>
  );
}
