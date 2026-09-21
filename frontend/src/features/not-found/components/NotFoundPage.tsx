'use client';

import AppShell from '@/components/layout/AppShell';

const DECORATIONS = [
    { left: 148, top: 84, rotate: 0, opacity: 0.5 },
    { left: 1068, top: 159, rotate: -173, opacity: 0.62 },
    { left: 610, top: 379, rotate: 34, opacity: 0.39 },
    { left: 995, top: 289, rotate: 115, opacity: 0.08 },
    { left: 321, top: 91, rotate: 167, opacity: 0.15 },
    { left: 240, top: 610, rotate: -145, opacity: 0.43 },
    { left: 299, top: 185, rotate: -13, opacity: 0.98 },
    { left: 857, top: 102, rotate: -141, opacity: 0.5 },
    { left: 515, top: 421, rotate: 115, opacity: 0.87 },
    { left: 151, top: 644, rotate: 78, opacity: 0.13 },
    { left: 56, top: 350, rotate: -73, opacity: 0.2 },
    { left: 804, top: 207, rotate: -18, opacity: 0.45 },
    { left: 84, top: 65, rotate: -16, opacity: 0.02 },
    { left: 332, top: 27, rotate: -8, opacity: 0.74 },
    { left: 783, top: 398, rotate: -66, opacity: 0.06 },
    { left: 191, top: 276, rotate: 109, opacity: 0.9 },
    { left: 517, top: 83, rotate: -59, opacity: 0.61 },
    { left: 512, top: 461, rotate: 120, opacity: 0.23 },
    { left: 358, top: 406, rotate: -33, opacity: 0.6 },
    { left: 871, top: 511, rotate: -126, opacity: 0.9 },
];

export default function NotFoundPage() {
    return (
        <AppShell>
            <section className="relative h-[743px] overflow-hidden bg-white">
                {/* Figma 배경 장식 */}
                <div
                    className="pointer-events-none absolute left-1/2 top-0 hidden h-full w-[1440px] -translate-x-1/2 lg:block"
                    aria-hidden="true"
                >
                    {DECORATIONS.map((item, index) => (
                        <span
                            key={index}
                            className="absolute block h-[15px] w-[12px] bg-[#61a0ff]"
                            style={{
                                left: item.left,
                                top: item.top,
                                opacity: item.opacity,
                                transform: `rotate(${item.rotate}deg)`,
                            }}
                        />
                    ))}
                </div>

                {/* 중앙 404 콘텐츠 */}
                <div className="relative z-10 mx-auto flex h-full w-full max-w-[603px] flex-col items-center justify-center gap-[30px] px-5">
                    <h1
                        className="
              bg-gradient-to-r
              from-[#61a0ff]
              to-brand
              bg-clip-text
              text-center
              text-[96px]
              leading-none
              font-extrabold
              text-transparent
              sm:text-[120px]
              lg:text-[141.698px]
            "
                    >
                        404
                    </h1>

                    <h2 className="text-center text-[28px] leading-normal font-bold text-brand lg:text-[34.873px]">
                        페이지를 찾을 수 없습니다!
                    </h2>

                    <div className="text-center text-[18px] leading-normal font-medium text-brand lg:text-[23.248px]">
                        <p>이전 페이지로 돌아가거나</p>
                        <p>더 나은 서비스를 위해 제보 부탁드립니다!</p>
                    </div>

                    <div className="flex w-full max-w-[611px] flex-col items-center gap-[10px] sm:flex-row">
                        <a
                            href="/"
                            className="
                flex
                h-[60px]
                w-full
                items-center
                justify-center
                rounded-[30px]
                border
                border-line
                bg-white
                text-[20px]
                font-semibold
                text-brand
                transition-colors
                hover:bg-line-soft
                sm:w-[300px]
              "
                        >
                            홈페이지로 돌아가기
                        </a>

                        <a
                            href="#"
                            className="
                flex
                h-[60px]
                w-full
                items-center
                justify-center
                rounded-[30px]
                bg-brand
                text-[20px]
                font-semibold
                text-white
                transition-colors
                hover:bg-brand-hover
                sm:w-[300px]
              "
                        >
                            제보하기
                        </a>
                    </div>
                </div>
            </section>
        </AppShell>
    );
}