import type { CSSProperties, ReactNode } from 'react';

type Props = {
  label: string;
  children: ReactNode;
  /** 라벨 칸 너비(px, 안쪽 여백 포함) */
  labelWidth?: number;
  className?: string;
  style?: CSSProperties;
};

/** "라벨 | 값" 한 줄 (Figma 높이 47 · 라벨 16px semibold · 값 14px medium) */
export default function InfoRow({ label, children, labelWidth = 90, className, style }: Props) {
  return (
    <div className={`flex min-h-[47px] items-center gap-2.5 ${className ?? ''}`} style={style}>
      <dt
        className="shrink-0 px-4 text-base leading-5 font-semibold text-brand"
        style={{ width: labelWidth }}
      >
        {label}
      </dt>
      <dd className="min-w-0 flex-1 px-4 text-sm leading-5 font-medium text-brand [overflow-wrap:anywhere]">
        {children}
      </dd>
    </div>
  );
}
