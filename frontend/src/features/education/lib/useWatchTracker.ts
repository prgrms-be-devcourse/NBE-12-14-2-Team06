'use client';

import { useEffect, useState, type RefObject } from 'react';
import { recordWatchLog } from '../api';

/** 하트비트 간격. 서버는 "직전 기록 이후 흐른 시간(최대 15초) × 1.2 + 2초" 까지 인정하므로 5초 간격이면 여유 있게 들어갑니다. */
const HEARTBEAT_MS = 5_000;
/** timeupdate 한 번에 이만큼 넘게 앞으로 가면 정상 재생이 아니라 건너뛰기로 봅니다. */
const SKIP_TOLERANCE_SEC = 1;

type Options = {
  videoId: number;
  /** 서버가 인정한 재생 위치 (이어보기 시작점) */
  initialMaxWatchedSec: number;
  initialCompleted: boolean;
  /** 교육 이수 여부 (동행 매니저 프로필의 verified) */
  initialVerified: boolean;
};

export type WatchTrackerState = {
  /** 서버가 인정한 재생 위치 */
  maxWatchedSec: number;
  completed: boolean;
  /** 교육 이수 여부 */
  verified: boolean;
  /** 건너뛰기·배속 차단 등 사용자에게 알려 줄 문구 */
  notice?: string;
  /** 시청 기록 저장 실패 문구 */
  error?: string;
};

/**
 * <video> 에 붙여서 시청 기록(하트비트)을 보내고, 서버가 인정하지 않는 시청(건너뛰기·배속)을 막습니다.
 *
 * - 재생 시작·일시정지 시 한 번, 재생 중 5초마다, 영상이 끝나면, 화면을 떠날 때 현재 위치를 보냅니다.
 *   (첫 기록은 서버가 +2초까지만 인정하므로 재생을 시작하자마자 보내야 합니다.)
 * - 아직 본 적 없는 구간으로 건너뛰면 본 곳까지 되돌리고, 배속은 1배로 되돌립니다.
 * - 다른 탭으로 가면 일시정지합니다. 숨은 탭에선 타이머가 늦어져 하트비트 간격이 벌어지고, 그러면 서버가 거부합니다.
 * - 그래도 서버가 거부하면(응답 maxWatchedSec 가 보낸 위치보다 작음) 인정된 위치로 되돌립니다.
 * - 시청을 완료한 영상, 또는 이미 교육을 이수(verified)한 경우엔 기록을 보내지 않고 자유롭게 볼 수 있습니다.
 *   이수한 동행 매니저의 기록은 서버가 저장하지 않고 예전 maxWatchedSec 를 그대로 돌려주는데,
 *   이를 "거부" 로 오해해 되돌리면 하트비트마다 처음으로 돌아가게 됩니다.
 */
