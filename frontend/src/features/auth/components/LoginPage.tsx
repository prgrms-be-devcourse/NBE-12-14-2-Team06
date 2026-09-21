'use client';

import Image from 'next/image';
import { AppShell } from '@/components/layout';
import LoginForm from './LoginForm';

/** 로그인 화면 — Figma 공통_로그인 564:17962 */
export default function LoginPage() {
  return (
    <AppShell>
      <section className="bg-white px-5 py-8 lg:px-[52px] lg:py-[26px]">
        <div className="mx-auto flex w-full max-w-[1066px] flex-col items-center justify-center lg:h-[692px] lg:flex-row">
          {/* 일러스트 — 좁은 화면에서는 폼에 집중할 수 있도록 숨깁니다 */}
          <div className="relative hidden h-[638px] w-[688px] shrink-0 overflow-clip rounded-card lg:block">
            <Image
              src="/images/login/illustration.png"
              alt="휠체어에 탄 어르신과 함께 병원으로 향하는 동행 매니저 일러스트"
              width={450}
              height={425}
              priority
              className="absolute top-[8.2%] left-[-14.47%] h-[91.79%] w-full max-w-none"
            />
          </div>

          {/* 카드가 일러스트 위로 128px 겹칩니다 (Figma 564:17966 의 -128 여백) */}
          <div className="relative z-10 flex w-full max-w-[502px] shrink-0 flex-col items-center justify-center rounded-card border border-line bg-white px-6 py-10 shadow-card lg:-ml-[128px] lg:h-[692px] lg:px-[50px] lg:py-[45px]">
            <LoginForm />
          </div>
        </div>
      </section>
    </AppShell>
  );
}
