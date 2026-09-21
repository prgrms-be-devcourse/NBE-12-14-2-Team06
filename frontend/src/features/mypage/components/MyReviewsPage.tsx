'use client';

import Image from 'next/image';
import { useState } from 'react';
import { SectionHeading } from '@/components/ui';
import { REVIEWS } from '../model';
import DateRangeFilter from './DateRangeFilter';
import EmptyState from './EmptyState';
import MyPageShell from './MyPageShell';
import StatBar from './StatBar';

const TAG = 'inline-flex h-[18px] items-center rounded-full px-[15px] text-[10px] leading-[15px] font-semibold whitespace-nowrap';

/**
 * 마이페이지 — 받은 리뷰 (Figma 562:14068)
 *
 * ⚠️ 모의 데이터(model/settlements.ts 의 REVIEWS)를 보여줍니다. 리뷰 API 연결 전입니다.
 */
export default function MyReviewsPage() {
  const [fromInput, setFromInput] = useState('');
  const [toInput, setToInput] = useState('');
  const [range, setRange] = useState({ from: '', to: '' });

  const reviews = REVIEWS.filter(
    (review) => (!range.from || review.date >= range.from) && (!range.to || review.date <= range.to),
  );
  const average = REVIEWS.length
    ? Math.round(REVIEWS.reduce((sum, review) => sum + review.rating, 0) / REVIEWS.length)
    : 0;

  return (
    <MyPageShell>
      <div className="flex max-w-[1000px] flex-col items-center gap-[30px] pt-8 lg:pt-[50px]">
        <SectionHeading title="받은 리뷰" description="동행을 마친 공고에 대한 리뷰를 확인할 수 있습니다." className="mb-0" />

        <StatBar
          items={[
            { icon: '/icons/mypage/stat-review.svg', label: '리뷰', value: `${REVIEWS.length}개` },
            { icon: '/icons/mypage/stat-rating.svg', label: '리뷰 평점', value: `${average}점` },
          ]}
        />

        <DateRangeFilter
          from={fromInput}
          to={toInput}
          onFromChange={setFromInput}
          onToChange={setToInput}
          onSearch={() => setRange({ from: fromInput, to: toInput })}
        />

        {reviews.length > 0 ? (
          <ul className="grid w-full max-w-[910px] gap-4 sm:grid-cols-2">
            {reviews.map((review) => (
              <li key={review.id}>
                <article className="flex min-h-[271px] flex-col items-center justify-center gap-2.5 rounded-[30px] border-[0.68px] border-line bg-white px-[22px] py-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)]">
                  <div className="flex w-full items-center gap-5">
                    <Image src="/icons/image-placeholder.svg" alt="" width={60} height={60} className="size-[60px] shrink-0" />
                    <div className="min-w-0">
                      <p className="truncate text-base leading-4 font-semibold text-brand">{review.title}</p>
                      <p className="mt-2.5 flex items-center gap-2 text-xs leading-4 font-medium text-brand">
                        {review.hospitalName}
                        <span aria-hidden="true" className="h-3 w-px bg-[#e6e8ec]" />
                        {review.location}
                      </p>
                    </div>
                  </div>
                  <Image
                    src={`/icons/mypage/stars-${review.rating}.svg`}
                    alt={`별점 ${review.rating}점`}
                    width={158}
                    height={27}
                  />
                  <ul aria-label="리뷰 태그" className="flex flex-wrap justify-center gap-1.5">
                    {review.positives.map((tag) => (
                      <li key={tag} className={`${TAG} bg-[#6796db] text-white`}>{tag}</li>
                    ))}
                    {review.negatives.map((tag) => (
                      <li key={tag} className={`${TAG} bg-[#e6e8ec] text-brand`}>{tag}</li>
                    ))}
                  </ul>
                  <p className="flex min-h-[61px] w-full items-center justify-center rounded-[25px] bg-[#e6e8ec] px-5 py-2.5 text-center text-xs leading-4 text-brand">
                    {review.comment}
                  </p>
                </article>
              </li>
            ))}
          </ul>
        ) : (
          <EmptyState title="받은 리뷰가 없습니다." description="동행을 마치면 의뢰인이 남긴 리뷰가 이곳에 표시됩니다." />
        )}
      </div>
    </MyPageShell>
  );
}
