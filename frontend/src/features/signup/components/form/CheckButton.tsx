import type { ComponentProps } from 'react';

/** "중복 확인" 버튼 (Figma 564:17809) */
export default function CheckButton(props: ComponentProps<'button'>) {
  return (
    <button
      type="button"
      {...props}
      className="h-[61px] w-28 shrink-0 rounded-[20px] border border-[#eaeaea] bg-[#f5f5f5] px-[18px] py-3.5 text-base leading-5 font-semibold text-footer drop-shadow-soft transition-colors hover:bg-line-soft"
    >
      중복 확인
    </button>
  );
}
