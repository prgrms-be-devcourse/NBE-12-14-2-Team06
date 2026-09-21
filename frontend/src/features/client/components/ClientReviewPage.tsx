'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';
import { STAGE_VIEW, getClientEscortCase } from '../model/escort';
import ManagerInfoCard from './ManagerInfoCard';
import TripSummary from './TripSummary';

const RATINGS = [1, 2, 3, 4, 5];
const KEYWORDS = [
  ['친절해요', '시간을 잘 지켜요', '보고서가 꼼꼼해요', '소통이 잘 돼요'],
  ['어르신을 세심하게 챙겨요', '시간 약속이 아쉬워요', '소통이 잘 안 됐어요'],
  ['응대가 아쉬웠어요', '보고서 내용이 부족해요'],
];
const GUIDES = [
  ['실제 이용 경험을 바탕으로 작성해주세요.'],
  ['다른 이용자에게 도움이 되는 소중한 평가입니다.'],
  ['욕설, 비방 등 부적절한 내용은 삭제될 수 있습니다.'],
  ['리뷰는 한 번만 작성될 수 있으며,', '제출 후 수정이 어려울 수 있습니다.'],
];

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-[35px]';
const TITLE = 'text-2xl leading-6 font-semibold text-brand';
const HINT = 'text-sm leading-5 font-medium text-brand';

/** 별 한 개 (Figma 50×50 Icon). 선택되면 파란색으로 채웁니다. */
function Star({ filled }: { filled: boolean }) {
  return filled ? (
    <svg viewBox="0 0 47.41 45.39" aria-hidden="true" className="size-[50px]">
      <path
        d="M21.8025 2.3815C22.4014 0.539345 25.0083 0.539356 25.6072 2.3815L30.0447 16.0377L44.4041 16.0387C46.3413 16.0388 47.147 18.518 45.5798 19.6569L33.9627 28.0973L38.4002 41.7536C38.9988 43.5961 36.8894 45.1286 35.322 43.9899L23.7048 35.5504L12.0877 43.9899C10.5202 45.1286 8.41092 43.5961 9.00953 41.7536L13.447 28.0973L1.82984 19.6569C0.262689 18.518 1.06835 16.0388 3.00562 16.0387L17.365 16.0377L21.8025 2.3815Z"
        fill="#6796DB"
        stroke="#6796DB"
        strokeWidth="2"
      />
    </svg>
  ) : (
    <Image src="/icons/client/star-empty.svg" alt="" width={47} height={45} className="size-[50px]" />
  );
}

/**
 * 동행 매니저 리뷰 작성 — Figma 의뢰인_리뷰 작성 459:3023
 *
 * ⚠️ 제출해도 서버로 보내지 않고 동행 현황 화면으로 돌아갑니다. (리뷰 API 연결 전)
 */
