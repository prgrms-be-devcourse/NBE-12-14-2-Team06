'use client';

import Image from 'next/image';
import Link from 'next/link';
import { cn } from '@/lib/cn';
import type { RoleOption } from '../types';

type Props = { option: RoleOption };

/** Figma 564:17913 (의뢰인) · 564:17937 (동행인) — 가입 유형 카드 */
export default function RoleCard({ option }: Props) {
  const solid = option.variant === 'solid';

  return (
    // 테두리(1px) 포함 바깥쪽에서 25px 이므로 p-6 + border
    <article className="flex w-full max-w-[412px] flex-col gap-2.5 overflow-clip rounded-[25px] border border-line bg-white p-6 shadow-card">
      {/* 이미지·버튼(300px)은 글 영역(282px)보다 넓게 가운데 정렬 — max-w 로 좁은 화면에서만 줄어듭니다 */}
      <div className="flex flex-col items-center rounded-[25px] border border-line bg-line-soft px-6 py-[29px] shadow-card lg:px-[39px]">
        <Image
          src={option.image}
          alt={option.imageAlt}
          width={300}
          height={107}
          className="h-[107px] w-[300px] max-w-[calc(100%+18px)] object-contain"
        />

        <h2 className="mt-3 w-full text-2xl leading-6 font-semibold text-brand lg:whitespace-nowrap">
          {option.title}
        </h2>
        <p className="mt-3.5 w-full text-base leading-6 text-brand">
          {option.description[0]}
          <br />
          {option.description[1]}
        </p>

        <Link
          href={option.href}
          className={cn(
            'mt-[26px] flex h-[49px] w-[300px] max-w-[calc(100%+18px)] items-center justify-center gap-[3px] rounded-[30px] px-[18px] py-3.5',
            'text-base leading-5 font-semibold drop-shadow-soft transition-colors',
            solid ? 'bg-brand text-white hover:bg-brand-hover' : 'bg-white text-brand hover:bg-line-soft',
          )}
        >
          {option.cta}
          <span aria-hidden="true" className="grid size-3 shrink-0 place-items-center">
            <Image
              src={solid ? '/icons/arrow-right-white.svg' : '/icons/arrow-right-brand.svg'}
              alt=""
              width={9.68566}
              height={9.52414}
            />
          </span>
        </Link>
      </div>

      <ul className="flex flex-col gap-[18px] py-[17px]">
        {option.benefits.map((text) => (
          <li key={text} className="flex items-center gap-[5px]">
            <span aria-hidden="true" className="grid size-4 shrink-0 place-items-center">
              <Image src="/icons/check-circle.svg" alt="" width={14.416} height={14.416} />
            </span>
            <span className="min-w-0 flex-1 text-base leading-[22px] text-brand">{text}</span>
          </li>
        ))}
      </ul>
    </article>
  );
}
