import Image from 'next/image';
import { cn } from '@/lib/cn';
import type { EscortStage } from '../../types';

const STEP_LABELS = ['매칭 완료', '동행 중', '동행 완료'];
const STAGE_INDEX: Record<EscortStage, number> = { ready: 0, ongoing: 1, done: 2 };

/** 위쪽 3단계 표시 (Figma 진행상황: 파란색 계열) */
export default function StageBar({ stage }: { stage: EscortStage }) {
  const current = STAGE_INDEX[stage];
  return (
    <ol aria-label="동행 진행 단계" className="mx-auto mb-[30px] flex w-full max-w-[411px]">
      {STEP_LABELS.map((label, index) => {
        const done = index < current;
        const active = index === current;
        return (
          <li key={label} aria-current={active ? 'step' : undefined} className="flex flex-1 flex-col items-center gap-[18px] px-1 py-3">
            <span className="relative size-9 shrink-0">
              <Image
                src={done ? '/icons/step-done-bg.svg' : active ? '/icons/escort/step-active.svg' : '/icons/escort/step-upcoming.svg'}
                alt=""
                width={36}
                height={36}
              />
              {done ? (
                <Image src="/icons/step-done-check.svg" alt="완료" width={36} height={36} className="absolute inset-0" />
              ) : (
                <span
                  className={cn(
                    'absolute inset-0 grid place-items-center text-[15px] leading-none font-semibold',
                    active ? 'text-white' : 'text-[#6796db]',
                  )}
                >
                  {index + 1}
                </span>
              )}
            </span>
            <span className={cn('text-[15px] leading-[21px] font-bold whitespace-nowrap lg:text-lg', active ? 'text-[#6796db]' : 'text-[#91a9d8]')}>
              {label}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
