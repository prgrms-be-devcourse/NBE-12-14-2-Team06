'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import type { ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow } from '@/components/ui';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT, MOCK_USER } from '@/lib/mockSession';
import { daysFromNow, formatFullDate } from '../lib/date';
import { getPostDetail } from '../model';
import type { LabelTone, PostBadge } from '../types';
import StatusLabel from './StatusLabel';

const BADGE: Record<PostBadge, { text: string; tone: LabelTone }> = {
  new: { text: '신규', tone: 'green' },
  closing: { text: '오늘 마감', tone: 'red' },
  open: { text: '모집 중', tone: 'blue' },
};

const CARD = 'rounded-[30px] border border-line bg-white shadow-card';
const CARD_TITLE = 'text-2xl leading-6 font-semibold text-brand';
const BUTTON =
  'flex items-center justify-center rounded-[25px] px-6 font-semibold transition-colors';

type SectionProps = {
  title: string;
  children: ReactNode;
  /** 카드 높이·안쪽 여백 조정 */
  className?: string;
  /** 제목과 내용 사이 간격 (기본 mb-5) */
  titleGap?: string;
};

/** 제목 + 내용으로 이루어진 상세 카드. 높이는 Figma 카드 높이를 최소값으로 맞춥니다. */
function Section({ title, children, className, titleGap = 'mb-5' }: SectionProps) {
  return (
    <section className={cn(CARD, 'px-6 pt-7 pb-6 lg:px-[35px]', className)}>
      <h2 className={cn(CARD_TITLE, titleGap)}>{title}</h2>
      {children}
    </section>
  );
}

type Props = {
  /** 누가 보는지. 동행 매니저는 "지원하기", 의뢰인(작성자)은 "수정하기" 버튼이 나옵니다. */
  viewer?: 'common' | 'escort' | 'client';
};

/**
 * 공고 상세 — Figma 동행 매니저_공고 상세 225:1146 · 의뢰인_공고 상세 521:2254
 *
 * ⚠️ 모의 데이터(model/posts.ts)를 보여줍니다. 상세 API(GET /api/v1/posts/{postId}) 연결 전입니다.
 */
