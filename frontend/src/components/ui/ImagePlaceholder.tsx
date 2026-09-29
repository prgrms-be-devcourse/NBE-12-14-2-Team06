import type { CSSProperties } from 'react';
import { cn } from '@/lib/cn';
import { ImageGlyphIcon } from './icons';

type Props = {
  /** 무엇이 들어갈 자리인지 */
  label: string;
  /** Figma 노드 ID · 원본 크기 */
  note?: string;
  /** 디자인상의 가로/세로 비율 (예: '598 / 494') */
  ratio: string;
  className?: string;
  style?: CSSProperties;
};

/**
 * Figma 일러스트(PNG) 자리를 잡아두는 컴포넌트.
 * 에셋을 받으면 next/image 의 <Image /> 로 교체하세요.
 */
export default function ImagePlaceholder({ label, note, ratio, className, style }: Props) {
  return (
    <div
      style={{ aspectRatio: ratio, ...style }}
      className={cn(
        'flex w-full flex-col items-center justify-center gap-2.5 p-4',
        'rounded-card border border-dashed border-line bg-line-soft',
        'text-center text-brand-muted',
        className,
      )}
    >
      <ImageGlyphIcon className="size-11 opacity-60" />
      <span className="text-[13px] leading-normal font-medium">
        {label}
        {note ? (
          <>
            <br />
            {note}
          </>
        ) : null}
      </span>
    </div>
  );
}
