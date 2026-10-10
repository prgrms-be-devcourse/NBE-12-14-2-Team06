import Image from 'next/image';
import { CardButton, POST_STATUS_LABEL, StatusLabel } from '@/features/post';
import type { ClientPost } from '../types';

const DIVIDER = 'h-px w-full max-w-[377px] self-center bg-[#e6e8ec] opacity-50';

/**
 * "내가 작성한 공고" 카드 — Figma 의뢰인_내가 작성한 공고 Card1~4 (447×315)
 * 모집 중이면 "지원자 확인", 매칭 이후에는 "동행 현황" 버튼이 나옵니다.
 */
export default function ClientPostCard({ post }: { post: ClientPost }) {
  const label = POST_STATUS_LABEL[post.status];

  // 동행이 끝난 공고의 미결제는 실제 동행 시간이 늘어 생긴 차액이라 "추가 결제"로 구분해 보여 줍니다.
  const isExtra = post.status === 'completed';
  // 결제 전 공고는 공개 목록(GET /api/v1/posts)에 올라가지 않아 실제로는 모집이 되지 않습니다.
  // "모집 중"을 같이 걸어 두면 지원자를 받고 있는 것처럼 읽혀서, 최초 미결제일 때는 상태 배지를 빼고
  // "미결제"만 남깁니다. (동행이 끝난 뒤의 "추가 결제"는 공고 상태도 같이 알아야 해서 둘 다 둡니다)
  const unpaidOnly = !!post.unpaid && !isExtra;
  // 결제 화면(토스 위젯)은 공고 등록 직후의 최초 결제와 같은 화면을 씁니다.
  // flow=extra 는 결제 후 공고 등록 완료 화면이 아니라 공고 상세로 돌아오기 위한 표시입니다.
  const paymentHref = post.unpaid
    ? `/client/posts/new/payment?${new URLSearchParams({
        postId: String(post.id),
        paymentId: String(post.unpaid.paymentId),
        amount: String(post.unpaid.amount),
        pay: String(post.unpaid.hourlyPay),
        ...(isExtra ? { flow: 'extra' } : {}),
      })}`
    : '';
  // 결제가 남아 있으면 채운 버튼은 "결제하기" 하나만 둡니다. (지원자 확인·동행 현황과 같이 강조되면 뭘 먼저
  // 눌러야 하는지가 흐려집니다)
  const actionVariant = post.unpaid ? 'ghost' : 'solid';

  return (
    <article className="flex min-h-[315px] w-full min-w-0 flex-col justify-center gap-[5.5px] rounded-[30px] border-[0.68px] border-line bg-white p-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)] lg:h-[315px] lg:w-[447px]">
      <div className="flex w-full max-w-[377px] items-center gap-5 self-center">
        <Image src="/images/post/eggplant.png" alt="" width={73} height={73} className="size-[72.5px] shrink-0 object-contain" />
        <div className="flex min-w-0 flex-1 flex-col gap-[13.7px]">
          <div className="flex gap-[5px]">
            {!unpaidOnly && <StatusLabel tone={label.tone}>{label.text}</StatusLabel>}
            {post.unpaid && <StatusLabel tone="red">{isExtra ? '추가 결제' : '미결제'}</StatusLabel>}
          </div>
          <p className="truncate text-[16.4px] leading-4 font-semibold text-brand">{post.title}</p>
          <p className="truncate text-[11px] leading-[13.7px] font-semibold text-brand">{post.hospitalName}</p>
          <p className="flex items-center gap-x-4 text-[9.6px] leading-[13.7px] font-medium text-brand">
            <span className="flex items-center gap-[6.9px]">
              <Image src="/icons/pin.svg" alt="" width={11} height={13} className="shrink-0" />
              {post.location}
            </span>
            {post.managerName && (
              <span className="flex items-center gap-[6.9px]">
                <Image src="/icons/client/person-small.svg" alt="" width={9} height={12} className="shrink-0" />
                {post.managerName}
              </span>
            )}
          </p>
        </div>
      </div>

      <div className={DIVIDER} />

      <div className="flex h-[32.4px] items-center justify-center gap-[15px] px-2 sm:gap-[28px]">
        <p className="w-[75px] text-center text-[11px] leading-[13.7px] font-semibold text-brand">
          {post.dateLabel}
          <br />
          {post.timeLabel}
        </p>
        <span aria-hidden="true" className="h-[24px] w-px bg-[#e6e8ec] opacity-50" />
        <p className="w-[75px] text-center text-[11px] leading-[13.7px] font-semibold text-brand">{post.durationLabel}</p>
        <span aria-hidden="true" className="h-[24px] w-px bg-[#e6e8ec] opacity-50" />
        <p className="w-[75px] text-center text-[9.6px] leading-[13.7px] font-semibold text-brand">{post.payLabel}</p>
      </div>

      <div className={DIVIDER} />

      <p className="flex h-[45px] w-full max-w-[363px] flex-col justify-center self-center text-[11px] leading-[13.7px] font-semibold text-brand">
        {post.description.map((line) => (
          <span key={line}>{line}</span>
        ))}
      </p>

      <div className="flex w-full max-w-[382px] items-center justify-center gap-[10.3px] self-center">
        <CardButton size="wide" href={`/client/posts/${post.id}`}>상세보기</CardButton>
        {post.applicationId === undefined ? (
          <CardButton size="wide" variant={actionVariant} href={`/client/posts/${post.id}/applicants`}>
            지원자 확인
          </CardButton>
        ) : (
          // 동행 현황 화면(/client/escort/[applicationId])이 postId 를 알 방법이 없어(백엔드에 신청 상세 조회 API가
          // 없음) 쿼리로 함께 넘깁니다. 서버에 GET /api/v1/applications/{applicationId} 가 생기면 지울 수 있습니다.
          <CardButton size="wide" variant={actionVariant} href={`/client/escort/${post.applicationId}?postId=${post.id}`}>
            동행 현황
          </CardButton>
        )}
        {/* 미결제 공고는 결제해야 공개 목록에 올라가므로, 결제가 가장 급한 버튼입니다.
            버튼이 셋이 되면 max-w 안에서 flex-1 로 나란히 줄어들어 카드 높이(315px)는 그대로입니다. */}
        {post.unpaid && (
          <CardButton size="wide" variant="solid" href={paymentHref}>
            {isExtra ? '추가 결제' : '결제하기'}
          </CardButton>
        )}
      </div>
    </article>
  );
}
