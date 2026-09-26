type Props = {
  /** 0 ~ 100 */
  percent: number;
  label: string;
};

/** 시청 진행 막대 (목록 카드 · 시청 화면 공용) */
export default function WatchProgressBar({ percent, label }: Props) {
  return (
    <div className="flex w-full flex-col gap-1.5">
      <div className="flex items-center justify-between text-xs leading-4 font-semibold text-brand">
        <span>{label}</span>
        <span>{percent}%</span>
      </div>
      <div
        role="progressbar"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={percent}
        className="h-2 w-full overflow-hidden rounded-full bg-line-soft"
      >
        <div className="h-full rounded-full bg-[#6796db] transition-[width] duration-300" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}
