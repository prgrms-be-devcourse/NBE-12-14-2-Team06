import Image from 'next/image';
import { cn } from '@/lib/cn';
import { formatDotDate } from '../lib/date';
import { ROLE_LABEL } from '../model';
import type { Member } from '../types';

/** 칸 사이의 짧은 세로 구분선 (Figma 30px) */
const DIVIDER =
  'after:absolute after:top-1/2 after:right-0 after:h-[30px] after:w-px after:-translate-y-1/2 after:bg-[#e6e8ec] after:opacity-50';

const CELL = 'relative h-[75px] px-2.5 text-left align-middle';
const FIRST_CELL = 'pl-[29.7px]';
const LAST_CELL = 'pr-[29.7px]';

const HEAD_CELL = 'bg-line-soft border-y-[0.849px] border-line text-lg font-semibold text-brand';

/** 표 머리글 (Figma 571:20318). 마지막 칸은 상세보기 화살표 자리라 비어 있습니다. */
const COLUMNS = ['회원 ID', '이름', '역할', '이메일', '전화번호', '가입일'];

/** 회원 목록 표 (Figma 571:19219) */
export default function MemberTable({ members }: { members: Member[] }) {
  return (
    <div className="w-full overflow-x-auto">
      <table className="w-[952px] table-fixed border-separate border-spacing-0">
        <colgroup>
          <col className="w-[120px]" />
          <col className="w-[100px]" />
          <col className="w-[100px]" />
          <col className="w-[160px]" />
          <col className="w-[160px]" />
          <col className="w-[160px]" />
          <col className="w-[152px]" />
        </colgroup>

        <thead>
          <tr>
            {COLUMNS.map((label, index) => (
              <th
                key={label}
                scope="col"
                className={cn(
                  CELL,
                  HEAD_CELL,
                  DIVIDER,
                  index === 0 && `${FIRST_CELL} rounded-l-[25.464px] border-l-[0.849px]`,
                )}
              >
                {label}
              </th>
            ))}
            <th scope="col" className={cn(CELL, HEAD_CELL, LAST_CELL, 'rounded-r-[25.464px] border-r-[0.849px]')}>
              <span className="sr-only">상세 보기</span>
            </th>
          </tr>
        </thead>

        <tbody>
          {members.map((member, rowIndex) => {
            // 행 사이에만 가로 구분선을 둡니다 (Figma 571:20345).
            const divided = rowIndex < members.length - 1 && 'border-b border-[#e6e8ec]/50';
            const cell = cn(CELL, divided, 'text-base font-medium text-brand');

            return (
              <tr key={member.id} className="transition-colors hover:bg-line-soft/60">
                <th scope="row" className={cn(cell, FIRST_CELL, DIVIDER, 'font-medium')}>
                  {member.id}
                </th>
                <td className={cn(cell, DIVIDER)}>{member.name}</td>
                <td className={cn(cell, DIVIDER)}>{ROLE_LABEL[member.role]}</td>
                <td className={cn(cell, DIVIDER)}>{member.email}</td>
                <td className={cn(cell, DIVIDER)}>{member.phone}</td>
                <td className={cell}>{formatDotDate(member.joinedAt)}</td>
                {/* chevron-right.svg 는 24px 박스 안에 7×14 화살표가 들어 있어 오른쪽 여백(8px)만큼 당깁니다. */}
                <td className={cn(cell, 'pr-[21.7px] text-right')}>
                  {/* TODO: 회원 상세 화면(GET /api/v1/admin/users/{userId}) 이 생기면 Link 로 바꾸세요. */}
                  <button
                    type="button"
                    aria-label={`${member.name}(${member.id}) 상세 정보 보기`}
                    className="ml-auto block size-6 text-brand transition-opacity hover:opacity-60"
                  >
                    <Image src="/icons/chevron-right.svg" alt="" width={24} height={24} />
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
