'use client';

import Image from 'next/image';
import { cn } from '@/lib/cn';

type Props = {
  id: string;
  title: string;
  description: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  /** 있으면 "필수 / 선택" 라벨과 "내용 보기"를 함께 보여줍니다. 전체 동의 카드에는 넣지 않습니다. */
  badge?: 'required' | 'optional';
  /** 체크하지 않으면 제출할 수 없습니다. */
  required?: boolean;
  /** 전체 동의 카드처럼 테두리를 진하게 */
  emphasized?: boolean;
};

/** Figma 564:17358 — 약관 동의 한 줄 (체크박스 · 제목 · 설명 · 내용 보기) */
export default function AgreementCard({
  id,
  title,
  description,
  checked,
  onChange,
  badge,
  required,
  emphasized,
}: Props) {
  return (
    <div
      className={cn(
        'flex min-h-[100px] flex-wrap items-center gap-x-5 gap-y-3 rounded-[30px] border bg-white px-6 py-5 shadow-card lg:flex-nowrap lg:px-[34px]',
        emphasized ? 'border-footer' : 'border-line',
      )}
    >
      <label className="flex min-w-0 basis-full cursor-pointer items-center gap-4 lg:basis-0 lg:flex-1 lg:gap-[35px]">
        <input
          type="checkbox"
          id={id}
          name={id}
          checked={checked}
          required={required}
          onChange={(event) => onChange(event.target.checked)}
          className="peer sr-only"
        />
        <span
          aria-hidden="true"
          className={cn(
            'grid size-[25px] shrink-0 place-items-center rounded-[3px] border-2 border-brand',
            'peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-brand',
            checked ? 'bg-brand' : 'bg-white',
          )}
        >
          {checked && <span className="mb-0.5 h-3 w-1.5 rotate-45 border-r-2 border-b-2 border-white" />}
        </span>

        <span className="flex min-w-0 flex-1 flex-col gap-2.5">
          <span className="flex flex-wrap items-center gap-x-[15px] gap-y-1">
            <span className="text-lg leading-6 font-semibold text-brand lg:text-2xl lg:leading-[17px]">
              {title}
            </span>
            {badge && (
              <span
                className={cn(
                  'grid h-7 w-[101px] shrink-0 place-items-center rounded-full text-base leading-[22px] font-semibold',
                  badge === 'required' ? 'bg-[#ffe3e3] text-[#b91d1d]' : 'bg-[#e6e8ec] text-footer',
                )}
              >
                {badge === 'required' ? '필수' : '선택'}
              </span>
            )}
          </span>
          <span className="text-sm leading-5 font-semibold text-brand-muted lg:text-base lg:leading-4">
            {description}
          </span>
        </span>
      </label>

      {badge && (
        // TODO: 약관 전문을 보여주는 화면/모달이 정해지면 연결하세요.
        <button
          type="button"
          aria-label={`${title} 내용 보기`}
          className="ml-auto flex shrink-0 items-center gap-5 text-base leading-6 font-semibold text-brand-muted transition-colors hover:text-brand"
        >
          내용 보기
          <span aria-hidden="true" className="grid h-3.5 w-[7px] place-items-center">
            <Image src="/icons/chevron-down.svg" alt="" width={16} height={9} className="max-w-none -rotate-90" />
          </span>
        </button>
      )}
    </div>
  );
}
