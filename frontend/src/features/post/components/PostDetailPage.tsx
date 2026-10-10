'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname, useParams, useRouter } from 'next/navigation';
import { useEffect, useState, type ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow } from '@/components/ui';
import { applyToPost, fetchMyApplications } from '@/features/application';
import { useCurrentUser, useRequireAuth, type CurrentUser } from '@/features/auth';
// 배럴(@/features/education)로 가져오면 education 화면 → @/features/post → 이 파일로 순환 import 가 생겨서 api 모듈을 직접 가져옵니다.
import { isEducationRequiredError } from '@/features/education/api';
import {
  cancelPayment,
  fetchPaymentsByPost,
  isCancelablePayment,
  type PaymentDto,
  type PaymentStatus,
} from '@/features/payment';
import { fetchRidesByPost, type RideDto } from '@/features/ride';
import { cn } from '@/lib/cn';
import { deletePost, fetchPost } from '../api';
import { daysFromNow, formatFullDate, formatTime, parseDateTime } from '../lib/date';
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

/** 결제 취소 사유 길이 제한. 입력한 사유가 그대로 토스 취소 내역으로 넘어가서 넉넉하게만 잡아 둡니다. */
const MAX_CANCEL_REASON_LENGTH = 200;

/** 결제 상태 배지. 문구는 백엔드 PaymentStatus 의 description 을 그대로 씁니다. */
const PAYMENT_LABEL: Record<PaymentStatus, { text: string; tone: LabelTone }> = {
  READY: { text: '미결제', tone: 'red' },
  IN_PROGRESS: { text: '결제 중', tone: 'blue' },
  DONE: { text: '결제 완료', tone: 'green' },
  CANCELED: { text: '결제 취소', tone: 'gray' },
  PARTIAL_CANCELED: { text: '부분 취소', tone: 'purple' },
  DELETED: { text: '공고 삭제', tone: 'gray' },
};

