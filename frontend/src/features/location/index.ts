/**
 * location(실시간 위치) 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 */
export { sendLocation, fetchLocation } from './api';
export { useShareLocation } from './lib/useShareLocation';
export { useEscortLocation } from './lib/useEscortLocation';
export { default as LiveMap } from './components/LiveMap';
export type { EscortLocationDto, SendLocationRequest, EscortLocation, LatLng, ShareStatus } from './types';
