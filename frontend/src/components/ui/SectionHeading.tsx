type Props = { title: string; description: string };

/** 섹션 공통 제목 블록 (Figma "Headings" 컴포넌트) */
export default function SectionHeading({ title, description }: Props) {
  return (
    <div className="mb-8 flex flex-col items-center gap-5 text-center lg:mb-[50px]">
      <h2 className="text-[28px] leading-[34px] font-extrabold text-brand lg:text-4xl lg:leading-10">
        {title}
      </h2>
      <p className="text-[17px] leading-6 text-brand lg:text-xl">{description}</p>
    </div>
  );
}