export default function ClientReviewPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const router = useRouter();
  const escort = getClientEscortCase(Number(applicationId));

  const [rating, setRating] = useState(0);
  const [keywords, setKeywords] = useState<string[]>([]);
  const [showError, setShowError] = useState(false);

  if (!escort) {
    return (
      <AppShell user={MOCK_CLIENT}>
        <section className="bg-white py-[100px] text-center text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</section>
      </AppShell>
    );
  }

  const toggleKeyword = (keyword: string) =>
    setKeywords((prev) => (prev.includes(keyword) ? prev.filter((item) => item !== keyword) : [...prev, keyword]));

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (rating === 0) {
      setShowError(true);
      return;
    }
    // TODO: 리뷰 등록 API 연결 (평점 rating · 세부 평가 keywords · 추가 의견 comment)
    router.push(`/client/escort/${escort.applicationId}`);
  };

  return (
    <AppShell user={MOCK_CLIENT}>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading title="동행 매니저 리뷰 작성" description="이번 동행에 함께해준 매니저의 서비스를 평가해주세요." className="mb-6" />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,858px)_minmax(0,396px)] lg:justify-center">
            <form onSubmit={handleSubmit} className="flex min-w-0 flex-col gap-[22px]">
              <TripSummary
                badge={STAGE_VIEW.done.badge}
                title={escort.title}
                hospitalName={escort.hospitalName}
                region={escort.region}
                scheduleLabel={escort.scheduleLabel}
                durationLabel={escort.durationLabel}
                payLabel={escort.payLabel}
                detailHref={`/client/posts/${escort.postId}`}
                variant="narrow"
              />

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-2')}>평점 평가</h2>
                <p className={HINT}>이번 동행에 대한 전반적인 만족도를 선택해주세요.</p>
                <div role="radiogroup" aria-label="평점" className="mt-2.5 flex justify-center gap-2 py-[23px] sm:gap-5">
                  {RATINGS.map((value) => (
                    <button
                      key={value}
                      type="button"
                      role="radio"
                      aria-checked={rating === value}
                      aria-label={`${value}점`}
                      onClick={() => {
                        setRating(value);
                        setShowError(false);
                      }}
                      className="rounded-md transition-transform hover:scale-105"
                    >
                      <Star filled={value <= rating} />
                    </button>
                  ))}
                </div>
                {showError && (
                  <p role="alert" className="text-center text-sm font-medium text-[#b91d1d]">
                    평점을 선택해주세요.
                  </p>
                )}
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-2 flex items-end gap-2.5')}>
                  세부 평가 <span className="pb-px text-[13px] leading-[22px] font-medium">(복수 선택 가능)</span>
                </h2>
                <p className={HINT}>매니저의 어떤 점이 좋았나요? 해당하는 항목을 선택해주세요.</p>
                <div className="mt-5 flex flex-col gap-2.5">
                  {KEYWORDS.map((row) => (
                    <div key={row.join()} className="flex flex-wrap justify-center gap-[15px]">
                      {row.map((keyword) => {
                        const selected = keywords.includes(keyword);
                        return (
                          <button
                            key={keyword}
                            type="button"
                            aria-pressed={selected}
                            onClick={() => toggleKeyword(keyword)}
                            className={cn(
                              'flex h-[46px] items-center justify-center gap-2.5 rounded-[25px] border px-5 text-base leading-[13px] font-semibold whitespace-nowrap transition-colors',
                              selected ? 'border-brand bg-brand text-white' : 'border-line bg-white text-brand hover:bg-line-soft',
                            )}
                          >
                            <Image
                              src="/icons/client/chip-plus.svg"
                              alt=""
                              width={10}
                              height={10}
                              className={cn('size-2.5 transition-transform', selected && 'rotate-45 brightness-0 invert')}
                            />
                            {keyword}
                          </button>
                        );
                      })}
                    </div>
                  ))}
                </div>
              </section>

              <section className={CARD}>
                <h2 className={cn(TITLE, 'mb-2 flex items-end gap-2.5')}>
                  추가 의견 <span className="pb-px text-[13px] leading-[22px] font-medium">(선택)</span>
                </h2>
                <label htmlFor="review-comment" className={cn(HINT, 'block')}>
                  이번 동행에 대한 의견을 자유롭게 작성해주세요.
                </label>
                <textarea
                  id="review-comment"
                  name="comment"
                  placeholder="예) 특히 도움이 되었던 점이나 개선되었으면 하는 점이 있다면 알려주세요."
                  className="mt-[11px] h-[99px] w-full resize-none rounded-[30px] border border-line bg-white p-5 text-sm leading-5 text-brand placeholder:text-brand-muted"
                />
              </section>

              <div className="flex gap-[15px]">
                <Link
                  href={`/client/escort/${escort.applicationId}`}
                  className="flex h-14 flex-1 items-center justify-center rounded-[25px] border border-line bg-white text-xl leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft"
                >
                  취소
                </Link>
                <button type="submit" className="flex h-14 flex-1 items-center justify-center rounded-[25px] bg-brand text-xl leading-[18px] font-semibold text-white transition-colors hover:bg-brand-hover">
                  리뷰 제출하기
                </button>
              </div>
            </form>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <ManagerInfoCard manager={escort.manager} size="sm" title="동행 매니저 정보" />

              <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6">
                <h2 className="mb-4 flex items-center gap-[15px] text-2xl leading-6 font-semibold text-brand">
                  <Image src="/icons/escort/warning.svg" alt="" width={31} height={31} className="size-7" />
                  리뷰 작성 안내
                </h2>
                <ul className="flex flex-col px-3">
                  {GUIDES.map((lines) => (
                    <li key={lines[0]} className="flex items-center gap-[19px] py-[10.5px] text-sm leading-5 font-semibold text-brand">
                      <Image src="/icons/escort/bullet.svg" alt="" width={7.5} height={7.5} className="shrink-0" />
                      <span>
                        {lines.map((line) => (
                          <span key={line} className="block">
                            {line}
                          </span>
                        ))}
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
