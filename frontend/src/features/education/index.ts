/**
 * education(교육 영상) 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 */
export { default as EducationListPage } from './components/EducationListPage';
export { default as EducationVideoPage } from './components/EducationVideoPage';
export { fetchEducationVideo, fetchEducationVideos, isEducationRequiredError, recordWatchLog } from './api';
export { isEducationCompleted } from './model/status';
export type { EducationVideoDto, WatchLogDto } from './types';
