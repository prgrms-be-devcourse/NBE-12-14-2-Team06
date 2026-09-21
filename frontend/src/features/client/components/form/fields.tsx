import Image from 'next/image';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

/** 입력칸 공통 모양 (Figma Input Text: 높이 61 · 라운드 20 · 그림자) */
export const FIELD =
  'h-[61px] w-full rounded-[20px] border border-line-soft bg-white px-4 text-base leading-5 text-brand shadow-card placeholder:text-brand-muted focus-visible:outline-offset-0 disabled:bg-white';

/** "라벨 + 입력" 한 줄. 라벨 칸은 132px (글자 100 + 좌우 여백 16) */
export function FormRow({
  label,
  htmlFor,
  children,
  className,
}: {
  label: string;
  htmlFor?: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn('flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-2.5', className)}>
      <label htmlFor={htmlFor} className="shrink-0 px-4 text-base leading-5 font-semibold text-brand sm:w-[132px]">
        {label}
      </label>
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}

/** 섹션 제목 (기본 정보 · 일정 정보 …) */
export function FormSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section>
      <h2 className="mb-[25px] px-[11px] text-2xl leading-6 font-semibold text-brand">{title}</h2>
      <div className="flex flex-col gap-[5px]">{children}</div>
    </section>
  );
}

type SelectProps = {
  id: string;
  name: string;
  placeholder: string;
  options: string[];
  value?: string;
  defaultValue?: string;
  onChange?: (value: string) => void;
  required?: boolean;
  disabled?: boolean;
  customMessage?: string;
};

/** 화살표가 있는 선택 상자 */
export function SelectField({ id, name, placeholder, options, value, defaultValue, onChange, required, disabled, customMessage }: SelectProps) {
  const controlled = value !== undefined;
  return (
    <div className="relative">
      <select
        id={id}
        name={name}
        required={required}
        disabled={disabled}
        {...(controlled ? { value } : { defaultValue: defaultValue ?? '' })}
        onChange={(event) => onChange?.(event.target.value)}
        ref={(element) => element?.setCustomValidity(customMessage ?? '')}
        className={cn(FIELD, 'appearance-none pr-12 invalid:text-brand-muted')}
      >
        <option value="" disabled hidden>
          {placeholder}
        </option>
        {options.map((option) => (
          <option key={option} value={option} className="text-brand">
            {option}
          </option>
        ))}
      </select>
      <Image src="/icons/escort/report-select.svg" alt="" width={14} height={7} className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2" />
    </div>
  );
}

/** 돋보기 아이콘이 있는 입력칸 (병원명 · 출발지) */
export function SearchField({ id, name, placeholder, defaultValue }: { id: string; name: string; placeholder: string; defaultValue?: string }) {
  return (
    // TODO: 병원 검색 / 주소 검색 연결 (지금은 직접 입력만 됩니다)
    <div className="relative">
      <input id={id} name={name} required defaultValue={defaultValue} placeholder={placeholder} className={cn(FIELD, 'pr-12')} />
      <Image src="/icons/search.svg" alt="" width={14} height={14} className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2" />
    </div>
  );
}

/** 체크 상자 + 글자 */
export function CheckField({ name, label, defaultChecked }: { name: string; label: string; defaultChecked?: boolean }) {
  return (
    <label className="flex cursor-pointer items-center gap-2.5 text-base leading-5 text-brand">
      <input type="checkbox" name={name} defaultChecked={defaultChecked} className="size-[18px] shrink-0 accent-[#6796db]" />
      {label}
    </label>
  );
}
