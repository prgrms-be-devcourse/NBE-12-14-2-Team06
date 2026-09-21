import { cn } from '@/lib/cn';
import type { Manager } from '../types';
import ManagerProfile from './ManagerProfile';

type Props = {
  manager: Manager;
  /** lg = 동행 현황 (745px 카드, 버튼 가로) / sm = 보고서·리뷰 (396px 카드, 버튼 세로) */
  size: 'lg' | 'sm';
  title: string;
};

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card';

/** "동행 정보 / 동행 매니저 정보" 카드: 프로필 + 프로필 보기·연락하기 버튼 */
export default function ManagerInfoCard({ manager, size, title }: Props) {
  const large = size === 'lg';
  const button = cn(
    'flex w-full items-center justify-center border border-line bg-white font-semibold text-brand transition-colors hover:bg-line-soft',
    large ? 'h-11 rounded-[21px] text-base' : 'h-[37px] rounded-[18px] text-[13.6px]',
  );

  return (
    <section className={CARD}>
      <h2 className="mb-[3px] text-2xl leading-6 font-semibold text-brand">{title}</h2>
      <div className={cn('flex flex-col', large ? 'gap-3 px-[25px] pt-[27px] pb-1' : 'mt-3 gap-[15px]')}>
        <ManagerProfile manager={manager} size={size} />
        <div className={cn('flex gap-[4.25px]', large ? 'flex-col gap-2.5 sm:flex-row' : 'flex-col')}>
          {/* TODO: 프로필 보기 · 연락하기(메시지) 기능 연결 */}
          <button type="button" className={button}>
            프로필 보기
          </button>
          <button type="button" className={button}>
            연락하기
          </button>
        </div>
      </div>
    </section>
  );
}
