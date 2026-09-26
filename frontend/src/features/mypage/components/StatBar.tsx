import Image from 'next/image';
type Item = { icon: string; label: string; value: string };

/** 위쪽 요약 카드 (Figma "정산 금액 · 정산 대기 · 정산 완료", "리뷰 · 리뷰 평점") */
export default function StatBar({ items }: { items: Item[] }) {
  return (
      <ul
          className={`grid w-full max-w-[910px] grid-cols-1 rounded-[30px] border border-line-soft bg-white px-8 py-6 shadow-card lg:min-h-[107px] lg:px-10 ${
              items.length === 2 ? 'lg:grid-cols-2' : 'lg:grid-cols-3'
          }`}
      >
        {items.map((item, index) => (
            <li
                key={item.label}
                className={`flex items-center gap-5 py-3 lg:py-0 ${
                    index > 0 ? 'lg:border-l lg:border-[#e6e8ec] lg:pl-10' : ''
                }`}
            >
              <Image
                  src={item.icon}
                  alt=""
                  width={50}
                  height={50}
                  className="size-[50px] shrink-0"
              />

              <div className="flex flex-col gap-3.5 text-left text-brand">
        <span className="text-base leading-4 font-semibold">
          {item.label}
        </span>
                <span className="text-2xl leading-6 font-bold">
          {item.value}
        </span>
              </div>
            </li>
        ))}
      </ul>
  );
}
