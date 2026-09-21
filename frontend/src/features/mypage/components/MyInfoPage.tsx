'use client';

import Image from 'next/image';
import Link from 'next/link';
import { InfoRow } from '@/components/ui';
import { ACTIVITY_STATS, CLIENT_ACTIVITY_STATS, CLIENT_PROFILE, MY_PROFILE } from '../model';
import type { MyPageRole } from '../types';
import InfoCard from './InfoCard';
import MyPageShell from './MyPageShell';

const ACCOUNT_ACTIONS = ['비밀번호 변경', '이메일 변경', '회원 탈퇴'];

/**
 * 마이페이지 — 내 정보 (Figma 동행매니저_마이페이지 210:1079 · 의뢰인_마이페이지 210:746)
 *
 * ⚠️ 모의 데이터(model/profile.ts)를 보여줍니다. 내 정보 API(GET /api/v1/users/profile) 연결 전입니다.
 */
export default function MyInfoPage({ role = 'escort' }: { role?: MyPageRole }) {
  const isClient = role === 'client';
  const profile = isClient ? CLIENT_PROFILE : MY_PROFILE;
  const stats = isClient ? CLIENT_ACTIVITY_STATS : ACTIVITY_STATS;

  return (
    <MyPageShell role={role}>
      <div className="flex max-w-[858px] flex-col gap-[22px] pt-8 lg:pt-[41px]">
        {/* 프로필 요약 */}
        <section className="flex flex-col gap-8 rounded-[30px] border border-line-soft bg-white p-8 shadow-card sm:flex-row lg:min-h-[210px]">
          <div className="grid size-[101px] shrink-0 place-items-center self-center rounded-[30px] bg-line-soft sm:self-start lg:self-center">
            <Image src="/icons/avatar.svg" alt="" width={36} height={38} />
          </div>
          <div className="flex min-w-0 flex-1 flex-col gap-3 lg:gap-[9px]">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2.5">
                <p className="text-2xl leading-6 font-semibold text-brand">{profile.name}</p>
                <span className="inline-flex h-[35px] items-center rounded-[10px] bg-brand px-[14px] text-base font-semibold whitespace-nowrap text-white">
                  {profile.roleLabel}
                </span>
              </div>
              {/* TODO: 프로필 수정 연결 */}
              <button
                type="button"
                className="h-[37px] rounded-[10px] border border-[#e6e8ec] bg-line-soft px-[17px] text-base leading-5 font-semibold text-brand transition-colors hover:bg-line"
              >
                프로필 수정
              </button>
            </div>
            <p className="text-base leading-6 font-semibold text-brand-muted underline">{profile.email}</p>
            <p className="text-base leading-6 font-semibold text-brand-muted">{profile.phone}</p>
            <p className="text-base leading-6 font-semibold text-brand-muted">{profile.address}</p>
          </div>
        </section>

        <div className="grid grid-cols-[minmax(0,1fr)] gap-[22px] lg:grid-cols-[375px_1fr]">
          {/* 기본 정보 */}
          <InfoCard title="기본 정보" action="수정하기" className="lg:min-h-[527px]">
            <dl className="mt-[30px] flex flex-col gap-[3px]">
              <InfoRow label="아이디">{profile.username}</InfoRow>
              <InfoRow label="이메일">{profile.email}</InfoRow>
              <InfoRow label="실명">{profile.name}</InfoRow>
              <InfoRow label="생년월일" labelWidth={92}>{profile.birthDate}</InfoRow>
              <InfoRow label="성별" labelWidth={92}>{profile.gender}</InfoRow>
              <InfoRow label="전화번호" labelWidth={92}>{profile.phone}</InfoRow>
              <InfoRow label="거주 지역" labelWidth={92}>{profile.address}</InfoRow>
              <InfoRow label="역할" labelWidth={92}>{profile.roleLabel}</InfoRow>
            </dl>
          </InfoCard>

          <div className="flex flex-col gap-[22px]">
            {/* 추가 정보 */}
            <InfoCard title="추가 정보" action="수정하기" paddingBottom="pb-6" className="lg:min-h-[256px]">
              {profile.guardian ? (
                <dl className="mt-[9px] flex flex-col gap-[3px]">
                  <InfoRow label="보호자 실명" labelWidth={138}>{profile.guardian.name}</InfoRow>
                  <InfoRow label="보호자 전화번호" labelWidth={138}>{profile.guardian.phone}</InfoRow>
                  <InfoRow label="특이사항" labelWidth={138}>{profile.guardian.careNote}</InfoRow>
                </dl>
              ) : (
                <div className="px-4 pt-[26px]">
                  <p className="text-base leading-5 font-semibold text-brand">자기소개</p>
                  <p className="mt-[21px] text-xs leading-[15px] font-semibold text-brand">
                    {profile.intro?.map((line) => (
                      <span key={line} className="block">
                        {line}
                      </span>
                    ))}
                  </p>
                </div>
              )}
            </InfoCard>

            {/* 계정 관리 */}
            <InfoCard title="계정 관리" paddingBottom="pb-6" className="lg:min-h-[250px]">
              <ul className="mt-[9px] flex flex-col gap-[3px]">
                {ACCOUNT_ACTIONS.map((label) => (
                  // TODO: 각 기능이 정해지면 연결하세요.
                  <li key={label}>
                    <button
                      type="button"
                      className="flex h-[47px] w-full items-center justify-between px-4 text-left text-base leading-5 font-semibold text-brand transition-colors hover:text-brand-hover"
                    >
                      {label}
                      <Image src="/icons/chevron-right.svg" alt="" width={24} height={24} />
                    </button>
                  </li>
                ))}
              </ul>
            </InfoCard>
          </div>
        </div>

        {/* 최근 활동 요약 */}
        <section className="rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:min-h-[207px] lg:px-[37px]">
          <div className="flex h-[47px] items-center justify-between">
            <h2 className="text-2xl leading-6 font-semibold text-brand">최근 활동 요약</h2>
            <Link
              href={isClient ? '/client/posts' : '/mypage/applications'}
              className="flex items-center gap-4 text-base leading-5 font-semibold text-brand transition-colors hover:text-brand-hover"
            >
              전체 보기
              <Image src="/icons/arrow-right.svg" alt="" width={24} height={24} />
            </Link>
          </div>
          <ul className="mt-2 grid grid-cols-2 gap-4 lg:grid-cols-4 lg:gap-[22px]">
            {stats.map((stat) => (
              <li
                key={stat.label}
                className="flex h-[83px] items-center gap-3 rounded-[20px] border border-line-soft bg-white px-4 shadow-card"
              >
                <Image src={stat.icon} alt="" width={50} height={50} className="size-[50px] shrink-0" />
                <div className="min-w-0 flex-1">
                  <p className="text-xs leading-3 font-semibold text-brand">{stat.label}</p>
                  <p className="mt-1.5 flex items-center justify-between text-xl leading-6 font-bold text-brand">
                    {stat.count}
                    <Image src="/icons/activity-chevron.svg" alt="" width={12} height={12} />
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </MyPageShell>
  );
}
