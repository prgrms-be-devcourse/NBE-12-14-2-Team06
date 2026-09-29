import type { Persona } from '../types';

export const PERSONAS: Persona[] = [
  {
    id: 'senior',
    tabTitle: '어르신',
    tabDesc: ['혼자 병원 가기 어렵지만', '도움이 필요한 어르신께'],
    headline: '“혼자 병원 가기 어려운 어르신”',
    body: ['혼자 병원 가기 어려운 어르신,', '동행 매니저가 함께 이동합니다!'],
    image: '/images/signup/role-escort.png',
    imageAlt: '동행 매니저가 휠체어를 밀어주는 모습',
  },
  {
    id: 'guardian',
    tabTitle: '보호자',
    tabDesc: ['부모님이 혼자 병원 가실 때', '걱정되는 보호자분들께'],
    // ⚠️ 디자인에 어르신 탭 콘텐츠만 있어 임시 카피입니다.
    headline: '“부모님 병원길이 걱정되는 보호자”',
    body: ['멀리 있어 함께 가지 못해도 괜찮아요.', '동행부터 진료 결과까지 알려드립니다.'],
    image: '/images/guardian.png',
    imageAlt: '보호자 탭 이미지',
  },
  {
    id: 'manager',
    tabTitle: '동행 매니저',
    tabDesc: ['고소득꿀알바 찾는 분들께'],
    // ⚠️ 임시 카피
    headline: '“시간을 자유롭게 쓰고 싶은 동행 매니저”',
    body: ['원하는 날짜와 지역의 공고를 골라 지원하세요.', '검증 절차를 거쳐 어르신과 매칭됩니다.'],
    image: '/images/manager.png',
    imageAlt: '동행 매니저 탭 이미지',
  },
];
