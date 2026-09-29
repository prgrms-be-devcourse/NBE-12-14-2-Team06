import Image from 'next/image';
import { EscortGradeBadge } from '@/components/ui';
import { reviewTagInfo } from '@/features/review';
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
    tag: 'h-[27.5px] px-3.5 text-[13px]',
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
    tag: 'h-[22px] px-3 text-[10.83px]',
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
    tag: 'h-[23px] px-3 text-[11px]',
    intro: 'text-[11px] leading-4',
    icon: 12,
    introBelow: false,
  },
} as const;

export type ManagerProfileSize = keyof typeof SIZE;

/**
 * 칩의 모양만 담고 색은 아래 CHIP_* 에서 반드시 하나 골라 함께 넘깁니다.
 * cn 은 단순 이어붙이기라 색을 덧칠하면 이기지 못합니다 — Tailwind v4 가 임의값(bg-[#...])을
 * 테마 색(bg-line-soft)보다 먼저 내보내서, 나중에 적은 쪽이 아니라 테마 색이 이깁니다.
 * 좌우 여백(px-*)은 SIZE.tag 가 주므로 폭은 고정하지 않습니다 — 문구 길이에 따라 늘어납니다.
 */
const CHIP = 'inline-flex items-center justify-center rounded-full border font-semibold whitespace-nowrap';

/** 긍정 리뷰 태그 — 마이페이지 "받은 리뷰" 칩과 같은 파랑입니다. */
const CHIP_POSITIVE = 'border-[#6796db] bg-[#6796db] text-white';
/** 부정 리뷰 태그 (기본 회색) */
const CHIP_NEGATIVE = 'border-line-soft bg-line-soft text-brand';
/** 신원 인증 배지 (연한 파랑) */
const CHIP_VERIFIED = 'border-transparent bg-[#e4f0ff] text-[#3576d6]';

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
          <Image src="/images/post/eggplant.png" alt="" width={36} height={38} className="object-contain" style={{ width: s.avatarIcon, height: 'auto' }} />
        </div>
        <div className={cn('flex min-w-0 flex-1 flex-col', s.info)}>
          <div className="flex flex-wrap items-center gap-1.5">
            <p className={cn('font-semibold text-brand', s.name)}>
              {manager.name}
            </p>
            {manager.grade && (
                <EscortGradeBadge grade={manager.grade} />
            )}
            {manager.verified && (
              <span className={cn(CHIP, CHIP_VERIFIED, s.tag)}>인증완료</span>
            )}
          </div>
          <p className={cn('flex flex-wrap items-center font-medium text-brand', s.stats)}>
            {manager.rating > 0 ? (
              <span className="flex items-center gap-1.5">
                <Image src="/icons/client/star.svg" alt="별점" width={s.icon} height={s.icon} />
                {manager.rating.toFixed(1)}
                {manager.ratingCount !== undefined && ` (${manager.ratingCount})`}
              </span>
            ) : (
              <span>평가 없음</span>
            )}
            <span>
              완료 동행 <strong className="font-semibold">{manager.completedCount}회</strong>
            </span>
            {manager.noShowCount !== undefined && (
              <span>
                노쇼 <strong className="font-semibold">{manager.noShowCount}회</strong>
              </span>
            )}
            {manager.age !== undefined && (
              <span>
                {manager.age}세{manager.gender && ` · ${manager.gender === 'MALE' ? '남성' : '여성'}`}
              </span>
            )}
            {manager.region && (
              <span className="flex items-center gap-1.5">
                <Image src="/icons/pin.svg" alt="" width={s.icon} height={s.icon} />
                {manager.region}
              </span>
            )}
          </p>
          {manager.tags && manager.tags.length > 0 && (
            <ul className="flex flex-wrap gap-[5px]">
              {manager.tags.map((tag) => {
                const { label, positive } = reviewTagInfo(tag);
                return (
                  <li key={tag} className={cn(CHIP, positive ? CHIP_POSITIVE : CHIP_NEGATIVE, s.tag)}>
                    {label}
                  </li>
                );
              })}
            </ul>
          )}
          {!s.introBelow && intro}
        </div>
      </div>
      {s.introBelow && intro}
    </div>
  );
}
