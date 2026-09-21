import Image from 'next/image';
import type { ReactNode } from 'react';
import type { LabelTone } from '../types';
import StatusLabel from './StatusLabel';

type Props = {
  title: string;
  hospitalName: string;
  /** "서울 종로구" */
  location: string;
  label: { text: string; tone: LabelTone };
  /** 라벨 오른쪽 "2시간 전" (없으면 생략) */
  postedAgo?: string;
  dateLabel: string;
  timeLabel: string;
  durationLabel: string;
  payLabel: string;
  description: string[];
  /** 0.8489배로 줄인 카드 (Figma "내가 신청한 공고" 화면). lg 이상에서만 줄어듭니다. */
  compact?: boolean;
  /** 아래쪽 버튼들 */
  children: ReactNode;
};

const DIVIDER = 'h-px w-full max-w-[313px] self-center bg-[#e6e8ec] opacity-50';

/** Figma 562:6775 — 공고 카드 (364×311) */
export default function PostCard({
  title,
  hospitalName,
  location,
  label,
  postedAgo,
  dateLabel,
  timeLabel,
  durationLabel,
  payLabel,
  description,
  compact,
  children,
}: Props) {
  const card = (
    <article className="flex h-[311px] w-full min-w-0 flex-col justify-center gap-[5.4px] rounded-[30px] border-[0.68px] border-line bg-white p-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)] lg:w-[364px]">
      <div className="flex w-full max-w-[313px] items-center gap-5 self-center">
        <Image
          src="/icons/image-placeholder.svg"
          alt=""
          width={72}
          height={72}
          className="size-[71.6px] shrink-0"
        />
        <div className="flex h-[113.7px] min-w-0 flex-1 flex-col gap-[13.5px]">
          <div className="flex items-center justify-between gap-[5.4px]">
            <StatusLabel tone={label.tone}>{label.text}</StatusLabel>
            {postedAgo && (
              <span className="text-[10.83px] leading-[15px] font-semibold text-[#c0c0c2]">{postedAgo}</span>
            )}
          </div>
          <p className="truncate text-[16.2px] leading-4 font-semibold text-brand">{title}</p>
          <p className="truncate text-[10.83px] leading-[13.5px] font-semibold text-brand">{hospitalName}</p>
          <p className="flex items-center gap-[6.8px] text-[9.5px] leading-[13.5px] font-medium text-brand">
            <Image src="/icons/pin.svg" alt="" width={11} height={13} className="shrink-0" />
            {location}
          </p>
        </div>
      </div>

      <div className={DIVIDER} />

      <div className="flex h-[31.8px] items-center justify-center gap-[16.9px] px-2">
        <p className="w-[74px] text-center text-[10.83px] leading-[13.5px] font-semibold text-brand">
          {dateLabel}
          <br />
          {timeLabel}
        </p>
        <span aria-hidden="true" className="h-[23.7px] w-px bg-[#e6e8ec] opacity-50" />
        <p className="w-[74px] text-center text-[10.83px] leading-[13.5px] font-semibold text-brand">
          {durationLabel}
        </p>
        <span aria-hidden="true" className="h-[23.7px] w-px bg-[#e6e8ec] opacity-50" />
        <p className="w-[74px] text-center text-[9.5px] leading-[13.5px] font-semibold text-brand">{payLabel}</p>
      </div>

      <div className={DIVIDER} />

      <p className="flex h-[45px] w-full max-w-[300px] flex-col justify-center self-center text-[10.83px] leading-[13.5px] font-semibold text-brand">
        {description.map((line) => (
          <span key={line}>{line}</span>
        ))}
      </p>

      <div className="flex w-full max-w-[313px] items-center justify-center gap-[10.2px] self-center">{children}</div>
    </article>
  );

  if (!compact) return card;

  return (
    <div className="lg:h-[264px] lg:w-[309px]">
      <div className="lg:w-[364px] lg:origin-top-left lg:scale-[0.8489]">{card}</div>
    </div>
  );
}
