import Image from 'next/image';
import { CardButton, StatusLabel } from '@/features/post';
import { STATUS_LABEL } from '../model/posts';
import type { ClientPost } from '../types';

const DIVIDER = 'h-px w-full max-w-[377px] self-center bg-[#e6e8ec] opacity-50';

/**
 * "내가 작성한 공고" 카드 — Figma 의뢰인_내가 작성한 공고 Card1~4 (447×315)
 * 모집 중이면 "지원자 확인", 매칭 이후에는 "동행 현황" 버튼이 나옵니다.
 */
export default function ClientPostCard({ post }: { post: ClientPost }) {
  const label = STATUS_LABEL[post.status];

  return (
    <article className="flex min-h-[315px] w-full min-w-0 flex-col justify-center gap-[5.5px] rounded-[30px] border-[0.68px] border-line bg-white p-5 shadow-[0_0.68px_2.7px_rgba(25,33,61,0.08)] lg:h-[315px] lg:w-[447px]">
      <div className="flex w-full max-w-[377px] items-center gap-5 self-center">
        <Image src="/icons/image-placeholder.svg" alt="" width={73} height={73} className="size-[72.5px] shrink-0" />
        <div className="flex min-w-0 flex-1 flex-col gap-[13.7px]">
          <div className="flex">
            <StatusLabel tone={label.tone}>{label.text}</StatusLabel>
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
        {post.status === 'open' ? (
          <CardButton size="wide" variant="solid" href={`/client/posts/${post.id}/applicants`}>
            지원자 확인
          </CardButton>
        ) : (
          <CardButton size="wide" variant="solid" href={`/client/escort/${post.applicationId ?? 1}`}>
            동행 현황
          </CardButton>
        )}
      </div>
    </article>
  );
}
