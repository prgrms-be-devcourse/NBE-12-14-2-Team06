'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname, useParams, useRouter } from 'next/navigation';
import { useEffect, useState, type ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow } from '@/components/ui';
import { applyToPost } from '@/features/application';
import { useCurrentUser, useRequireAuth, type CurrentUser } from '@/features/auth';
import { fetchPendingPayment, type PaymentDto } from '@/features/payment';
import { fetchRidesByPost, type RideDto } from '@/features/ride';
import { cn } from '@/lib/cn';
import { deletePost, fetchPost } from '../api';
import { daysFromNow, formatFullDate } from '../lib/date';
import { canDeletePost, canEditPost, postStatusLabel, toPostStatusKey } from '../model/status';
import type { LabelTone, PostBadge, PostDetail } from '../types';
import StatusLabel from './StatusLabel';

const BADGE: Record<PostBadge, { text: string; tone: LabelTone }> = {
  new: { text: '신규', tone: 'green' },
  closing: { text: '오늘 마감', tone: 'red' },
  open: { text: '모집 중', tone: 'blue' },
  closed: { text: '마감', tone: 'gray' },
};

const CARD = 'rounded-[30px] border border-line bg-white shadow-card';
const CARD_TITLE = 'text-2xl leading-6 font-semibold text-brand';
const BUTTON =
  'flex items-center justify-center rounded-[25px] px-6 font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50';

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
  /** 누가 보는지. 동행 매니저는 "지원하기", 의뢰인(작성자)은 "수정하기·삭제" 버튼이 나옵니다. */
  viewer?: 'common' | 'escort' | 'client';
};

const REQUIRED_ROLE: Record<'escort' | 'client', CurrentUser['role']> = {
  escort: 'ESCORT',
  client: 'CLIENT',
};

/**
 * 공고 상세 — Figma 동행 매니저_공고 상세 225:1146 · 의뢰인_공고 상세 521:2254
 *
 * viewer='common'(/posts/[postId])은 비로그인도 볼 수 있는 공개 화면이라 로그인 가드를 걸지 않습니다.
 * viewer='escort'·'client' 는 각각 역할 가드를 거친 뒤에만 본문을 그립니다.
 */
export default function PostDetailPage({ viewer = 'common' }: Props) {
  if (viewer === 'common') return <PostDetailPageBody viewer={viewer} />;
  return <GuardedPostDetailPage viewer={viewer} />;
}

