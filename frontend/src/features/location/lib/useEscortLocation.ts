'use client';

import { useEffect, useState } from 'react';
import { fetchLocation } from '../api';
import { toEscortLocation } from '../model/mapper';
import type { EscortLocation, LatLng } from '../types';

/** 동행 매니저의 위치를 서버에 물어보는 간격(밀리초) */
const POLL_INTERVAL_MS = 5000;
/** 지나온 경로 선에 남겨 둘 점의 최대 개수 (오래 열어 둬도 메모리가 늘지 않게) */
const MAX_TRAIL_POINTS = 300;

type PollState = {
  /** 이 상태가 어느 지원(동행)의 것인지. 다른 동행으로 옮겨가면 이전 값을 쓰지 않습니다. */
  key?: number;
  location?: EscortLocation;
  /** 이 화면을 열어 둔 동안 받은 위치를 이은 경로 */
  trail: LatLng[];
  error?: string;
};

const EMPTY_TRAIL: LatLng[] = [];

/**
 * 의뢰인 화면에서 동행 매니저의 최근 위치를 일정 간격으로 물어봅니다. (폴링)
 *
 * enabled 가 true 인 동안(동행 중)에만 요청합니다. 화면이 가려져 있는 동안은 요청을 건너뛰고,
 * 다시 보이는 순간 바로 한 번 물어봅니다.
 */
export function useEscortLocation(applicationId: number, enabled: boolean) {
  const [state, setState] = useState<PollState>({ trail: EMPTY_TRAIL });

  useEffect(() => {
    if (!enabled || !Number.isFinite(applicationId)) return;

    let ignore = false;
    let inFlight = false;

    const poll = () => {
      // 이전 요청이 아직 안 끝났거나, 화면이 가려져 있으면 이번 차례는 건너뜁니다.
      if (inFlight || document.visibilityState === 'hidden') return;
      inFlight = true;

      fetchLocation(applicationId)
        .then((dto) => {
          if (ignore) return;
          setState((prev) => {
            const base: PollState = prev.key === applicationId ? prev : { key: applicationId, trail: EMPTY_TRAIL };
            // 아직 위치가 올라오지 않았습니다. 이전 값은 그대로 두고 오류만 지웁니다.
            if (!dto) return { ...base, error: undefined };

            const location = toEscortLocation(dto);
            const last = base.trail[base.trail.length - 1];
            const moved = !last || last.lat !== location.position.lat || last.lng !== location.position.lng;
            const trail = moved ? [...base.trail, location.position].slice(-MAX_TRAIL_POINTS) : base.trail;
            return { key: applicationId, location, trail, error: undefined };
          });
        })
        .catch((error: unknown) => {
          if (ignore) return;
          const message = error instanceof Error ? error.message : '동행 매니저의 위치를 불러오지 못했습니다.';
          setState((prev) => ({
            ...(prev.key === applicationId ? prev : { trail: EMPTY_TRAIL }),
            key: applicationId,
            error: message,
          }));
        })
        .finally(() => {
          inFlight = false;
        });
    };

    const onVisibilityChange = () => {
      if (document.visibilityState === 'visible') poll();
    };

    poll();
    const timer = window.setInterval(poll, POLL_INTERVAL_MS);
    document.addEventListener('visibilitychange', onVisibilityChange);

    return () => {
      ignore = true;
      window.clearInterval(timer);
      document.removeEventListener('visibilitychange', onVisibilityChange);
    };
  }, [applicationId, enabled]);

  const active = enabled && state.key === applicationId;
  return {
    location: active ? state.location : undefined,
    trail: active ? state.trail : EMPTY_TRAIL,
    error: active ? state.error : undefined,
  };
}
