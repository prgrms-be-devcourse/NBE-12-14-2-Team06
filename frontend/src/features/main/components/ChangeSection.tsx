'use client';

import Image from 'next/image';

import { Container, SectionHeading } from '@/components/ui';
import { CHANGES } from '../model';

/**
 * 구분선 규칙
 *  - 1열(모바일): 첫 칸 빼고 위쪽 선
 *  - 2열(sm):     1·2번째 위쪽 선 제거, 짝수 칸에 왼쪽 선
 *  - 4열(xl):     위쪽 선 모두 제거, 첫 칸 빼고 왼쪽 선
 */
const CELL = [
  'flex flex-col items-center justify-center gap-5 px-2 py-5 text-center border-line',
  'border-t [&:first-child]:border-t-0',
  'sm:[&:nth-child(-n+2)]:border-t-0 sm:[&:nth-child(even)]:border-l',
  'xl:border-t-0 xl:border-l xl:[&:first-child]:border-l-0',
].join(' ');

/** Figma 564:18114 — 가지가 만드는 특별한 변화 */
export default function ChangeSection() {
  return (
    <section id="change" className="bg-white py-14 lg:py-20">
      <Container>
        <SectionHeading
          title="가지가 만드는 특별한 변화"
          description="더 안전하고, 더 편리하고, 더 건강한 일상을 위해"
        />

        <div className="grid grid-cols-1 rounded-card border border-line bg-white p-8 drop-shadow-soft sm:grid-cols-2 xl:grid-cols-4 xl:px-[66px] xl:py-10">
          {CHANGES.map(({ image, imageAlt, title, description }) => (
            <div key={title} className={CELL}>
              <Image
                src={image}
                alt={imageAlt}
                width={240}
                height={240}
                className="size-[120px] shrink-0 object-contain"
              />
              <h3 className="text-xl leading-6 font-semibold text-brand">{title}</h3>
              <p className="max-w-[170px] text-base leading-[22px] font-semibold text-brand-muted">
                {description[0]}
                <br />
                {description[1]}
              </p>
            </div>
          ))}
        </div>
      </Container>
    </section>
  );
}
