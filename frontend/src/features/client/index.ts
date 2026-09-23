/**
 * client(의뢰인) 도메인의 공개 API.
 * 이 도메인 바깥(app/, 다른 features/)에서는 반드시 여기를 통해서만 import 합니다.
 */
export { default as ClientPostsPage } from './components/ClientPostsPage';
export { default as PostFormPage } from './components/PostFormPage';
export { default as PaymentPage } from './components/PaymentPage';
export { default as PaymentSuccessPage } from './components/SuccessPage';
export { default as PaymentFailPage } from './components/FailPage';
export { default as PostCompletePage } from './components/PostCompletePage';
export { default as ApplicantsPage } from './components/ApplicantsPage';
export { default as ClientTrackingPage } from './components/ClientTrackingPage';
export { default as ClientReportPage } from './components/ClientReportPage';
export { default as ClientReviewPage } from './components/ClientReviewPage';
export { fetchMyPosts } from './api';
export type { ClientPost, ClientPostStatus } from './types';
