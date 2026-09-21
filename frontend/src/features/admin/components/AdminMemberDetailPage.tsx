'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { AppShell } from '@/components/layout';
import { InfoRow } from '@/components/ui';
import { cn } from '@/lib/cn';
import { MOCK_ADMIN } from '@/lib/mockSession';
import { formatDotDate } from '../lib/date';
import { ROLE_LABEL, getMemberDetail } from '../model';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card';
const CARD_TITLE = 'text-2xl leading-6 font-semibold text-brand';

/**
 * 관리자 — 회원 상세 (Figma 571:20504 관리자_회원상세)
 *
 * 이 화면에는 회원 관리 목록과 달리 왼쪽 관리자 메뉴가 없습니다(디자인 그대로).
 *
 * ⚠️ 모의 데이터(model/memberDetail.ts)를 보여줍니다.
 *    회원 상세 API(GET /api/v1/admin/users/{userId}) 연결 전입니다.
 */
export default function AdminMemberDetailPage() {
  const params = useParams<{ memberId: string }>();
  const member = getMemberDetail(params.memberId);

  if (!member) {
    return (
      <AppShell user={MOCK_ADMIN}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">회원을 찾을 수 없습니다.</p>
          <Link
            href="/admin/members"
            className="mx-auto mt-8 flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand transition-colors hover:bg-line-soft"
          >
            목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  return (
    <AppShell user={MOCK_ADMIN}>
      <section className="bg-white py-8 lg:py-[50px]">
        <div className="mx-auto flex w-full max-w-[1066px] flex-col gap-[22px] px-4 lg:px-0">
          {/* 목록으로 · 회원 정보 수정 (Figma 571:20849) */}
          <div className="flex flex-wrap justify-end gap-4">
            <Link
              href="/admin/members"
              className="flex h-[45px] w-[155px] items-center justify-center gap-[6.48px] rounded-[24.3px] border border-line bg-white text-base font-semibold text-brand transition-colors hover:bg-line-soft"
            >
              {/* 아래를 향한 꺾쇠를 90° 돌려 왼쪽 화살표로 씁니다 (Figma 571:20851). */}
              <Image src="/icons/back-chevron.svg" alt="" width={11.465} height={6.4075} className="rotate-90" />
              목록으로
            </Link>
            {/* TODO: 회원 정보 수정 화면/API 가 생기면 연결하세요. */}
            <button
              type="button"
              className="h-[45px] w-[155px] rounded-[24.3px] bg-brand text-base font-semibold text-white transition-colors hover:bg-brand-hover"
            >
              회원 정보 수정
            </button>
          </div>

          {/* 프로필 요약 (Figma 571:21101) */}
          <section className="flex flex-col items-center gap-8 rounded-[30px] border border-line-soft bg-white p-8 shadow-card lg:min-h-[210px] lg:flex-row lg:gap-0">
            <div className="grid size-[101px] shrink-0 place-items-center rounded-[30px] bg-line-soft">
              <Image src="/icons/avatar.svg" alt="" width={36} height={38} />
            </div>

            <div className="flex min-w-0 flex-1 flex-col lg:ml-8 lg:max-w-[538px]">
              <div className="flex flex-wrap items-center gap-2.5">
                <p className="text-2xl leading-6 font-semibold text-brand">{member.name}</p>
                <span className="inline-flex h-[35px] min-w-[74px] items-center justify-center rounded-[10px] bg-brand px-2.5 text-base leading-[22px] font-semibold whitespace-nowrap text-white">
                  {ROLE_LABEL[member.role]}
                </span>
              </div>
              <div className="mt-[13px] flex flex-col gap-[11px] text-base leading-6 font-semibold text-brand-muted">
                <a href={`mailto:${member.email}`} className="w-fit underline [overflow-wrap:anywhere]">
                  {member.email}
                </a>
                <p>{member.phone}</p>
                <p>{member.address}</p>
              </div>
            </div>

            <div aria-hidden="true" className="hidden h-[110px] w-px bg-[#e6e8ec] opacity-50 lg:block" />

            <dl className="flex w-full flex-col gap-2.5 lg:ml-8 lg:w-[298px] lg:shrink-0">
              {[
                { label: '회원 상태', value: member.status },
                { label: '회원 가입일', value: formatDotDate(member.joinedAt) },
              ].map((row) => (
                <div key={row.label} className="flex min-h-9 items-center">
                  <dt className="w-[150px] shrink-0 px-3 text-base leading-6 font-semibold text-brand">
                    {row.label}
                  </dt>
                  <dd className="min-w-0 px-3 text-base leading-6 font-semibold text-brand-muted">{row.value}</dd>
                </div>
              ))}
            </dl>
          </section>

          <div className="grid grid-cols-[minmax(0,1fr)] gap-[22px] lg:grid-cols-[480px_564px]">
            {/* 기본 정보 (Figma 571:21130) */}
            <section className={cn(CARD, 'lg:min-h-[527px]')}>
              <h2 className={CARD_TITLE}>기본 정보</h2>
              <dl className="mt-[30px] flex flex-col gap-[3px]">
                <InfoRow label="아이디">{member.id}</InfoRow>
                <InfoRow label="이메일">{member.email}</InfoRow>
                <InfoRow label="실명">{member.name}</InfoRow>
                <InfoRow label="생년월일" labelWidth={92}>{member.birthDate}</InfoRow>
                <InfoRow label="성별" labelWidth={92}>{member.gender}</InfoRow>
                <InfoRow label="전화번호" labelWidth={92}>{member.phone}</InfoRow>
                <InfoRow label="거주 지역" labelWidth={92}>{member.address}</InfoRow>
                <InfoRow label="역할" labelWidth={92}>{ROLE_LABEL[member.role]}</InfoRow>
              </dl>
            </section>

            <div className="flex flex-col gap-[21px]">
              {/* 추가 정보 (Figma 571:21180) */}
              <section className={cn(CARD, 'lg:min-h-[256px]')}>
                <h2 className={CARD_TITLE}>추가 정보</h2>
                <dl className="mt-[9px] flex flex-col gap-[3px]">
                  <InfoRow label="보호자 실명" labelWidth={135}>{member.guardianName}</InfoRow>
                  <InfoRow label="보호자 전화번호" labelWidth={135}>{member.guardianPhone}</InfoRow>
                  <InfoRow label="특이사항" labelWidth={135}>{member.careNote}</InfoRow>
                </dl>
              </section>

              {/* 최근 활동 (Figma 571:21204) */}
              <section className={cn(CARD, 'lg:min-h-[255px]')}>
                <h2 className={CARD_TITLE}>최근 활동</h2>
                <dl className="mt-[9px] flex flex-col gap-[3px]">
                  {member.recent.map((item) => (
                    <div key={item.label} className="flex min-h-[47px] items-center gap-2.5">
                      <dt className="w-[109px] shrink-0 px-4 text-base leading-5 font-semibold text-brand">
                        {item.label}
                      </dt>
                      <dd className="min-w-0 flex-1 px-4 text-sm leading-5 font-semibold text-brand-muted [overflow-wrap:anywhere]">
                        {item.detail}
                      </dd>
                      <dd className="shrink-0 px-4 text-base leading-5 font-semibold whitespace-nowrap text-brand-muted">
                        {item.date}
                      </dd>
                    </div>
                  ))}
                </dl>
              </section>
            </div>
          </div>

          {/* 최근 활동 요약 (Figma 571:21231) */}
          <section className={cn(CARD, 'lg:min-h-[207px]')}>
            <div className="flex min-h-[47px] flex-wrap items-center justify-between gap-2">
              <h2 className={CARD_TITLE}>최근 활동 요약</h2>
              {/* TODO: 회원별 활동 목록 화면이 생기면 Link 로 바꾸세요. */}
              <button
                type="button"
                className="flex items-center gap-4 px-4 text-base leading-5 font-semibold text-brand transition-colors hover:text-brand-hover"
              >
                전체 보기
                <Image src="/icons/arrow-right.svg" alt="" width={24} height={24} />
              </button>
            </div>
            <ul className="mt-[3px] grid grid-cols-2 gap-[22px] lg:grid-cols-4">
              {member.activity.map((stat) => (
                <li
                  key={stat.label}
                  className="flex h-[83px] items-center gap-3 rounded-[17px] border-[0.5px] border-line-soft bg-white px-5 shadow-card"
                >
                  <Image
                    src={stat.icon}
                    alt=""
                    width={50.694}
                    height={50.694}
                    className="size-[50.694px] shrink-0"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="text-sm leading-3 font-semibold text-brand">{stat.label}</p>
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
      </section>
    </AppShell>
  );
}
