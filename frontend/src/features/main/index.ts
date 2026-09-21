/**
 * main 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 *   ✅ import { MainPage } from '@/features/main';
 *   ❌ import MainPage from '@/features/main/components/MainPage';
 */
export { default as MainPage } from './components/MainPage';
export type { ChangeItem, Persona, UsageStep } from './types';