function GuardedPostDetailPage({ viewer }: { viewer: 'escort' | 'client' }) {
  const { loading, user } = useRequireAuth(REQUIRED_ROLE[viewer]);

  if (loading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  return <PostDetailPageBody viewer={viewer} />;
}

/**
 * 상세 API(GET /api/v1/posts/{postId})로 조회합니다. 삭제·지원하기도 여기서 연결합니다.
 * ⚠️ 백엔드에 없는 항목(진료과 · 이동수단 · 의뢰인 유형/보호자 동행 여부/성별 선호/소개)은 화면에서 뺐습니다.
 */
function PostDetailPageBody({ viewer }: Props) {
  const params = useParams<{ postId: string }>();
  const router = useRouter();
  const pathname = usePathname();
  const { user, unauthenticated } = useCurrentUser();
  const postId = Number(params.postId);

  // result.postId 로 "지금 postId 의 결과인지"를 판단합니다. (postId 가 바뀐 직후에는 이전 결과를 버리고 loading 으로 봅니다)
  const [result, setResult] = useState<{ postId: number; data?: PostDetail; error?: string }>();
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string>();
  const [applyState, setApplyState] = useState<'idle' | 'applying' | 'applied'>('idle');
  const [applyError, setApplyError] = useState<string>();
  const [rides, setRides] = useState<RideDto[]>([]);
  // 동행이 끝난 뒤 남아 있는 미결제(추가 결제) 건. 공고 조회와 같은 방식으로 postId 를 같이 들고 있습니다.
  const [paymentResult, setPaymentResult] = useState<{ postId: number; data: PaymentDto | null }>();

  useEffect(() => {
    let ignore = false;
    fetchPost(postId)
      .then((data) => !ignore && setResult({ postId, data }))
      .catch((error: Error) => !ignore && setResult({ postId, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [postId]);

  useEffect(() => {
    let ignore = false;
    fetchRidesByPost(postId)
        .then((data) => {
          if (!ignore) {
            setRides(data);
          }
        })
        .catch(() => {
          if (!ignore) {
            setRides([]);
          }
        });
    return () => {
      ignore = true;
    };
  }, [postId]);

  const loading = result?.postId !== postId;
  const post = loading ? undefined : result?.data;
  const loadError = loading ? undefined : result?.error;

  const isClient = viewer === 'client';

  const listHref =
    viewer === 'client'
      ? '/client/posts'
      : viewer === 'escort'
        ? '/escort/posts'
        : '/posts';

  // 동행이 끝난 내 공고일 때만 남은 결제를 확인합니다.
  // 결제 조회는 로그인(의뢰인 본인)이 필요하고, 동행 완료 전에 남아 있는 READY 결제는
  // "추가 결제"가 아니라 아직 안 낸 최초 결제라서 여기서 물어보면 안 됩니다.
  const canHaveExtraPayment = isClient && !!post && toPostStatusKey(post.postStatus) === 'completed';

  useEffect(() => {
    if (!canHaveExtraPayment) return;
    let ignore = false;
    fetchPendingPayment(postId)
      .then((payment) => !ignore && setPaymentResult({ postId, data: payment }))
      // 조회에 실패해도 공고 상세 자체는 보여 줍니다. (추가 결제 안내만 뜨지 않습니다)
      .catch(() => !ignore && setPaymentResult({ postId, data: null }));
    return () => {
      ignore = true;
    };
  }, [canHaveExtraPayment, postId]);

  // 다른 공고로 이동한 직후에는 이전 공고의 결제 정보를 쓰지 않습니다.
  const pendingPayment = canHaveExtraPayment && paymentResult?.postId === postId ? paymentResult.data : null;

  const handleDelete = async () => {
    if (!window.confirm('이 공고를 삭제할까요? 되돌릴 수 없습니다.')) return;
    setDeleting(true);
    setDeleteError(undefined);
    try {
      // TODO: 삭제 API(DELETE /api/v1/posts/{postId})는 로그인 쿠키(작성자 본인)가 있어야 합니다.
      await deletePost(postId);
      router.push(listHref);
    } catch (error) {
      setDeleteError(error instanceof Error ? error.message : '삭제에 실패했습니다.');
      setDeleting(false);
    }
  };

  const handleApply = async () => {
    // 공개 화면(viewer='common')은 비로그인도 볼 수 있어서, 여기서만 로그인 여부를 확인합니다.
    // escort·client 화면은 이미 useRequireAuth 가드를 거쳐 들어오므로 항상 로그인 상태입니다.
    if (unauthenticated) {
      router.push(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }
    // TODO: 지원 API(POST /api/v1/applications/{postId})는 동행 매니저(ESCORT) 로그인 쿠키가 있어야 합니다.
    setApplyState('applying');
    setApplyError(undefined);
    try {
      await applyToPost(postId);
      setApplyState('applied');
    } catch (error) {
      setApplyState('idle');
      setApplyError(error instanceof Error ? error.message : '지원에 실패했습니다.');
    }
  };

  if (!post) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">{loadError ? `공고를 불러오지 못했습니다. (${loadError})` : '공고를 불러오는 중입니다.'}</p>
          <Link href={listHref} className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-xl text-brand')}>
            목록으로
          </Link>
        </section>
      </AppShell>
    );
  }

  // 의뢰인은 자기 공고의 진행 상태(모집 중 · 매칭 완료 · 진행 중 · 동행 완료 …)를 봅니다.
  // 동행 매니저·비로그인은 "지원할 수 있는지"가 중요해서 신규/오늘 마감 같은 모집 배지를 그대로 씁니다.
  const label = isClient ? postStatusLabel(post.postStatus) : BADGE[post.badge];
  const date = formatFullDate(daysFromNow(post.startsInDays));
  const duration = `약 ${post.hours}시간`;
  const pay = `시급 ${post.hourlyPay.toLocaleString()}원`;
  const location = `${post.region} ${post.district}`;
  const toHospitalRide = rides.find((ride) => ride.direction === 'TO_HOSPITAL');
  const toHomeRide = rides.find((ride) => ride.direction === 'TO_HOME');

  const rideLabel: Record<NonNullable<RideDto['selected']>, string> = {
    WALK: '도보',
    BUS: '대중교통',
    TAXI: '택시',
    OWN_CAR: '자가용',
  };

  const toHospitalTransport = toHospitalRide?.selected
      ? rideLabel[toHospitalRide.selected]
      : '미정';

  const toHomeTransport = toHomeRide?.selected
      ? rideLabel[toHomeRide.selected]
      : '미정';

  const applyClosed = post.badge === 'closed';
  // 지원은 동행 매니저(ESCORT)만 할 수 있어서, 의뢰인(CLIENT)으로 로그인했으면 공개 화면에서도 누를 수 없게 둡니다.
  const applyBlockedByRole = user?.role === 'CLIENT';
  const applyLabel = applyBlockedByRole
    ? '지원 불가'
    : applyState === 'applied'
      ? '지원 완료'
      : applyState === 'applying'
        ? '지원 중…'
        : applyClosed
          ? '지원 불가'
          : '지원하기';
  const applyDisabled = applyBlockedByRole || applyClosed || applyState !== 'idle';

  const editHref = `/client/posts/${post.id}/edit`;

  // 추가 결제도 공고 등록 때와 같은 결제 화면(토스 위젯)을 씁니다.
  // flow=extra 는 결제 후 공고 등록 완료 화면이 아니라 이 공고 상세로 돌아오기 위한 표시입니다.
  const extraPaymentHref = pendingPayment
    ? `/client/posts/new/payment?${new URLSearchParams({
        postId: String(post.id),
        paymentId: String(pendingPayment.id),
        amount: String(pendingPayment.amount),
        pay: String(post.hourlyPay),
        flow: 'extra',
      })}`
    : '';

  // 수정·삭제는 백엔드가 공고 상태로 막습니다. 눌러도 실패할 버튼은 아예 보여 주지 않습니다.
  // 수정은 "모집 중 + 모집 시작 전", 삭제는 "모집 중이거나 마감 기한 초과"일 때만 됩니다.
  // (조건이 서로 달라서 따로 계산합니다 — 모집이 시작된 공고는 삭제만 됩니다)
  const canEdit = isClient && canEditPost(post.postStatus, post.recruitStarted);
  const canDelete = isClient && canDeletePost(post.postStatus);

  return (
    <AppShell>
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
                  <InfoRow label="병원 주소" labelWidth={90}>{post.hospitalAddress}</InfoRow>
                  <InfoRow label="날짜" labelWidth={90}>{date}</InfoRow>
                  <InfoRow label="시간" labelWidth={92}>{post.startTime}</InfoRow>
                </div>
                <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-40" />
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="출발지" labelWidth={124}>{post.pickupAddress}</InfoRow>
                  <InfoRow label="예상 소요 시간" labelWidth={124}>{duration}</InfoRow>
                  <InfoRow label="갈 때 이동수단" labelWidth={124}>{toHospitalTransport}</InfoRow>
                  <InfoRow label="올 때 이동수단" labelWidth={124}>{toHomeTransport}</InfoRow>
                  <InfoRow label="지역" labelWidth={124}>{location}</InfoRow>
                  <InfoRow label="시급/보수" labelWidth={124}>{pay}</InfoRow>
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

            {/* 환자 특이사항 (작성하지 않았으면 섹션 자체를 생략) */}
            {post.patientNote.length > 0 && (
              <Section title="환자 특이사항" titleGap="mb-[26px]" className="lg:min-h-[239px]">
                <ul className="flex flex-col gap-[7px] px-0.5">
                  {post.patientNote.map((note) => (
                    <li key={note} className="flex items-center gap-[15px] text-base leading-5 font-semibold text-brand">
                      <Image src="/icons/check-circle-fill.svg" alt="" width={20} height={20} className="shrink-0" />
                      {note}
                    </li>
                  ))}
                </ul>
              </Section>
            )}

            {/* 모집 정보 */}
            <Section title="모집 정보" titleGap="mb-[22px]" className="lg:min-h-[120px]">
              <dl className="flex flex-col gap-[3px]">
                <InfoRow label="모집 기간" labelWidth={110}>{post.recruitPeriod}</InfoRow>
                <InfoRow label="보고서 요청" labelWidth={110}>{post.reportRequired ? '동행 후 보고서 작성을 요청합니다.' : '보고서를 요청하지 않습니다.'}</InfoRow>
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

            {/* 추가 결제 안내 — 실제 동행 시간이 예상보다 길어져 차액이 남았을 때만 */}
            {pendingPayment && (
              <section className={cn(CARD, 'border-brand px-6 pt-7 pb-6 lg:px-[35px]')}>
                <h2 className={cn(CARD_TITLE, 'mb-5')}>추가 결제 안내</h2>
                <p className="mb-5 text-base leading-6 font-medium text-brand">
                  실제 동행 시간이 예상보다 길어져 차액이 발생했습니다.
                  아래 금액을 결제하시면 동행 건이 최종 정산됩니다.
                </p>
                <dl className="mb-5 flex flex-col gap-[3px]">
                  <InfoRow label="추가 결제 금액" labelWidth={140}>{`${pendingPayment.amount.toLocaleString()}원`}</InfoRow>
                  <InfoRow label="실제 동행 시간" labelWidth={140}>{`약 ${pendingPayment.hours}시간`}</InfoRow>
                  <InfoRow label="적용 시급" labelWidth={140}>{`${pendingPayment.hourlyPaySnapshot.toLocaleString()}원`}</InfoRow>
                </dl>
                <Link href={extraPaymentHref} className={cn(BUTTON, 'h-14 w-full bg-brand text-xl text-white hover:bg-brand-hover')}>
                  {`${pendingPayment.amount.toLocaleString()}원 추가 결제하기`}
                </Link>
              </section>
            )}

            {(deleteError || applyError) && (
              <p role="alert" className="px-2 text-sm font-medium text-[#b91d1d]">
                {deleteError || applyError}
              </p>
            )}
            <div className="flex gap-[15px]">
              <Link href={listHref} className={cn(BUTTON, 'h-14 flex-1 border border-line bg-white text-xl text-brand hover:bg-line-soft')}>
                목록으로
              </Link>
              {isClient ? (
                <>
                  {canDelete && (
                    <button
                      type="button"
                      onClick={handleDelete}
                      disabled={deleting}
                      className={cn(BUTTON, 'h-14 flex-1 border border-line bg-white text-xl text-[#b91d1d] hover:bg-line-soft')}
                    >
                      {deleting ? '삭제 중…' : '삭제하기'}
                    </button>
                  )}
                  {canEdit && (
                    <Link href={editHref} className={cn(BUTTON, 'h-14 flex-1 bg-brand text-xl text-white hover:bg-brand-hover')}>
                      수정하기
                    </Link>
                  )}
                </>
              ) : (
                <button type="button" onClick={handleApply} disabled={applyDisabled} className={cn(BUTTON, 'h-14 flex-1 bg-brand text-xl text-white hover:bg-brand-hover')}>
                  {applyLabel}
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
              {pendingPayment && (
                <Link href={extraPaymentHref} className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                  {`${pendingPayment.amount.toLocaleString()}원 추가 결제`}
                </Link>
              )}
              {isClient ? (
                <>
                  {canEdit && (
                    <Link href={editHref} className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                      수정하기
                    </Link>
                  )}
                  {canDelete && (
                    <button
                      type="button"
                      onClick={handleDelete}
                      disabled={deleting}
                      className={cn(BUTTON, 'h-11 border border-line bg-white text-base text-[#b91d1d] hover:bg-line-soft')}
                    >
                      {deleting ? '삭제 중…' : '삭제하기'}
                    </button>
                  )}
                </>
              ) : (
                <button type="button" onClick={handleApply} disabled={applyDisabled} className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                  {applyLabel}
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
