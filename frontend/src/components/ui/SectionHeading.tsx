import { cn } from '@/lib/cn';

type Props = {
  title: string;
  description: string;
  /** 아래쪽 여백. 지정하면 기본값(mb-8 lg:mb-[50px])을 대체합니다. */
  className?: string;
};

/** 섹션 공통 제목 블록 (Figma "Headings" 컴포넌트) */
export default function SectionHeading({
  title,
  description,
  className = 'mb-8 lg:mb-[50px]',
}: Props) {
  return (
    <div className={cn('flex flex-col items-center gap-5 text-center', className)}>
      <h2 className="text-[28px] leading-[34px] font-extrabold text-brand lg:text-4xl lg:leading-10">
        {title}
      </h2>
      <p className="text-[17px] leading-6 text-brand lg:text-xl">{description}</p>
    </div>
  );
}
