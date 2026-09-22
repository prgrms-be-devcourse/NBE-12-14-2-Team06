import Image from 'next/image';
import { cn } from '@/lib/cn';
import type { Manager } from '../types';

/** 크기별 글자·상자 크기 (Figma: lg = 동행 현황 745 카드, md = 지원자 카드, sm = 보고서·리뷰 396 카드) */
const SIZE = {
  lg: {
    avatar: 'size-[100px] rounded-[37.5px]',
    avatarIcon: 36,
    gap: 'gap-[25px]',
    info: 'gap-3',
    name: 'text-2xl leading-6',
    role: 'h-[31px] min-w-[111px] px-5 text-[13.5px] leading-[15px]',
    stats: 'gap-x-[18px] text-base leading-5',
    tag: 'h-[27.5px] min-w-[87.5px] text-[13px]',
    intro: 'text-base leading-[25px]',
    icon: 16,
    introBelow: true,
  },
  md: {
    avatar: 'size-[80px] rounded-[30px]',
    avatarIcon: 29,
    gap: 'gap-5',
    info: 'gap-2.5',
    name: 'text-xl leading-5',
    role: 'h-[25px] min-w-[89px] px-4 text-[10.83px] leading-3',
    stats: 'gap-x-[15px] text-sm leading-[17px]',
    tag: 'h-[22px] min-w-[70px] text-[10.83px]',
    intro: 'text-[13px] leading-[19px]',
    icon: 14,
    introBelow: false,
  },
  sm: {
    avatar: 'size-[68px] rounded-[32px]',
    avatarIcon: 24,
    gap: 'gap-[21px]',
    info: 'gap-3',
    name: 'text-[17px] leading-[17px]',
    role: 'h-[26.5px] min-w-[94.5px] px-[17px] text-[11.5px] leading-[13px]',
    stats: 'gap-x-[13px] text-[11.56px] leading-[15px]',
    tag: 'h-[23px] min-w-[74px] text-[11px]',
    intro: 'text-[11px] leading-4',
    icon: 12,
    introBelow: false,
  },
} as const;

export type ManagerProfileSize = keyof typeof SIZE;

const CHIP = 'inline-flex items-center justify-center rounded-full border border-line-soft bg-line-soft font-semibold whitespace-nowrap text-brand';

/** 동행 매니저 프로필 (아바타 · 이름 · 별점 · 완료 동행 · 지역 · 태그 · 소개). lg 는 소개글이 아래 줄에 나옵니다. */
export default function ManagerProfile({ manager, size, className }: { manager: Manager; size: ManagerProfileSize; className?: string }) {
  const s = SIZE[size];
  const intro = (
    <p className={cn('font-medium text-footer', s.intro)}>
      {manager.intro.map((line) => (
        <span key={line} className="block">
          {line}
        </span>
      ))}
    </p>
  );

  return (
    <div className={cn('flex min-w-0 flex-col gap-3', className)}>
      <div className={cn('flex min-w-0 items-center', s.gap)}>
        <div className={cn('grid shrink-0 place-items-center bg-line-soft', s.avatar)}>
          <Image src="/icons/avatar.svg" alt="" width={36} height={38} style={{ width: s.avatarIcon, height: 'auto' }} />
        </div>
        <div className={cn('flex min-w-0 flex-1 flex-col', s.info)}>
          <div className="flex flex-wrap items-center gap-1.5">
            <p className={cn('font-semibold text-brand', s.name)}>{manager.name}</p>
            <span className={cn(CHIP, s.role)}>동행 매니저</span>
          </div>
          <p className={cn('flex flex-wrap items-center font-medium text-brand', s.stats)}>
            {manager.rating > 0 ? (
              <span className="flex items-center gap-1.5">
                <Image src="/icons/client/star.svg" alt="별점" width={s.icon} height={s.icon} />
                {manager.rating.toFixed(1)}
              </span>
            ) : (
              <span>평가 없음</span>
            )}
            <span>
              완료 동행 <strong className="font-semibold">{manager.completedCount}회</strong>
            </span>
            {manager.region && (
              <span className="flex items-center gap-1.5">
                <Image src="/icons/pin.svg" alt="" width={s.icon} height={s.icon} />
                {manager.region}
              </span>
            )}
          </p>
          {manager.tags && manager.tags.length > 0 && (
            <ul className="flex flex-wrap gap-[5px]">
              {manager.tags.map((tag) => (
                <li key={tag} className={cn(CHIP, s.tag)}>
                  {tag}
                </li>
              ))}
            </ul>
          )}
          {!s.introBelow && intro}
        </div>
      </div>
      {s.introBelow && intro}
    </div>
  );
}
