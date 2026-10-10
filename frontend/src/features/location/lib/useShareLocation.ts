'use client';

import { useEffect, useState } from 'react';
import { sendLocation } from '../api';
import { formatClock } from '../model/mapper';
import type { LatLng, ShareStatus } from '../types';

/** 서버로 위치를 보내는 간격(밀리초). 기기가 가만히 있어도 이 간격마다 마지막 위치를 다시 보냅니다. */
const SEND_INTERVAL_MS = 5000;
/** 전송이 이만큼 연달아 실패하면 화면에 알립니다. (한두 번은 네트워크 흔들림일 수 있어 참습니다) */
const MAX_SILENT_FAILURES = 3;

type ShareState = {
  status: ShareStatus;
  /** 이 기기에서 마지막으로 측정한 위치 */
  position?: LatLng;
  /** 서버에 마지막으로 보낸 시각 "오후 3:25:10" */
  lastSentLabel?: string;
  /** 사용자에게 보여줄 안내 (문제가 있을 때만) */
  message?: string;
};

const IDLE: ShareState = { status: 'idle' };

/** 위치 권한 거부 시 안내. 한 번 거부하면 브라우저가 다시 묻지 않아서 설정 방법을 알려줍니다. */
const DENIED_MESSAGE =
  '위치 권한이 거부되어 위치를 공유할 수 없습니다. 브라우저 주소창의 사이트 설정에서 위치를 허용한 뒤 새로고침해 주세요.';

/**
 * 동행 매니저 기기의 현재 위치를 측정해 서버로 보냅니다.
 *
 * enabled 가 true 인 동안(동행 중)에만 동작합니다. 위치는 브라우저의 위치 권한을 허용해야 받을 수 있고,
 * HTTPS(또는 localhost) 에서만 동작합니다. 화면이 꺼지거나 다른 탭으로 가면 브라우저가 전송을 늦추거나
 * 멈출 수 있습니다.
 */
export function useShareLocation(applicationId: number, enabled: boolean) {
  const [state, setState] = useState<ShareState>(IDLE);

  useEffect(() => {
    if (!enabled || !Number.isFinite(applicationId)) return;

    let ignore = false;
    let inFlight = false;
    let failures = 0;
    let latest: { lat: number; lng: number; accuracy: number } | undefined;
    let firstFixSent = false;

    const update = (patch: Partial<ShareState>) => {
      if (!ignore) setState((prev) => ({ ...prev, ...patch }));
    };

    const send = async () => {
      if (!latest || inFlight) return;
      inFlight = true;
      try {
        await sendLocation(applicationId, latest);
        failures = 0;
        update({ status: 'sharing', lastSentLabel: formatClock(new Date()), message: undefined });
      } catch (error) {
        failures += 1;
        if (failures >= MAX_SILENT_FAILURES) {
          update({
            status: 'error',
            message: error instanceof Error ? error.message : '위치를 서버로 보내지 못했습니다.',
          });
        }
      } finally {
        inFlight = false;
      }
    };

    if (!('geolocation' in navigator)) {
      // 이 기기·브라우저는 위치 기능이 없습니다. (효과 안에서 바로 상태를 바꾸지 않으려고 한 박자 늦춥니다.)
      queueMicrotask(() => update({ status: 'unsupported', message: '이 기기에서는 위치 공유를 지원하지 않습니다.' }));
      return () => {
        ignore = true;
      };
    }

    // 가만히 서 있으면 위치 변화 이벤트가 오지 않으므로, 일정 간격마다 마지막 위치를 다시 보냅니다.
    // (첫 위치를 받기 전에는 보낼 위치가 없어 send 가 바로 끝납니다.)
    const timer = window.setInterval(() => void send(), SEND_INTERVAL_MS);

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        latest = { lat: pos.coords.latitude, lng: pos.coords.longitude, accuracy: pos.coords.accuracy };
        const position = { lat: latest.lat, lng: latest.lng };
        // 이미 전송 중(sharing)이거나 오류 안내 중일 때는 상태를 건드리지 않고 위치만 갱신합니다. (깜빡임 방지)
        if (!ignore) {
          setState((prev) => ({ ...prev, position, status: prev.status === 'idle' ? 'starting' : prev.status }));
        }

        // 처음 위치를 받은 순간 바로 한 번 보내, 의뢰인이 5초를 기다리지 않게 합니다.
        if (!firstFixSent) {
          firstFixSent = true;
          void send();
        }
      },
      (error) => {
        if (error.code === error.PERMISSION_DENIED) {
          // 거부된 상태에서는 더 보낼 위치도 없습니다. 전송을 멈춥니다.
          window.clearInterval(timer);
          update({ status: 'denied', message: DENIED_MESSAGE });
        } else {
          // 일시적으로 위치를 못 잡은 경우입니다. 브라우저가 계속 다시 시도하므로 안내만 남깁니다.
          update({ status: 'error', message: '현재 위치를 가져오지 못했습니다. 잠시 후 다시 시도합니다.' });
        }
      },
      { enableHighAccuracy: true, maximumAge: SEND_INTERVAL_MS, timeout: 20000 },
    );

    return () => {
      ignore = true;
      navigator.geolocation.clearWatch(watchId);
      window.clearInterval(timer);
    };
  }, [applicationId, enabled]);

  return enabled ? state : IDLE;
}
