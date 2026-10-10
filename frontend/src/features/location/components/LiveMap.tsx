'use client';

import { useEffect, useEffectEvent, useRef, useState } from 'react';
import { createLiveMap, type LiveMapController } from '@/lib/kakaoMap';
import type { LatLng } from '../types';

/** 지도를 불러오는 데 이만큼 걸리면 실패로 보고 임시 화면으로 돌아갑니다. */
const LOAD_TIMEOUT_MS = 10000;

/**
 * 동행 매니저의 현재 위치를 카카오 지도 위에 보여줍니다. 부모 상자(relative)를 가득 채웁니다.
 * 지도를 불러오지 못하면(키 없음·허용되지 않은 도메인·네트워크 오류) onError 를 한 번 호출합니다.
 */
export default function LiveMap({
  position,
  trail,
  onError,
}: {
  position: LatLng;
  trail: LatLng[];
  onError: () => void;
}) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [controller, setController] = useState<LiveMapController>();
  // onError 가 렌더마다 새 함수여도 지도를 다시 만들지 않도록, 효과 안에서만 쓰는 이벤트 함수로 감쌉니다.
  const handleError = useEffectEvent(onError);

  // 1. 지도를 한 번 만듭니다.
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    let ignore = false;

    const timeout = new Promise<never>((_, reject) => {
      window.setTimeout(() => reject(new Error('지도를 불러오는 시간이 너무 오래 걸립니다.')), LOAD_TIMEOUT_MS);
    });

    Promise.race([createLiveMap(container), timeout])
      .then((created) => {
        if (ignore) return;
        created.relayout();
        setController(created);
      })
      .catch(() => {
        if (!ignore) handleError();
      });

    return () => {
      ignore = true;
      container.replaceChildren();
    };
  }, []);

  // 2. 위치가 바뀔 때마다 표시와 경로를 갱신합니다.
  useEffect(() => {
    controller?.update(position, trail);
  }, [controller, position, trail]);

  return <div ref={containerRef} role="img" aria-label="동행 매니저의 실시간 위치 지도" className="absolute inset-0" />;
}
