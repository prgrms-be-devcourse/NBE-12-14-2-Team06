import type { EscortGrade } from '@/features/application';

type EscortGradeCardProps = {
    grade: EscortGrade;
    completedCount: number;
};

const GRADE_INFO: Record<
    EscortGrade,
    {
        label: string;
        icon: string;
        minCount: number;
        nextCount: number | null;
    }
> = {
    SEED: {
        label: '씨앗',
        icon: '🫘',
        minCount: 0,
        nextCount: 5,
    },
    SPROUT: {
        label: '새싹',
        icon: '🌱',
        minCount: 5,
        nextCount: 10,
    },
    FLOWER: {
        label: '꽃',
        icon: '🌸',
        minCount: 10,
        nextCount: 30,
    },
    EGGPLANT: {
        label: '가지',
        icon: '🍆',
        minCount: 30,
        nextCount: null,
    },
};

const GRADE_ORDER: EscortGrade[] = [
    'SEED',
    'SPROUT',
    'FLOWER',
    'EGGPLANT',
];

function getBaseGrade(completedCount: number): EscortGrade {
    if (completedCount >= 30) return 'EGGPLANT';
    if (completedCount >= 10) return 'FLOWER';
    if (completedCount >= 5) return 'SPROUT';
    return 'SEED';
}

export default function EscortGradeCard({
                                            grade,
                                            completedCount,
                                        }: EscortGradeCardProps) {
    const info = GRADE_INFO[grade];

    const baseGrade = getBaseGrade(completedCount);

    const currentGradeIndex = GRADE_ORDER.indexOf(grade);
    const baseGradeIndex = GRADE_ORDER.indexOf(baseGrade);

    const hasPenalty = currentGradeIndex < baseGradeIndex;

    const nextCount = info.nextCount;

    const remaining =
        nextCount === null
            ? 0
            : Math.max(0, nextCount - completedCount);

    const progress =
        nextCount === null
            ? 100
            : Math.min(
                100,
                Math.max(
                    0,
                    ((completedCount - info.minCount) /
                        (nextCount - info.minCount)) *
                    100,
                ),
            );

    const progressCurrent =
        nextCount === null ? 0 : Math.max(0, completedCount - info.minCount);

    const progressTotal =
        nextCount === null ? 0 : nextCount - info.minCount;

    return (
        <section className="rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:px-8">
            <div className="flex flex-col gap-7">
                <div className="flex items-center justify-between gap-4">
                    <div>
                        <p className="text-sm font-semibold text-brand-muted">
                            나의 등급
                        </p>

                        <div className="mt-2 flex items-center gap-3">
              <span
                  className="text-[38px] leading-none"
                  aria-hidden="true"
              >
                {info.icon}
              </span>

                            <div>
                                <p className="text-2xl font-bold text-brand">
                                    {info.label}
                                </p>

                                <p className="mt-1 text-sm font-medium text-brand-muted">
                                    완료 동행 {completedCount}회
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="hidden items-center gap-2 sm:flex">
                        {GRADE_ORDER.map((item) => {
                            const gradeInfo = GRADE_INFO[item];
                            const active =
                                GRADE_ORDER.indexOf(item) <= currentGradeIndex;

                            return (
                                <div
                                    key={item}
                                    className="flex flex-col items-center gap-1"
                                >
                                    <div
                                        className={`grid size-10 place-items-center rounded-full text-lg ${
                                            active
                                                ? 'bg-line-soft'
                                                : 'bg-[#F7F7FA] opacity-40'
                                        }`}
                                    >
                                        {gradeInfo.icon}
                                    </div>

                                    <span
                                        className={`text-xs font-semibold ${
                                            active
                                                ? 'text-brand'
                                                : 'text-brand-muted'
                                        }`}
                                    >
                    {gradeInfo.label}
                  </span>
                                </div>
                            );
                        })}
                    </div>
                </div>

                {hasPenalty ? (
                    <div className="rounded-[20px] bg-line-soft px-5 py-4">
                        <p className="text-sm font-semibold text-brand">
                            노쇼 이력이 현재 등급에 반영되어 있어요.
                        </p>

                        <p className="mt-1 text-xs leading-5 font-medium text-brand-muted">
                            등급은 완료 동행 횟수와 노쇼 이력을 함께 반영해 계산됩니다.
                        </p>
                    </div>
                ) : grade === 'EGGPLANT' ? (
                    <div className="rounded-[20px] bg-line-soft px-5 py-4">
                        <p className="text-sm font-semibold text-brand">
                            최고 등급을 달성했어요! 🎉
                        </p>

                        <p className="mt-1 text-xs font-medium text-brand-muted">
                            지금까지 완료한 동행은 총 {completedCount}회예요.
                        </p>
                    </div>
                ) : (
                    <div>
                        <div className="mb-2 flex items-center justify-between gap-4">
                            <p className="text-sm font-semibold text-brand">
                                다음 등급까지 {remaining}회 남았어요
                            </p>

                            <p className="text-xs font-medium text-brand-muted">
                                {progressCurrent} / {progressTotal}회
                            </p>
                        </div>

                        <div className="h-2.5 overflow-hidden rounded-full bg-line-soft">
                            <div
                                className="h-full rounded-full bg-brand transition-all"
                                style={{ width: `${progress}%` }}
                            />
                        </div>
                    </div>
                )}
            </div>
        </section>
    );
}