/** 결제가 아니라 취소 시점을 보여 줘야 하는 상태 (부분 취소는 결제 자체가 살아 있어 결제 일시를 그대로 둡니다) */
const CANCELED_STATUS: PaymentStatus[] = ['CANCELED', 'DELETED'];

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
 * 이동수단은 GET /api/v1/rides/posts/{postId}로 별도 조회합니다.
 * ⚠️ 백엔드에 없는 항목(진료과 · 의뢰인 유형/보호자 동행 여부/성별 선호/소개)은 화면에서 뺐습니다.
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
  const [alreadyApplied, setAlreadyApplied] = useState(false);
  const [applyError, setApplyError] = useState<string>();
  /** 교육 미이수로 지원이 막혔는지 (에러 문구 옆에 교육 영상 링크를 보여줍니다) */
  const [educationRequired, setEducationRequired] = useState(false);
  const [rides, setRides] = useState<RideDto[]>([]);
  // 이 공고의 결제 전체(미결제·완료·취소 모두). 공고 조회와 같은 방식으로 postId 를 같이 들고 있습니다.
  const [historyResult, setHistoryResult] = useState<{ postId: number; data: PaymentDto[] }>();
  const [canceling, setCanceling] = useState(false);
  const [cancelError, setCancelError] = useState<string>();
  /** 취소 사유 입력칸을 펼친 결제 번호 (닫혀 있으면 undefined) */
  const [cancelOpenId, setCancelOpenId] = useState<number>();
  const [cancelReason, setCancelReason] = useState('');
  /** 결제를 취소한 뒤 결제 상태를 다시 불러오기 위한 값 (바뀌면 조회 effect 가 다시 돕니다) */
  const [paymentReload, setPaymentReload] = useState(0);

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

  useEffect(() => {
    if (user?.role !== 'ESCORT') {
      setAlreadyApplied(false);
      return;
    }

    let ignore = false;

    fetchMyApplications()
        .then((applications) => {
          if (ignore) return;

          setAlreadyApplied(
              applications.some(
                  (application) =>
                      application.postId === postId &&
                      application.applicationStatus !== 'CANCELED'
              )
          );
        })
        .catch(() => {
          if (!ignore) {
            setAlreadyApplied(false);
          }
        });

    return () => {
      ignore = true;
    };
  }, [postId, user?.role]);
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

  // 결제 이력은 내 공고에서만 보여 줍니다. (결제 조회는 의뢰인 본인 로그인이 필요합니다)
  const showPayments = isClient && !!post;

  useEffect(() => {
    if (!showPayments) return;
    let ignore = false;
    fetchPaymentsByPost(postId)
      .then((data) => !ignore && setHistoryResult({ postId, data }))
      // 조회에 실패해도 공고 상세는 그대로 보여 줍니다. (결제 정보 카드만 뜨지 않습니다)
      .catch(() => !ignore && setHistoryResult({ postId, data: [] }));
    return () => {
      ignore = true;
    };
  }, [showPayments, postId, paymentReload]);

  // 다른 공고로 이동한 직후에는 이전 공고의 결제 이력을 쓰지 않습니다.
  const payments: PaymentDto[] = showPayments && historyResult?.postId === postId ? historyResult.data : [];

  // 아직 내지 않은 결제. 목록은 최근 건이 앞이라 find 가 가장 나중에 만들어진 미결제 건입니다.
  // (결제를 취소하면 백엔드가 같은 금액의 READY 건을 새로 만들어 둡니다)
  // 공고별 조회(GET /api/v1/payments/posts/{postId})를 따로 부르지 않고 이미 받아 온 이력에서 뽑습니다.
  const unpaidPayment = payments.find((payment) => payment.paymentStatus === 'READY');
  // 동행이 끝난 뒤의 미결제는 실제 동행 시간이 늘어 생긴 차액이라 "추가 결제"로 구분해 보여 줍니다.
  // (공고 목록 카드 ClientPostCard 와 같은 기준입니다)
  const isExtraPayment = !!post && toPostStatusKey(post.postStatus) === 'completed';

  // 결제 취소는 아직 아무도 매칭되지 않은 공고에서만 보여 줍니다.
  // 백엔드(PaymentService.cancel)도 모집 중·마감 기한 초과만 허용합니다(InvalidException 46) —
  // 매칭·진행 중인 공고의 결제를 취소하면 동행 매니저가 보수 없이 동행을 하게 되고,
  // 동행이 끝난 건은 이미 정산 데이터가 만들어져 있기 때문입니다.
  const postAllowsCancel = !!post && ['open', 'expired'].includes(toPostStatusKey(post.postStatus));

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

  /** 사유 입력칸을 접습니다. 입력하던 사유와 실패 문구도 같이 버립니다. */
  const closeCancelForm = () => {
    setCancelOpenId(undefined);
    setCancelReason('');
    setCancelError(undefined);
  };

  const handleCancelPayment = async (paymentId: number) => {
    // 취소 사유는 백엔드 PaymentCancelRequest 의 필수 본문이고, 토스 취소 내역에도 그대로 남습니다.
    const reason = cancelReason.trim();
    if (!reason) return;

    setCanceling(true);
    setCancelError(undefined);
    try {
      await cancelPayment(paymentId, reason);
      // 취소하면 백엔드가 같은 금액의 READY 결제를 새로 만들어 둡니다(= 다시 결제할 수 있는 "미결제" 상태).
      // 공고의 대기 중인 지원자도 서버가 모두 거절하므로 결제 이력을 다시 불러옵니다.
      setHistoryResult(undefined);
      setCancelOpenId(undefined);
      setCancelReason('');
      setPaymentReload((count) => count + 1);
    } catch (error) {
      setCancelError(error instanceof Error ? error.message : '결제 취소에 실패했습니다.');
    } finally {
      setCanceling(false);
    }
  };

  const handleApply = async () => {
    // 공개 화면(viewer='common')은 비로그인도 볼 수 있어서, 여기서만 로그인 여부를 확인합니다.
    // escort·client 화면은 이미 useRequireAuth 가드를 거쳐 들어오므로 항상 로그인 상태입니다.
    if (unauthenticated) {
      router.push(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }
    if (!window.confirm('이 공고에 지원하시겠습니까?')) return;
    // TODO: 지원 API(POST /api/v1/applications/{postId})는 동행 매니저(ESCORT) 로그인 쿠키가 있어야 합니다.
    setApplyState('applying');
    setApplyError(undefined);
    setEducationRequired(false);
    try {
      await applyToPost(postId);
      setApplyState('applied');
      setAlreadyApplied(true);
    } catch (error) {
      setApplyState('idle');
      setApplyError(error instanceof Error ? error.message : '지원에 실패했습니다.');
      setEducationRequired(isEducationRequiredError(error));
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

  // /client/posts/[postId] 는 CLIENT 로그인이면 누구든 들어올 수 있어서(useRequireAuth 는 역할만 봅니다),
  // 공고 작성자 본인이 아니면 여기서 막습니다. (GET /posts/{id} 자체는 누구나 볼 수 있는 공개 조회라 서버는 막지 않습니다)
  if (isClient && post.clientId !== user?.username) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">본인이 작성한 공고만 볼 수 있습니다.</p>
          <Link href={listHref} className={cn(BUTTON, 'mx-auto mt-8 h-14 w-60 border border-line text-xl text-brand')}>
            내 공고 목록으로
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
      : alreadyApplied || applyState === 'applied'
          ? '지원완료'
          : applyState === 'applying'
              ? '지원 중…'
              : applyClosed
                  ? '지원 불가'
                  : '지원하기';

  const applyDisabled =
      applyBlockedByRole ||
      alreadyApplied ||
      applyClosed ||
      applyState !== 'idle';

  const editHref = `/client/posts/${post.id}/edit`;

  // 결제 화면(토스 위젯)은 공고 등록 직후의 최초 결제와 같은 화면을 씁니다. 공고 목록 카드와 같은 주소입니다.
  // flow=extra 는 결제 후 공고 등록 완료 화면이 아니라 이 공고 상세로 돌아오기 위한 표시입니다.
  const unpaidHref = unpaidPayment
    ? `/client/posts/new/payment?${new URLSearchParams({
        postId: String(post.id),
        paymentId: String(unpaidPayment.id),
        amount: String(unpaidPayment.amount),
        pay: String(post.hourlyPay),
        ...(isExtraPayment ? { flow: 'extra' } : {}),
      })}`
    : '';

  const payLabel = isExtraPayment ? '추가 결제' : '결제하기';

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
              <Image src="/images/post/eggplant.png" alt="" width={100} height={100} className="size-[100px] shrink-0 object-contain" />
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
                  <InfoRow label="병원명" labelWidth={110}>{post.hospitalName}</InfoRow>
                  <InfoRow label="병원 주소" labelWidth={110}>{post.hospitalAddress}</InfoRow>
                  <InfoRow label="날짜" labelWidth={110}>{date}</InfoRow>
                  <InfoRow label="시간" labelWidth={110}>{post.startTime}</InfoRow>
                  <InfoRow label="시급" labelWidth={110}>{pay}</InfoRow>
                </div>
                <div aria-hidden="true" className="hidden self-center bg-[#e6e8ec] opacity-50 lg:block lg:h-40" />
                <div className="flex flex-col gap-[3px]">
                  <InfoRow label="출발지" labelWidth={124}>{post.pickupAddress}</InfoRow>
                  <InfoRow label="예상 소요 시간" labelWidth={124}>{duration}</InfoRow>
                  <InfoRow label="갈 때 이동수단" labelWidth={124}>{toHospitalTransport}</InfoRow>
                  <InfoRow label="올 때 이동수단" labelWidth={124}>{toHomeTransport}</InfoRow>
                  <InfoRow label="지역" labelWidth={124}>{location}</InfoRow>
                </div>
              </dl>
            </Section>

            {/* 공고 설명 */}
            <Section title="공고 설명">
              <div className="text-base leading-6 font-semibold text-brand">
                {post.details.map((line) => (
                  <p key={line}>{line}</p>
                ))}
              </div>
            </Section>

            {/* 환자 특이사항 (작성하지 않았으면 섹션 자체를 생략) */}
            {post.patientNote.length > 0 && (
                <Section title="환자 특이사항" titleGap="mb-[26px]">
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

            {/* "지원 전 안내"(지원 시 개인정보 제공 동의) 섹션은 보는 사람(비로그인/의뢰인/동행인)에
                따라 조건을 맞추기가 계속 어긋나서, 요청에 따라 전체 삭제했습니다. */}

            {/* 결제 정보 — 이 공고의 결제 전부(미결제·완료·취소). 최근 건이 위에 옵니다. */}
            {payments.length > 0 && (
              <Section title="결제 정보">
                <ul className="flex flex-col">
                  {payments.map((payment, index) => {
                    const paymentLabel = PAYMENT_LABEL[payment.paymentStatus];
                    // 백엔드가 취소를 받아 주는 조건(결제 DONE + 공고 모집 중·마감 초과)일 때만 버튼을 답니다.
                    const cancelable = postAllowsCancel && isCancelablePayment(payment);
                    const formOpen = cancelOpenId === payment.id;
                    // 취소된 건은 결제 시점이 아니라 취소 시점을 보여 줍니다.
                    const canceled = CANCELED_STATUS.includes(payment.paymentStatus);
                    const dateValue = canceled ? payment.canceledAt : payment.approvedAt;

                    return (
                      <li
                        key={payment.id}
                        className={cn('flex flex-col gap-[11px]', index > 0 && 'mt-5 border-t border-line-soft pt-5')}
                      >
                        {/* 배지는 내용만큼만 넓어야 해서 감싸 둡니다. (flex-col 안에서는 자식이 가로로 늘어납니다) */}
                        <div>
                          <StatusLabel tone={paymentLabel.tone}>{paymentLabel.text}</StatusLabel>
                        </div>
                        <dl className="flex flex-col gap-[3px]">
                          <InfoRow label="결제 금액" labelWidth={140}>{`${payment.amount.toLocaleString()}원`}</InfoRow>
                          {/* 아직 내지 않은(READY) 건은 승인 시각이 없습니다. */}
                          <InfoRow label={canceled ? '취소 일시' : '결제 일시'} labelWidth={140}>
                            {dateValue ? `${formatFullDate(parseDateTime(dateValue))} ${formatTime(dateValue)}` : '-'}
                          </InfoRow>
                        </dl>

                        {/* 아직 내지 않은 건에는 공고 목록 카드와 같은 결제 버튼을 답니다.
                            동행 완료 공고(추가 결제)는 바로 아래 "추가 결제 안내" 카드가 같은 버튼을 들고 있어 생략합니다. */}
                        {payment.id === unpaidPayment?.id && !isExtraPayment && (
                          <Link href={unpaidHref} className={cn(BUTTON, 'mx-auto h-14 w-full max-w-sm bg-brand text-xl text-white hover:bg-brand-hover')}>
                            {`${payment.amount.toLocaleString()}원 결제하기`}
                          </Link>
                        )}

                        {cancelable && !formOpen && (
                          <button
                            type="button"
                            onClick={() => setCancelOpenId(payment.id)}
                            className={cn(BUTTON, 'mx-auto h-14 w-full max-w-sm border border-line bg-white text-xl text-[#b91d1d] hover:bg-line-soft')}
                          >
                            결제 취소
                          </button>
                        )}
                        {cancelable && formOpen && (
                          <>
                            <p className="text-base leading-6 font-medium text-brand-muted">
                              결제를 취소하면 이 공고는 다시 미결제 상태가 되어 공개 목록에서 내려가고,
                              대기 중인 지원자는 모두 거절됩니다. 같은 금액으로 다시 결제할 수 있습니다.
                            </p>
                            <label htmlFor="cancel-reason" className="px-0.5 text-base leading-5 font-semibold text-brand">
                              결제 취소 사유
                            </label>
                            <textarea
                              id="cancel-reason"
                              value={cancelReason}
                              onChange={(event) => setCancelReason(event.target.value)}
                              maxLength={MAX_CANCEL_REASON_LENGTH}
                              disabled={canceling}
                              autoFocus
                              placeholder="예) 병원 예약이 취소되어 동행이 필요하지 않습니다."
                              className="h-[99px] w-full resize-none rounded-[30px] border border-line bg-white p-5 text-sm leading-5 text-brand placeholder:text-brand-muted"
                            />
                            {/* 접힌 상태의 "결제 취소" 버튼과 같은 폭으로 가운데에 둡니다. */}
                            <div className="mx-auto flex w-full max-w-sm gap-[15px]">
                              <button
                                type="button"
                                onClick={closeCancelForm}
                                disabled={canceling}
                                className={cn(BUTTON, 'h-14 flex-1 border border-line bg-white text-xl text-brand hover:bg-line-soft')}
                              >
                                닫기
                              </button>
                              {/* 사유를 적지 않으면 서버가 빈 사유를 그대로 토스 취소 내역에 남기게 되므로 버튼을 막아 둡니다. */}
                              <button
                                type="button"
                                onClick={() => handleCancelPayment(payment.id)}
                                disabled={canceling || !cancelReason.trim()}
                                className={cn(BUTTON, 'h-14 flex-1 border border-line bg-white text-xl text-[#b91d1d] hover:bg-line-soft')}
                              >
                                {canceling ? '결제 취소 중…' : `${payment.amount.toLocaleString()}원 결제 취소`}
                              </button>
                            </div>
                          </>
                        )}
                      </li>
                    );
                  })}
                </ul>
              </Section>
            )}

            {/* 추가 결제 안내 — 실제 동행 시간이 예상보다 길어져 차액이 남았을 때만 */}
            {isExtraPayment && unpaidPayment && (
              <section className={cn(CARD, 'border-brand px-6 pt-7 pb-6 lg:px-[35px]')}>
                <h2 className={cn(CARD_TITLE, 'mb-5')}>추가 결제 안내</h2>
                <p className="mb-5 text-base leading-6 font-medium text-brand">
                  실제 동행 시간이 예상보다 길어져 차액이 발생했습니다.
                  아래 금액을 결제하시면 동행 건이 최종 정산됩니다.
                </p>
                <dl className="mb-5 flex flex-col gap-[3px]">
                  <InfoRow label="추가 결제 금액" labelWidth={140}>{`${unpaidPayment.amount.toLocaleString()}원`}</InfoRow>
                  <InfoRow label="실제 동행 시간" labelWidth={140}>{`약 ${unpaidPayment.hours}시간`}</InfoRow>
                  <InfoRow label="적용 시급" labelWidth={140}>{`${unpaidPayment.hourlyPaySnapshot.toLocaleString()}원`}</InfoRow>
                </dl>
                <Link href={unpaidHref} className={cn(BUTTON, 'h-14 w-full bg-brand text-xl text-white hover:bg-brand-hover')}>
                  {`${unpaidPayment.amount.toLocaleString()}원 추가 결제하기`}
                </Link>
              </section>
            )}

            {(deleteError || cancelError || applyError) && (
              <p role="alert" className="px-2 text-sm font-medium text-[#b91d1d]">
                {deleteError || cancelError || applyError}
              </p>
            )}
            {!deleteError && educationRequired && (
              <Link href="/mypage/education" className={cn(BUTTON, 'h-14 w-full bg-brand text-xl text-white hover:bg-brand-hover')}>
                교육 영상 보러 가기
              </Link>
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
              {/* 미결제 공고는 결제해야 공개 목록에 올라가므로, 결제가 가장 급한 버튼입니다. (공고 목록 카드와 같은 기준) */}
              {unpaidPayment && (
                <Link href={unpaidHref} className={cn(BUTTON, 'h-11 bg-brand text-base text-white hover:bg-brand-hover')}>
                  {`${unpaidPayment.amount.toLocaleString()}원 ${payLabel}`}
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
