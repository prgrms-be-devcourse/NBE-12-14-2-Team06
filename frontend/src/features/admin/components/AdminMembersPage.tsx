'use client';

import Image from 'next/image';
import { useState, type FormEvent } from 'react';
import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { Pagination } from '@/features/post';
import { cn } from '@/lib/cn';
import {
  MEMBERS,
  ROLE_TABS,
  SORT_OPTIONS,
  filterMembers,
  type MemberSort,
  type RoleTab,
} from '../model';
import AdminShell from './AdminShell';
import MemberTable from './MemberTable';

/** Figma 화면에 한 페이지당 6줄이 보입니다. */
const PAGE_SIZE = 6;

/**
 * 관리자 — 회원 관리 (Figma 571:19168 관리자_회원관리)
 *
 * ⚠️ 모의 데이터(model/members.ts)를 보여줍니다. 회원 목록 API 연결 전입니다.
 */
export default function AdminMembersPage() {
  const { loading, user } = useRequireAuth('ADMIN');
  const [tab, setTab] = useState<RoleTab>('all');
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState<MemberSort>('latest');
  const [page, setPage] = useState(0);

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setKeyword(keywordInput.trim());
    setPage(0);
  };

  if (loading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  const members = filterMembers(MEMBERS, { tab, keyword, sort });
  const pageCount = Math.max(1, Math.ceil(members.length / PAGE_SIZE));
  // 조건이 바뀌어 페이지 수가 줄면 마지막 페이지를 보여줍니다.
  const currentPage = Math.min(page, pageCount - 1);
  const visible = members.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  return (
    <AdminShell>
      <div className="mx-auto flex max-w-[1000px] flex-col items-center gap-8 pt-8 lg:gap-[50px] lg:pt-[50px]">
        <SectionHeading
          title="회원 관리"
          description="가입된 회원을 조회하고 상세 정보를 확인할 수 있습니다."
          className=""
        />

        {/* 역할 탭 */}
        <div
          role="tablist"
          aria-label="회원 역할"
          className="flex w-full max-w-[360px] rounded-[50px] border border-line-soft bg-white max-lg:overflow-x-auto"
        >
          {ROLE_TABS.map((item) => {
            const selected = item.value === tab;
            return (
              <button
                key={item.value}
                type="button"
                role="tab"
                aria-selected={selected}
                onClick={() => {
                  setTab(item.value);
                  setPage(0);
                }}
                className={cn(
                    'h-[35px] flex-1 rounded-[30px] px-3 text-base leading-[18px] font-semibold whitespace-nowrap transition-colors',
                    selected ? 'bg-brand text-white' : 'text-brand hover:bg-line-soft',
                )}
              >
                {item.label}
              </button>
            );
          })}
        </div>

        {/* 검색 + 정렬 (Figma 571:19201) */}
        <form
          onSubmit={handleSearch}
          className="flex w-full max-w-[952px] flex-col gap-3 rounded-[25px] border-[0.85px] border-line bg-line-soft px-5 py-6 sm:flex-row lg:px-[30px] lg:py-[25.5px]"
        >
          <label className="flex h-[46.7px] min-w-0 flex-1 items-center gap-[17px] rounded-[25.5px] border-[0.85px] border-line bg-white px-6 lg:px-[29.7px]">
            <Image src="/icons/search.svg" alt="" width={18} height={18} className="size-[13.6px] shrink-0" />
            <input
              type="search"
              value={keywordInput}
              onChange={(event) => setKeywordInput(event.target.value)}
              placeholder="이름, 회원 ID, 이메일 등으로 검색해보세요."
              aria-label="회원 검색"
              className="min-w-0 flex-1 bg-transparent text-[13.6px] leading-4 font-semibold text-brand placeholder:text-brand-muted focus:outline-none"
            />
          </label>
          <button
            type="submit"
            className="h-[46.7px] rounded-[25.5px] bg-brand px-6 text-[13.6px] leading-4 font-semibold text-white transition-colors hover:bg-brand-hover sm:w-[145px]"
          >
            검색하기
          </button>
          <div className="relative h-[46.7px] sm:w-[136px]">
            <select
              value={sort}
              onChange={(event) => {
                setSort(event.target.value as MemberSort);
                setPage(0);
              }}
              aria-label="정렬"
              className="h-full w-full cursor-pointer appearance-none rounded-[25.5px] border-[0.85px] border-line-soft bg-white pr-9 pl-[18.7px] text-[15.3px] leading-4 font-semibold text-brand-muted shadow-card"
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <Image
              src="/icons/select-chevron.svg"
              alt=""
              width={11.09}
              height={6.14}
              className="pointer-events-none absolute top-1/2 right-[18px] -translate-y-1/2"
            />
          </div>
        </form>

        {/* 목록 */}
        <div className="flex w-full max-w-[952px] flex-col gap-2.5">
          <p
            aria-live="polite"
            className="text-[13px] leading-[19px] font-semibold text-brand lg:ml-[29.7px]"
          >
            총 {members.length}명의 회원이 있습니다.
          </p>

          {visible.length > 0 ? (
            <MemberTable members={visible} />
          ) : (
            <p className="rounded-[25.464px] border-[0.849px] border-line bg-line-soft px-[29.7px] py-[50px] text-center text-base font-semibold text-brand-muted">
              조건에 맞는 회원이 없습니다.
            </p>
          )}
        </div>

        <Pagination page={currentPage} pageCount={pageCount} onPageChange={setPage} />
      </div>
    </AdminShell>
  );
}
