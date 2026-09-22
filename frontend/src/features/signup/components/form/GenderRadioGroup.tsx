'use client';

import Image from 'next/image';
import { GENDER_OPTIONS } from '../../model';
import type { SignupGender } from '../../types';

type Props = {
  value: SignupGender | '';
  onChange: (value: SignupGender) => void;
  /** 이 묶음을 설명하는 라벨(FormField)의 id */
  labelId: string;
};

/** 성별 라디오 (Figma 564:17830 — 백엔드 Gender enum 에 맞춰 남성 / 여성만 둡니다) */
export default function GenderRadioGroup({ value, onChange, labelId }: Props) {
  return (
    <div role="radiogroup" aria-labelledby={labelId} className="flex flex-wrap items-center gap-x-[26px] gap-y-2">
      {GENDER_OPTIONS.map((option) => {
        const checked = value === option.value;
        return (
          <label key={option.value} className="flex cursor-pointer items-center gap-1.5">
            <input
              type="radio"
              name="gender"
              value={option.value}
              checked={checked}
              onChange={() => onChange(option.value)}
              required
              className="peer sr-only"
            />
            <span className="relative grid size-6 shrink-0 place-items-center rounded-[3px] peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-brand">
              <Image src="/icons/radio.svg" alt="" width={20} height={20} />
              {checked && <span aria-hidden="true" className="absolute size-2.5 rounded-full bg-brand" />}
            </span>
            <span className="text-base leading-5 font-semibold text-footer">{option.label}</span>
          </label>
        );
      })}
    </div>
  );
}
