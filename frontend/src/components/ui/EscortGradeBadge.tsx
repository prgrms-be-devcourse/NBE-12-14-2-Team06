import type { EscortGrade } from '@/features/application';

type EscortGradeBadgeProps = {
    grade: EscortGrade;
    className?: string;
};

const GRADE_INFO: Record<
    EscortGrade,
    {
        label: string;
        icon: string;
        className: string;
    }
> = {
    SEED: {
        label: '씨앗',
        icon: '🫘',
        className: 'bg-[#F5EEE5] text-[#8A6848]',
    },
    SPROUT: {
        label: '새싹',
        icon: '🌱',
        className: 'bg-[#E8F7E8] text-[#37964A]',
    },
    FLOWER: {
        label: '꽃',
        icon: '🌸',
        className: 'bg-[#FCEAF3] text-[#D95B96]',
    },
    EGGPLANT: {
        label: '가지',
        icon: '🍆',
        className: 'bg-[#EEE8FA] text-[#7253A6]',
    },
};

export default function EscortGradeBadge({
                                             grade,
                                             className = '',
                                         }: EscortGradeBadgeProps) {
    const info = GRADE_INFO[grade];

    return (
        <span
            className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold ${info.className} ${className}`}
        >
      <span aria-hidden="true">{info.icon}</span>
            {info.label}
    </span>
    );
}