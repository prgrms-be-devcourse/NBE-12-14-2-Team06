import Link from 'next/link';
import { cn } from '@/lib/cn';

type Props = {
  badge: { text: string; tone: 'green' | 'blue' | 'strong' };
  title: string;
  hospitalName: string;
  region: string;
  /** "2026년 9월 22일(화) 오전 9:00" */
  scheduleLabel: string;
  durationLabel: string;
  payLabel: string;
  detailHref: string;
  /** narrow = 리뷰 작성 화면의 858px 카드 (라벨·버튼이 조금 작습니다) */
  variant?: 'wide' | 'narrow';
};

const TONE = {
  green: 'border-[#e6ffe5] bg-[#e6ffe5] text-[#209d37]',
  blue: 'border-[#e8eefa] bg-[#e8eefa] text-[#203b9d]',
  strong: 'border-[#6796db] bg-[#6796db] text-white',
} as const;

const DIVIDER = <span aria-hidden="true" className="hidden h-[30px] w-px bg-[#e6e8ec] opacity-50 sm:block" />;

/** 동행 현황 · 보고서 · 리뷰 화면 맨 위의 공고 요약 카드 (Figma "Grid" 1282×187 / 858×187) */
export default function TripSummary({ badge, title, hospitalName, region, scheduleLabel, durationLabel, payLabel, detailHref, variant = 'wide' }: Props) {
  const wide = variant === 'wide';

  return (
    <section className="flex flex-col justify-center gap-[9px] rounded-[30px] border border-line-soft bg-white p-6 shadow-card sm:p-8 lg:min-h-[187px]">
      <div className="flex flex-wrap items-center gap-x-5 gap-y-3">
        <span
          className={cn(
            'inline-flex shrink-0 items-center justify-center border font-semibold',
            wide ? 'h-[49px] w-[145px] rounded-[30px] text-[16.9px]' : 'h-[41.65px] w-[123px] rounded-[25.5px] text-[14.4px]',
            TONE[badge.tone],
          )}
        >
          {badge.text}
        </span>
        <div className="flex min-w-0 flex-1 flex-col gap-[17px] text-brand">
          <p className="text-2xl leading-5 font-semibold">{title}</p>
          <p className="flex items-center gap-2 text-base leading-[17px] font-medium">
            {hospitalName}
            <span aria-hidden="true" className="h-[13px] w-px bg-[#e6e8ec]" />
            {region}
          </p>
        </div>
        <Link
          href={detailHref}
          className={cn(
            'flex shrink-0 items-center justify-center border border-line bg-white font-semibold text-brand transition-colors hover:bg-line-soft',
            wide ? 'h-[54px] w-[180px] rounded-[30px] text-[21px]' : 'h-[46px] w-[153px] rounded-[25.5px] text-[18px]',
          )}
        >
          공고 상세보기
        </Link>
      </div>
      <p className={cn('flex flex-wrap items-center gap-x-[15px] gap-y-1 text-base leading-[22px] font-medium text-brand', wide ? 'lg:pl-[164px]' : 'lg:pl-[140px]')}>
        <span>{scheduleLabel}</span>
        {DIVIDER}
        <span>{durationLabel}</span>
        {DIVIDER}
        <span>{payLabel}</span>
      </p>
    </section>
  );
}
