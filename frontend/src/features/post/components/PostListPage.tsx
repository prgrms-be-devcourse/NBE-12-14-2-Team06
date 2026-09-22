'use client';

import Image from 'next/image';
import { useEffect, useState } from 'react';
import { Container, SectionHeading } from '@/components/ui';
import { applyToPost } from '@/features/application';
import { cn } from '@/lib/cn';
import { fetchPosts, type PostPage } from '../api';
import { DEFAULT_FILTERS, PAY_OPTIONS, PERIOD_OPTIONS, optionLabel } from '../model';
import { daysFromNow, formatMonthDay } from '../lib/date';
import type { LabelTone, PostBadge, PostFilters } from '../types';
import CardButton from './CardButton';
import Pagination from './Pagination';
import PostCard from './PostCard';
import PostSearchPanel from './PostSearchPanel';

/** 한 번에 서버에서 가져오는 공고 수 (= 한 페이지에 보여주는 카드 수) */
const PAGE_SIZE = 6;

/** 공고 상태 탭 — true(모집중)가 기본값 */
const STATUS_TABS = [
  { openOnly: true, label: '모집중' },
  { openOnly: false, label: '마감' },
] as const;

const BADGE: Record<PostBadge, { text: string; tone: LabelTone }> = {
  new: { text: '신규', tone: 'green' },
  closing: { text: '오늘 마감', tone: 'red' },
  open: { text: '모집 중', tone: 'blue' },
  closed: { text: '마감', tone: 'gray' },
};

type PostListPageProps = {
  detailBasePath?: string;
};

/**
 * 공고 목록 겸 메인 — Figma 동행 매니저_공고 목록 겸 메인페이지 188:1289
 *
 * 공고 목록은 백엔드(GET /api/v1/posts)에서 가져옵니다. 검색·필터·정렬·페이지는 서버가 처리합니다.
 * ⚠️ 목록 API 는 공고 상태와 무관하게 결제 완료된 공고를 모두 주므로, 모집이 끝난 공고는 "마감"으로 표시합니다.
 */
export default function PostListPage({
                                       detailBasePath = '/posts',
                                     }: PostListPageProps) {
  const [filters, setFilters] = useState<PostFilters>(DEFAULT_FILTERS);
  const [keywordInput, setKeywordInput] = useState('');
  const [page, setPage] = useState(0);
  // 카드마다 다른 지원 상태를 기억합니다. (postId -> 'applying' | 'applied')
  const [applyStatus, setApplyStatus] = useState<Record<number, 'applying' | 'applied'>>({});

  // 조건(필터·페이지)이 바뀔 때마다 서버에서 다시 가져옵니다.
  // result.key 로 "어떤 조건의 결과인지" 기억해 두면, 조건이 바뀐 직후에는 loading 으로 판단할 수 있습니다.
  const requestKey = JSON.stringify([filters, page]);
  const [result, setResult] = useState<{ key: string; data?: PostPage; error?: string }>();

  useEffect(() => {
    let ignore = false; // 조건이 또 바뀌면 이전 요청의 결과는 버립니다.
    fetchPosts(filters, page, PAGE_SIZE)
      .then((data) => !ignore && setResult({ key: requestKey, data }))
      .catch((error: Error) => !ignore && setResult({ key: requestKey, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [filters, page, requestKey]);

  const loading = result?.key !== requestKey;
  const posts = (!loading && result?.data?.posts) || [];
  const totalCount = (!loading && result?.data?.totalElements) || 0;
  const pageCount = Math.max(1, (!loading && result?.data?.totalPages) || 1);
  const errorMessage = !loading ? result?.error : undefined;

  const updateFilters = (patch: Partial<PostFilters>) => {
    setFilters((prev) => ({ ...prev, ...patch }));
    setPage(0);
  };

  const reset = () => {
    setFilters(DEFAULT_FILTERS);
    setKeywordInput('');
    setPage(0);
  };

  // TODO: 지원 API(POST /api/v1/applications/{postId})는 동행 매니저(ESCORT) 로그인 쿠키가 있어야 합니다.
  const handleApply = async (postId: number) => {
    setApplyStatus((prev) => ({ ...prev, [postId]: 'applying' }));
    try {
      await applyToPost(postId);
      setApplyStatus((prev) => ({ ...prev, [postId]: 'applied' }));
    } catch (error) {
      setApplyStatus((prev) => {
        const next = { ...prev };
        delete next[postId];
        return next;
      });
      window.alert(error instanceof Error ? error.message : '지원에 실패했습니다.');
    }
  };

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
      <section className="bg-white py-[50px]">
        <Container className="flex flex-col items-center">
          <SectionHeading
            title="병원 동행 공고를 찾아보세요!"
            description="지금 나에게 맞는 병원 동행 공고를 확인하고 지원할 수 있어요."
            className="mb-8 lg:mb-6"
            descriptionClassName="leading-6 lg:leading-[30px]"
          />

          {/* 모집중 / 마감 탭 — 마감된 공고 때문에 신규·모집중 공고가 뒤로 밀리지 않도록 목록 자체를 분리 */}
          <div role="tablist" aria-label="공고 상태" className="mb-5 flex gap-2.5">
            {STATUS_TABS.map((tab) => (
              <button
                key={String(tab.openOnly)}
                type="button"
                role="tab"
                aria-selected={filters.openOnly === tab.openOnly}
                onClick={() => updateFilters({ openOnly: tab.openOnly })}
                className={cn(
                  'rounded-full px-6 py-2.5 text-base font-semibold transition-colors',
                  filters.openOnly === tab.openOnly
                    ? 'bg-brand text-white'
                    : 'border border-line-soft bg-white text-brand-muted hover:bg-line-soft',
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>

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
                총 {totalCount}개의 공고가 있습니다.
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

            {loading ? (
              <p className="py-20 text-center text-base font-semibold text-brand-muted">
                공고를 불러오는 중입니다.
              </p>
            ) : errorMessage ? (
              <p role="alert" className="py-20 text-center text-base font-semibold text-brand-muted">
                공고를 불러오지 못했습니다. ({errorMessage})
              </p>
            ) : posts.length > 0 ? (
              <ul className="grid gap-x-4 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
                {posts.map((post) => (
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
                      <CardButton href={`${detailBasePath}/${post.id}`}>
                        상세보기
                      </CardButton>
                      {post.badge === 'closed' ? (
                        <CardButton variant="disabled">지원 불가</CardButton>
                      ) : applyStatus[post.id] === 'applied' ? (
                        <CardButton variant="disabled">지원 완료</CardButton>
                      ) : (
                        <CardButton variant="solid" onClick={() => handleApply(post.id)}>
                          {applyStatus[post.id] === 'applying' ? '지원 중…' : '지원하기'}
                        </CardButton>
                      )}
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
  );
}
