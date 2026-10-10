'use client';

import Image from 'next/image';
import { useState } from 'react';
import { LiveMap, type LatLng } from '@/features/location';
import { cn } from '@/lib/cn';
import type { EscortStage } from '../../types';
import { CARD, CARD_TITLE } from './tracking';

/**
 * 지도 + 마커 (Figma 실시간 위치).
 *
 * position 이 있으면 카카오 지도에 동행 매니저의 현재 위치(와 지나온 경로)를 보여줍니다.
 * position 이 없거나 지도를 불러오지 못하면 예전 임시 화면(고정 이미지 + 화면용 마커·경로)을 그대로 보여줍니다.
 * updatedAt("최근 업데이트" 시각)은 넘겨줄 데이터가 있을 때만 표시합니다.
 * note 는 위치 공유에 문제가 있을 때(권한 거부 등) 지도 아래에 띄우는 안내입니다.
 */
export default function MapCard({
  stage,
  updatedAt,
  position,
  trail = [],
  note,
}: {
  stage: EscortStage;
  updatedAt?: string;
  position?: LatLng;
  trail?: LatLng[];
  note?: string;
}) {
  const [mapFailed, setMapFailed] = useState(false);
  const showLiveMap = !!position && !mapFailed;

  const status =
    stage === 'ready' ? (
      <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 공유 대기중</span>
    ) : stage === 'ongoing' ? (
      note ? (
        // 위치 공유에 문제가 있으면(권한 거부·전송 실패 등) 초록 "공유중" 표시를 하지 않습니다.
        <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 확인 필요</span>
      ) : position || updatedAt ? (
        <span className="flex items-center gap-3 text-base leading-5 font-semibold text-brand">
          <span className="flex items-center gap-1.5">
            <span aria-hidden="true" className="size-2 rounded-full bg-[#209d37]" />
            위치 공유중
          </span>
          {updatedAt && <span className="text-[#c0c0c2]">최근 업데이트 {updatedAt}</span>}
        </span>
      ) : (
        <span className="text-base leading-5 font-semibold text-[#c0c0c2]">위치 확인 중</span>
      )
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
        {showLiveMap ? (
          <LiveMap position={position} trail={trail} onError={() => setMapFailed(true)} />
        ) : (
          <>
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
          </>
        )}
      </div>
      {note && (
        <p role="alert" className="mt-3 text-sm font-medium text-[#b91d1d]">
          {note}
        </p>
      )}
    </section>
  );
}
