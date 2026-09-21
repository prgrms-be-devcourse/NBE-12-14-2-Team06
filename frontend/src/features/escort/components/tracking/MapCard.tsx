import Image from 'next/image';
import { cn } from '@/lib/cn';
import type { EscortStage } from '../../types';
import { CARD, CARD_TITLE } from './tracking';

/** 지도 + 마커 (Figma 실시간 위치). 마커·경로 위치는 화면용 임시 값입니다. updatedAt 은 "최근 업데이트" 시각 */
export default function MapCard({ stage, updatedAt = '9:00' }: { stage: EscortStage; updatedAt?: string }) {
  const status =
    stage === 'ready' ? (
      <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 공유 대기중</span>
    ) : stage === 'ongoing' ? (
      <span className="flex items-center gap-3 text-base leading-5 font-semibold text-brand">
        <span className="flex items-center gap-1.5">
          <span aria-hidden="true" className="size-2 rounded-full bg-[#209d37]" />
          위치 공유중
        </span>
        <span className="text-[#c0c0c2]">최근 업데이트 {updatedAt}</span>
      </span>
    ) : (
      <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 공유 종료</span>
    );

  return (
    <section className={cn(CARD, 'px-6 pt-8 pb-6')}>
      <div className="mb-6 flex items-center justify-between gap-3">
        <h2 className={CARD_TITLE}>실시간 위치</h2>
        {status}
      </div>
      <div className="relative h-[300px] overflow-hidden rounded-[30px] bg-line-soft lg:h-[400px]">
        <Image src="/images/escort-map.png" alt="동행 경로 지도" fill sizes="700px" className="object-cover" />
        {stage !== 'ready' && (
          <svg aria-hidden="true" viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 size-full">
            <polyline
              points={stage === 'done' ? '22,72 22,48 40,48 40,30 58,30' : '22,72 22,48 40,48'}
              fill="none"
              stroke="#353e5c"
              strokeWidth="3"
              vectorEffect="non-scaling-stroke"
              strokeLinejoin="round"
            />
          </svg>
        )}
        <span className="absolute top-[30%] left-[58%] -translate-x-1/2 -translate-y-full">
          <Image src="/icons/escort/marker.svg" alt="병원" width={57} height={57} />
        </span>
        <span className="absolute top-[72%] left-[22%] grid size-11 -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full bg-[#353e5c]">
          <Image src="/icons/escort/home.svg" alt="출발지" width={20} height={20} className="brightness-0 invert" />
        </span>
      </div>
    </section>
  );
}
