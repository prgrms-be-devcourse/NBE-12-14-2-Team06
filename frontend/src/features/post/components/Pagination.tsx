import Image from 'next/image';
import { cn } from '@/lib/cn';

type Props = {
  /** 0부터 시작하는 현재 페이지 */
  page: number;
  pageCount: number;
  onPageChange: (page: number) => void;
};

const BUTTON = 'grid size-10 place-items-center rounded-[17px] transition-colors disabled:cursor-not-allowed disabled:opacity-40';

/** Figma 537:3274 — 이전 / 다음 원형 버튼 */
export default function Pagination({ page, pageCount, onPageChange }: Props) {
  return (
    <nav aria-label="페이지 이동" className="flex gap-4">
      <button
        type="button"
        aria-label="이전 페이지"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
        className={cn(BUTTON, 'border border-line bg-white hover:bg-line-soft')}
      >
        {/* 에셋은 이름과 달리 오른쪽을 가리켜서, Figma(571:20441)처럼 180도 돌려 씁니다. */}
        <Image
          src="/icons/page-arrow-left.svg"
          alt=""
          width={14.85}
          height={13.7077}
          className="rotate-180"
        />
      </button>
      <button
        type="button"
        aria-label="다음 페이지"
        disabled={page >= pageCount - 1}
        onClick={() => onPageChange(page + 1)}
        className={cn(BUTTON, 'bg-brand hover:bg-brand-hover')}
      >
        <Image src="/icons/page-arrow-right.svg" alt="" width={13.9124} height={13.6701} />
      </button>
    </nav>
  );
}
