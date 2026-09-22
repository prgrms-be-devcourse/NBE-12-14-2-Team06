import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  title: string;
  /** 제목 오른쪽 작은 버튼 문구 (예: "수정하기") */
  action?: string;
  /** action 버튼을 눌렀을 때. 없으면 버튼이 아무 동작도 하지 않습니다(TODO 표시용). */
  onAction?: () => void;
  children: ReactNode;
  /** 카드 최소 높이 등 */
  className?: string;
  /** 아래쪽 안쪽 여백 (기본 pb-[30px]) */
  paddingBottom?: string;
};

/** 마이페이지 카드: 제목 + (선택) 수정 버튼 + 내용 (Figma BasicInformation 등, 라운드 30) */
export default function InfoCard({
  title,
  action,
  onAction,
  children,
  className,
  paddingBottom = 'pb-[30px]',
}: Props) {
  return (
    <section
      className={cn('rounded-[30px] border border-line bg-white px-6 pt-8 shadow-card', paddingBottom, className)}
    >
      <div className="flex h-9 items-center justify-between gap-4">
        <h2 className="text-2xl leading-6 font-semibold text-brand">{title}</h2>
        {action && (
          <button
            type="button"
            onClick={onAction}
            className="h-[35px] shrink-0 rounded-[10px] border border-[#e6e8ec] bg-line-soft px-4 text-base leading-5 font-semibold text-brand transition-colors hover:bg-line"
          >
            {action}
          </button>
        )}
      </div>
      {children}
    </section>
  );
}
