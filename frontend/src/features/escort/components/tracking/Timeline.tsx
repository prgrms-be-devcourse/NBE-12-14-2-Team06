import Image from 'next/image';
import { cn } from '@/lib/cn';
import type { EscortCase } from '../../types';
import { CARD, CARD_TITLE } from './tracking';

/** 오른쪽 아래 "실시간 현황" 타임라인. compact 는 의뢰인 화면의 촘촘한 간격 (Figma 단계 간격 38px) */
export default function Timeline({ steps, compact }: { steps: EscortCase['timeline']; compact?: boolean }) {
  return (
    <section className={cn(CARD, 'py-8', compact ? 'px-[35px]' : 'px-6')}>
      <h2 className={cn(CARD_TITLE, compact ? 'mb-5' : 'mb-6')}>실시간 현황</h2>
      <ol>
        {steps.map((step, index) => {
          const last = index === steps.length - 1;
          return (
            <li key={step.label} className={cn('relative flex gap-4 last:pb-0', compact ? 'pb-[13px]' : 'pb-[18px]')}>
              {!last && (
                <span
                  aria-hidden="true"
                  className={cn('absolute top-[25px] bottom-0 left-[11px] w-[3px] rounded-full', step.done && steps[index + 1].done ? 'bg-brand' : 'bg-[#a3a3a4]/30')}
                />
              )}
              <Image
                src={step.done ? '/icons/escort/timeline-check.svg' : '/icons/escort/timeline-dot.svg'}
                alt={step.done ? '완료' : '대기'}
                width={25}
                height={25}
                className="relative shrink-0"
              />
              <div className={cn('flex min-w-0 flex-1 items-center gap-3', !step.done && 'opacity-40')}>
                <span className="w-[86px] shrink-0 text-base leading-5 font-semibold text-brand">{step.label}</span>
                <span className="min-w-0 flex-1 text-xs leading-4 font-medium text-brand-muted">{step.description}</span>
                {step.time && <span className="shrink-0 text-xs leading-4 font-medium text-brand-muted">{step.time}</span>}
              </div>
            </li>
          );
        })}
      </ol>
    </section>
  );
}
