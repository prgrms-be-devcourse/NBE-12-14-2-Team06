import Link from 'next/link';

type Props = {
  title: string;
  description?: string;
  /** "공고 찾기" 같은 이동 버튼 */
  action?: { label: string; href: string };
};

/** 목록이 비었을 때 보여주는 안내 (Figma "아직 지원한 공고가 없습니다.") */
export default function EmptyState({ title, description, action }: Props) {
  return (
    <div className="flex min-h-[130px] w-full max-w-[910px] flex-col items-center justify-center gap-2 rounded-[30px] border border-line bg-white px-6 py-10 text-center shadow-card">
      <p className="text-lg leading-6 font-semibold text-brand">{title}</p>
      {description && <p className="text-xs leading-4 font-medium text-brand">{description}</p>}
      {action && (
        <Link
          href={action.href}
          className="mt-2 flex h-[27px] items-center rounded-[30px] bg-brand px-7 text-xs leading-4 font-semibold text-white transition-colors hover:bg-brand-hover"
        >
          {action.label}
        </Link>
      )}
    </div>
  );
}
