'use client';

import Image from 'next/image';
import { useState, type FormEvent } from 'react';
import { SectionHeading } from '@/components/ui';
import { CardButton, PostCard } from '@/features/post';
import { cn } from '@/lib/cn';
import { APPLICATIONS, STATUS_INFO, STATUS_TABS, type StatusTab } from '../model';
import type { Application } from '../types';
import MyPageShell from './MyPageShell';

type Sort = 'latest' | 'oldest';

/** 상태별 카드 아래쪽 버튼 (Figma: 지원 취소 / 동행 보기 / 보고서 보기 / 지원불가) */
function ApplicationActions({ application }: { application: Application }) {
  const detail = <CardButton href={`/posts/${application.postId}`}>상세보기</CardButton>;

  switch (application.status) {
    case 'pending':
      // TODO: 지원 취소 API(PATCH /api/v1/applications/{applicationId}/cancel) 연결
      return (
        <>
          {detail}
          <CardButton variant="solid">지원 취소</CardButton>
        </>
      );
    case 'matched':
    case 'inProgress':
      return (
        <>
          {detail}
          <CardButton variant="solid">동행 보기</CardButton>
        </>
      );
    case 'completed':
      return (
        <>
          {detail}
          <CardButton variant="solid">보고서 보기</CardButton>
        </>
      );
    case 'rejected':
      return (
        <>
          {detail}
          <CardButton variant="disabled">지원불가</CardButton>
        </>
      );
  }
}

/**
 * 마이페이지 — 내가 신청한 공고 (Figma 525:5182 전체 · 529:7165 대기 중)
 *
 * ⚠️ 모의 데이터(model/applications.ts)를 보여줍니다. 신청 목록 API 가 아직 없습니다.
 */
export default function MyApplicationsPage() {
  const [tab, setTab] = useState<StatusTab>('all');
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState<Sort>('latest');

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setKeyword(keywordInput.trim());
  };

  const filtered = APPLICATIONS.filter((application) => {
    if (tab !== 'all' && application.status !== tab) return false;
    if (!keyword) return true;
    return [application.title, application.hospitalName, application.location].some((text) =>
      text.includes(keyword),
    );
  });
  const applications = sort === 'latest' ? filtered : [...filtered].reverse();
  const tabLabel = STATUS_TABS.find((item) => item.value === tab)?.label ?? '';

  return (
    <MyPageShell>
      <div className="max-w-[1000px] pt-8 lg:pt-[50px]">
        <SectionHeading
          title="내가 신청한 공고"
          description="지원한 공고와 현재 상태를 확인할 수 있습니다."
          className="mb-8 lg:mb-6"
        />

        {/* 상태 탭 */}
        <div
          role="tablist"
          aria-label="신청 상태"
          className="mx-auto flex w-full max-w-[734px] justify-between gap-2 rounded-[50px] border border-line-soft bg-white max-lg:overflow-x-auto lg:gap-[30px]"
        >
          {STATUS_TABS.map((item) => {
            const selected = item.value === tab;
            return (
              <button
                key={item.value}
                type="button"
                role="tab"
                aria-selected={selected}
                onClick={() => setTab(item.value)}
                className={cn(
                  'h-[35px] w-[123px] shrink-0 rounded-[30px] px-3 text-lg leading-[18px] font-semibold whitespace-nowrap transition-colors',
                  selected ? 'bg-brand text-white' : 'text-brand hover:bg-line-soft',
                )}
              >
                {item.label}
              </button>
            );
          })}
        </div>

        {/* 검색 + 정렬 (Figma는 공고 목록의 검색창을 0.8489배로 줄인 모양) */}
        <form
          onSubmit={handleSearch}
          className="mt-[50px] flex max-w-[952px] flex-col gap-3 rounded-[25px] border-[0.85px] border-line bg-line-soft px-5 py-6 sm:flex-row lg:mx-auto lg:px-[30px] lg:py-[25.5px]"
        >
          <label className="flex h-[46.7px] min-w-0 flex-1 items-center gap-[17px] rounded-[25.5px] border-[0.85px] border-line bg-white px-6 lg:px-[29.7px]">
            <Image src="/icons/search.svg" alt="" width={18} height={18} className="size-[13.6px] shrink-0" />
            <input
              type="search"
              value={keywordInput}
              onChange={(event) => setKeywordInput(event.target.value)}
              placeholder="병원명, 지역, 공고 제목 등으로 검색해보세요."
              aria-label="신청한 공고 검색"
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
              onChange={(event) => setSort(event.target.value as Sort)}
              aria-label="정렬"
              className="h-full w-full cursor-pointer appearance-none rounded-[25.5px] border-[0.85px] border-line-soft bg-white pr-9 pl-[18.7px] text-[15.3px] leading-4 font-semibold text-brand-muted shadow-card"
            >
              <option value="latest">최신 순</option>
              <option value="oldest">오래된 순</option>
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

        <p className="mt-[50px] mb-2.5 text-[13.6px] leading-[15px] font-semibold text-brand lg:ml-[23px]">
          총 {applications.length}개의 {tab === 'all' ? '신청' : tabLabel} 공고가 있습니다.
        </p>

        {applications.length > 0 ? (
          <ul className="grid gap-x-[13.8px] gap-y-[17.2px] sm:grid-cols-2 lg:ml-[23px] lg:grid-cols-[repeat(3,309px)]">
            {applications.map((application) => {
              const status = STATUS_INFO[application.status];
              return (
                <li key={application.id} className="flex justify-center">
                  <PostCard
                    compact
                    title={application.title}
                    hospitalName={application.hospitalName}
                    location={application.location}
                    label={{ text: status.label, tone: status.tone }}
                    dateLabel={application.dateLabel}
                    timeLabel={application.timeLabel}
                    durationLabel={application.durationLabel}
                    payLabel={application.payLabel}
                    description={application.description}
                  >
                    <ApplicationActions application={application} />
                  </PostCard>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="py-20 text-center text-base font-semibold text-brand-muted">
            해당하는 신청 공고가 없습니다.
          </p>
        )}
      </div>
    </MyPageShell>
  );
}
