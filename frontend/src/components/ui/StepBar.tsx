import Image from 'next/image';
import { cn } from '@/lib/cn';

type Props = {
  /** 단계 이름. 번호는 순서대로 1부터 자동으로 붙습니다. */
  steps: readonly string[];
  /** 현재 단계 (1 ~ steps.length). 이전 단계는 체크 표시, 이후 단계는 흐리게 표시됩니다. */
  current: number;
  /** 스크린리더용 목록 이름 (예: "동행 진행 단계") */
  label: string;
  className?: string;
};

/**
 * 단계 진행 표시 (Figma 진행상황: 파란색 계열)
 *
 * 동행 현황(StageBar)과 회원가입(SignupStepper)이 같은 모양이라 여기 한 곳에 둡니다.
 * 단계 수만 다르므로 가로 폭은 className 으로 각 화면에서 정합니다.
 */
export default function StepBar({ steps, current, label, className }: Props) {
  return (
    <ol aria-label={label} className={cn('flex w-full', className)}>
      {steps.map((title, index) => {
        const num = index + 1;
        const done = num < current;
        const active = num === current;
        return (
          <li
            key={title}
            aria-current={active ? 'step' : undefined}
            className="flex min-w-0 flex-1 flex-col items-center gap-[18px] px-1 py-3"
          >
            <span className="relative size-9 shrink-0">
              <Image
                src={done ? '/icons/step-done-bg.svg' : active ? '/icons/step-active.svg' : '/icons/step-inactive.svg'}
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
                  {num}
                </span>
              )}
            </span>
            <span
              className={cn(
                'text-[15px] leading-[21px] font-bold whitespace-nowrap lg:text-lg',
                active ? 'text-[#6796db]' : 'text-[#91a9d8]',
              )}
            >
              {title}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