export default function PostDetailPage({ viewer = 'common' }: Props) {
  const params = useParams<{ postId: string }>();
  const post = getPostDetail(Number(params.postId));

  const isClient = viewer === 'client';

  const user =
      viewer === 'client'
          ? MOCK_CLIENT
          : viewer === 'escort'
              ? MOCK_USER
              : undefined;

  const listHref =
      viewer === 'client'
          ? '/client/posts'
          : viewer === 'escort'
              ? '/escort/posts'
              : '/posts';

  if (!post) {
    return (
      <AppShell user={user}>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">공고를 찾을 수 없습니다.</p>
          <Link href={listHref} className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-xl text-brand')}>
            목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  const label = isClient ? { text: '모집 중', tone: 'purple' as const } : BADGE[post.badge];
  const date = formatFullDate(daysFromNow(post.startsInDays));
  const duration = `약 ${post.hours}시간`;
  const pay = `시급 ${post.hourlyPay.toLocaleString()}원`;
  const location = `${post.region} ${post.district}`;

  const editHref = `/client/posts/${post.id}/edit`;

  return (
    <AppShell user={user}>
      <section className="bg-white py-[50px]">
        <Container width="wide" className="grid items-start gap-[22px] lg:grid-cols-[858px_396px] lg:justify-center">
          <div className="flex min-w-0 flex-col gap-[22px]">
            {/* 제목 */}
            <section className="flex flex-col items-center gap-6 rounded-[30px] border border-line-soft bg-white px-6 py-5 shadow-card sm:flex-row sm:px-8 lg:min-h-[210px]">
              <Image src="/icons/image-placeholder.svg" alt="" width={100} height={100} className="size-[100px] shrink-0" />
              <div className="flex min-w-0 flex-1 flex-col gap-[15px]">
                <div className="flex items-center justify-between gap-3">
                  <StatusLabel tone={label.tone} size="large">
                    {label.text}
                  </StatusLabel>
                  <span className="text-base leading-4 font-semibold whitespace-pre text-[#c0c0c2]">
                    {`등록일   ${post.postedAt}`}
                  </span>
                </div>
                <h1 className="text-2xl leading-6 font-semibold text-brand">{post.title}</h1>
                <p className="flex flex-wrap items-center gap-x-[50px] gap-y-1 text-base leading-5 font-semibold text-brand">
                  {post.hospitalName}
                  <span className="flex items-center gap-[9px]">
                    <Image src="/icons/pin.svg" alt="" width={15} height={18} />
                    {location}
                  </span>
                </p>
                <p className="text-base leading-5 font-medium text-brand">{post.description.join('   ')}</p>
              </div>
            </section>

            {/* 기본 정보 */}
            <Section title="기본 정보" titleGap="mb-[21px]" className="lg:min-h-[290px] lg:px-6 lg:pb-4">
              <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="병원명" labelWidth={90}>{post.hospitalName}</InfoRow>
                  <InfoRow label="진료과" labelWidth={90}>{post.department}</InfoRow>
                  <InfoRow label="날짜" labelWidth={90}>{date}</InfoRow>
                  <InfoRow label="시간" labelWidth={92}>{post.startTime}</InfoRow>
                </div>
                <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-40" />
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="예상 소요 시간" labelWidth={124}>{duration}</InfoRow>
                  <InfoRow label="지역" labelWidth={124}>{location}</InfoRow>
                  <InfoRow label="시급/보수" labelWidth={124}>{pay}</InfoRow>
                  <InfoRow label="이동수단" labelWidth={124}>{post.transport}</InfoRow>
                </div>
              </dl>
            </Section>

            {/* 공고 설명 */}
            <Section title="공고 설명" className="lg:min-h-[195px]">
              <div className="text-base leading-6 font-semibold text-brand">
                {post.details.map((line) => (
                  <p key={line}>{line}</p>
                ))}
              </div>
            </Section>

            {/* 요청사항 / 특이사항 */}
            <Section title="요청사항 / 특이사항" titleGap="mb-[26px]" className="lg:min-h-[239px]">
              <ul className="flex flex-col gap-[7px] px-0.5">
                {post.requests.map((request) => (
                  <li key={request} className="flex items-center gap-[15px] text-base leading-5 font-semibold text-brand">
                    <Image src="/icons/check-circle-fill.svg" alt="" width={20} height={20} className="shrink-0" />
                    {request}
                  </li>
                ))}
              </ul>
            </Section>

            {/* 의뢰인 정보 */}
            <Section title="의뢰인 정보" titleGap="mb-[22px]" className="lg:min-h-[228px]">
              <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="의뢰인 유형" labelWidth={138}>{post.clientType}</InfoRow>
                  <InfoRow label="보호자 동행 여부" labelWidth={138}>{post.withGuardian}</InfoRow>
                </div>
                <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-[70px]" />
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="성별 선호" labelWidth={138}>{post.genderPreference}</InfoRow>
                  <InfoRow label="간단한 소개" labelWidth={138} className="items-start">
                    {post.clientIntro.map((line) => (
                      <span key={line} className="block">
                        {line}
                      </span>
                    ))}
                  </InfoRow>
                </div>
              </dl>
            </Section>

            {/* 지원 전 안내 */}
            <section className={cn(CARD, 'flex flex-col gap-[30px] px-6 pt-8 pb-5 lg:min-h-[194px] lg:flex-row lg:px-10')}>
              <h2 className={cn(CARD_TITLE, 'shrink-0 lg:w-[116px]')}>지원 전 안내</h2>
              <div className="flex min-h-[134px] flex-1 items-center gap-4 rounded-[30px] border border-line bg-line-soft px-6 lg:px-[17px]">
                <Image src="/icons/notice.svg" alt="" width={48} height={101} className="shrink-0" />
                <div className="flex flex-col gap-[30px] py-4 pl-2.5 font-semibold text-brand">
                  <p className="text-lg leading-6 lg:text-2xl">지원 시 개인정보 제공 동의가 필요합니다.</p>
                  <p className="text-sm leading-6">
                    지원하시면 의뢰인에게 회원님의 연락처가 제공되며, 매칭을 위한 최소한의 정보만 전달됩니다.
                    개인정보는 매칭 목적 외에 사용되지 않습니다.
                  </p>
                </div>
              </div>
            </section>

            <div className="flex gap-[15px]">
              <Link href={listHref} className={cn(BUTTON, 'h-14 flex-1 border border-line bg-white text-xl text-brand hover:bg-line-soft')}>
                목록으로
              </Link>
              {isClient ? (
                <Link href={editHref} className={cn(BUTTON, 'h-14 flex-1 bg-brand text-xl text-white hover:bg-brand-hover')}>
                  수정하기
                </Link>
              ) : (
                // TODO: 지원 API(POST /api/v1/applications/{postId}) 연결
                <button type="button" className={cn(BUTTON, 'h-14 flex-1 bg-brand text-xl text-white hover:bg-brand-hover')}>
                  지원하기
                </button>
              )}
            </div>
          </div>

          {/* 공고 요약 (오른쪽 고정 카드) */}
          <aside className={cn(CARD, 'px-[35px] pt-7 pb-[30px] lg:sticky lg:top-6')}>
            <h2 className={cn(CARD_TITLE, 'mb-[31px]')}>공고 요약</h2>
            <dl className="flex flex-col gap-[3px]">
              <InfoRow label="날짜" labelWidth={94}>{date}</InfoRow>
              <InfoRow label="시간" labelWidth={94}>{post.startTime}</InfoRow>
              <InfoRow label="소요 시간" labelWidth={94}>{duration}</InfoRow>
              <InfoRow label="시급/보수" labelWidth={94}>{pay}</InfoRow>
              <InfoRow label="지역" labelWidth={94}>{location}</InfoRow>
            </dl>
            <div className="mt-3.5 flex flex-col gap-[5px]">
              {isClient ? (
                <Link href={editHref} className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                  수정하기
                </Link>
              ) : (
                // TODO: 지원 API 연결
                <button type="button" className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                  지원하기
                </button>
              )}
              <Link href={listHref} className={cn(BUTTON, 'h-11 border border-line bg-white text-base text-brand hover:bg-line-soft')}>
                목록으로
              </Link>
            </div>
            <p className="mt-[17px] flex items-center gap-2 px-5 text-sm leading-4 font-semibold text-brand-muted">
              <Image src="/icons/info-circle.svg" alt="" width={17.5} height={17.5} className="size-4 shrink-0" />
              지원 후 의뢰인의 승인 시 매칭이 진행됩니다.
            </p>
          </aside>
        </Container>
      </section>
    </AppShell>
  );
}
