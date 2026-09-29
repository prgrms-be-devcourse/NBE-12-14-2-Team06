import { Fragment } from 'react';
import { cn } from '@/lib/cn';

type Props = {
  title: string;
  /** 문자열 배열이면 줄바꿈해서 보여줍니다. */
  description: string | string[];
  /** 아래쪽 여백. 지정하면 기본값(mb-8 lg:mb-[50px])을 대체합니다. */
  className?: string;
  /** 설명 줄 간격. 지정하면 기본값(leading-6)을 대체합니다. */
  descriptionClassName?: string;
};

/** 섹션 공통 제목 블록 (Figma "Headings" 컴포넌트) */
export default function SectionHeading({
  title,
  description,
  className = 'mb-8 lg:mb-[50px]',
  descriptionClassName = 'leading-6',
}: Props) {
  const lines = Array.isArray(description) ? description : [description];

  return (
    <div className={cn('flex flex-col items-center gap-5 text-center', className)}>
      <h2 className="text-[28px] leading-[34px] font-extrabold text-brand lg:text-4xl lg:leading-10">
        {title}
      </h2>
      <p className={cn('text-[17px] text-brand lg:text-xl', descriptionClassName)}>
        {lines.map((line, index) => (
          <Fragment key={line}>
            {index > 0 && <br />}
            {line}
          </Fragment>
        ))}
      </p>
    </div>
  );
}