export function useWatchTracker(videoRef: RefObject<HTMLVideoElement | null>, { videoId, initialMaxWatchedSec, initialCompleted, initialVerified }: Options): WatchTrackerState {
  const [state, setState] = useState<WatchTrackerState>({ maxWatchedSec: initialMaxWatchedSec, completed: initialCompleted, verified: initialVerified });

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    /** 기록·제한 없이 자유롭게 보는 상태 (시청 완료했거나 교육을 이수함) */
    let free = initialCompleted || initialVerified;
    /** 화면에서 정상 재생으로 도달한 가장 먼 위치. 여기까지는 자유롭게 오갈 수 있습니다. */
    let watchedUntil = initialMaxWatchedSec;
    let timer: number | undefined;
    // 기록은 순서대로 한 건씩 보냅니다. 서버가 "직전 기록 이후 흐른 시간"으로 인정 범위를 계산해서 순서가 섞이면 안 됩니다.
    let queue = Promise.resolve();
    let disposed = false;
    /** 이 화면에서 한 번이라도 재생했는지 (떠날 때 마지막 위치를 보낼지 판단) */
    let played = false;

    const notify = (notice: string) => setState((prev) => ({ ...prev, notice }));

    /**
     * final = 화면을 떠나면서 보내는 마지막 기록.
     * 줄(queue)에 세우면 앞선 요청을 기다리는 사이 페이지가 닫힐 수 있어서 바로 보냅니다(keepalive).
     * 앞선 요청보다 먼저 도착해도 괜찮습니다. 늦게 도착한 쪽은 이미 인정된 위치보다 작은 위치라 서버가 그대로 받아 줍니다.
     * 응답은 이미 사라진 화면이라 반영하지 않습니다.
     */
    const send = (positionSec: number, { final = false } = {}) => {
      if (free) return;
      if (final) {
        recordWatchLog(videoId, positionSec, { keepalive: true }).catch(() => {});
        return;
      }
      queue = queue.then(async () => {
        if (disposed || free) return;
        try {
          const result = await recordWatchLog(videoId, positionSec);
          if (disposed) return;
          free = result.completed || result.verified;
          const rejected = !free && result.maxWatchedSec + 0.001 < positionSec;
          if (rejected) {
            watchedUntil = result.maxWatchedSec;
            video.currentTime = result.maxWatchedSec;
          }
          setState((prev) => ({
            maxWatchedSec: result.maxWatchedSec,
            completed: result.completed,
            verified: result.verified,
            notice: rejected ? '정상 시청으로 인정되지 않은 구간이 있어 마지막으로 인정된 위치로 이동했습니다.' : prev.notice,
          }));
        } catch (error) {
          if (disposed) return;
          setState((prev) => ({ ...prev, error: error instanceof Error ? error.message : '시청 기록을 저장하지 못했습니다.' }));
        }
      });
    };

    const stopTimer = () => {
      window.clearInterval(timer);
      timer = undefined;
    };

    // 이어보기: 서버가 인정한 위치부터 시작합니다. (끝에 거의 닿아 있으면 처음부터)
    const resume = () => {
      if (initialCompleted || initialMaxWatchedSec <= 0) return;
      if (initialMaxWatchedSec < video.duration - SKIP_TOLERANCE_SEC) video.currentTime = initialMaxWatchedSec;
    };

    const handleTimeUpdate = () => {
      if (free || video.seeking) return;
      const position = video.currentTime;
      if (position - watchedUntil <= SKIP_TOLERANCE_SEC) watchedUntil = Math.max(watchedUntil, position);
    };

    const handleSeeking = () => {
      if (free) return;
      if (video.currentTime > watchedUntil + SKIP_TOLERANCE_SEC) {
        video.currentTime = watchedUntil;
        notify('아직 시청하지 않은 구간으로는 건너뛸 수 없습니다.');
      }
    };

    const handleRateChange = () => {
      if (free || video.playbackRate === 1) return;
      video.playbackRate = 1;
      notify('교육 영상은 1배속으로만 시청할 수 있습니다.');
    };

    const handlePlay = () => {
      played = true;
      send(video.currentTime);
      stopTimer();
      timer = window.setInterval(() => send(video.currentTime), HEARTBEAT_MS);
    };

    const handlePause = () => {
      stopTimer();
      if (!video.ended) send(video.currentTime);
    };

    const handleEnded = () => {
      stopTimer();
      send(Number.isFinite(video.duration) ? video.duration : video.currentTime);
    };

    // 화면을 떠나면(다른 페이지로 이동·탭 닫기·새로고침) 마지막 하트비트 이후 시청분이 사라지지 않게 지금 위치를 보냅니다.
    // "지금 재생 중인지" 로 판단하지 않습니다. 페이지 이동 시 정리 함수가 불릴 때는 <video> 가 이미 화면에서 빠져
    // 브라우저가 일시정지해 둔 뒤일 수 있기 때문입니다. 같은 위치를 한 번 더 보내도 서버는 그대로 받아 줍니다.
    const sendBeforeLeave = () => {
      if (played && !video.ended) send(video.currentTime, { final: true });
    };

    const handleVisibilityChange = () => {
      if (document.hidden && !video.paused && !free) {
        video.pause();
        notify('다른 화면으로 이동해 영상을 일시정지했습니다.');
      }
    };

    if (video.readyState >= HTMLMediaElement.HAVE_METADATA) resume();
    video.addEventListener('loadedmetadata', resume);
    video.addEventListener('timeupdate', handleTimeUpdate);
    video.addEventListener('seeking', handleSeeking);
    video.addEventListener('ratechange', handleRateChange);
    video.addEventListener('play', handlePlay);
    video.addEventListener('pause', handlePause);
    video.addEventListener('ended', handleEnded);
    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('pagehide', sendBeforeLeave);

    return () => {
      // 리스너를 떼기 전에 보냅니다. 떼고 나면 <video> 가 빠질 때 나는 pause 를 받지 못합니다.
      sendBeforeLeave();
      disposed = true;
      stopTimer();
      window.removeEventListener('pagehide', sendBeforeLeave);
      video.removeEventListener('loadedmetadata', resume);
      video.removeEventListener('timeupdate', handleTimeUpdate);
      video.removeEventListener('seeking', handleSeeking);
      video.removeEventListener('ratechange', handleRateChange);
      video.removeEventListener('play', handlePlay);
      video.removeEventListener('pause', handlePause);
      video.removeEventListener('ended', handleEnded);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [videoRef, videoId, initialMaxWatchedSec, initialCompleted, initialVerified]);

  return state;
}
