'use client';

import { useRef, useState, type KeyboardEvent } from 'react';
import { Container, SectionHeading, ImagePlaceholder, UserIcon } from '@/components/ui';
import { cn } from '@/lib/cn';
import { PERSONAS } from '../model';
import Image from 'next/image';

/** Figma 564:18079 — 이런 분들께 추천해요! (탭) */
export default function PersonaSection() {
  const [activeIndex, setActiveIndex] = useState(0);
  const tabRefs = useRef<Array<HTMLButtonElement | null>>([]);
  const active = PERSONAS[activeIndex];

  const handleKeyDown = (event: KeyboardEvent<HTMLButtonElement>, index: number) => {
    let next: number | null = null;
    if (event.key === 'ArrowRight') next = (index + 1) % PERSONAS.length;
    if (event.key === 'ArrowLeft') next = (index - 1 + PERSONAS.length) % PERSONAS.length;
    if (next === null) return;

    event.preventDefault();
    setActiveIndex(next);
    tabRefs.current[next]?.focus();
  };

  return (
    <section id="persona" className="bg-white py-14 lg:py-20">
      <Container width="narrow">
        <SectionHeading
          title="이런 분들께 추천해요!"
          description="서비스 이용자 뿐만 아니라 서비스 제공자까지!"
        />

        <div
          role="tablist"
          aria-label="추천 대상"
          className="mb-[30px] flex flex-col gap-3 lg:flex-row lg:gap-5"
        >
          {PERSONAS.map((persona, index) => {
            const selected = index === activeIndex;
            return (
              <button
                key={persona.id}
                ref={(el) => {
                  tabRefs.current[index] = el;
                }}
                role="tab"
                id={`tab-${persona.id}`}
                aria-controls={`panel-${persona.id}`}
                aria-selected={selected}
                tabIndex={selected ? 0 : -1}
                onClick={() => setActiveIndex(index)}
                onKeyDown={(event) => handleKeyDown(event, index)}
                className={cn(
                  'flex min-w-0 flex-1 items-center gap-[30px] rounded-card border bg-white px-[30px] py-5 text-left drop-shadow-soft transition',
                  'lg:min-h-[123px]',
                  selected
                    ? 'border-brand-muted opacity-100'
                    : 'border-line opacity-50 hover:opacity-80',
                )}
              >
                <span className="grid size-12 shrink-0 place-items-center rounded-[26px] bg-line-soft">
                  <UserIcon className="size-[22px] text-brand" />
                </span>
                <span>
                  <span className="block text-xl leading-[22px] font-semibold text-brand">
                    {persona.tabTitle}
                  </span>
                  <span className="mt-4 block text-base leading-5 text-brand-muted">
                    {persona.tabDesc.map((line, i) => (
                      <span key={line}>
                        {i > 0 && <br />}
                        {line}
                      </span>
                    ))}
                  </span>
                </span>
              </button>
            );
          })}
        </div>

        <div
          role="tabpanel"
          id={`panel-${active.id}`}
          aria-labelledby={`tab-${active.id}`}
          className="flex flex-col items-stretch overflow-hidden rounded-card border border-line-soft bg-white shadow-card lg:flex-row lg:items-center lg:gap-10 lg:pr-10"
        >
          <div className="lg:flex-[0_0_422px]">
            <div className="w-full self-start lg:flex-[0_0_422px]">
              <Image
                src={active.image}
                alt={active.imageAlt}
                width={422}
                height={396}
                className="h-auto w-full"
              />
            </div>
          </div>
          <div className="min-w-0 flex-1 px-7 pt-7 pb-9 lg:p-0 lg:py-10">
            <h3 className="text-2xl leading-6 font-semibold text-brand">{active.headline}</h3>
            <p className="mt-5 text-base leading-[22px] text-brand">
              {active.body[0]}
              <br />
              {active.body[1]}
            </p>
          </div>
        </div>
      </Container>
    </section>
  );
}
