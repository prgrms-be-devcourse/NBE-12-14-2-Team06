'use client';

import { Container, ImagePlaceholder } from '@/components/ui';
import Image from 'next/image';

/** Figma 564:18140 — ‘가지’란? */
export default function AboutSection() {
  return (
    <section id="about" className="bg-white py-14 lg:py-20">
      {/* 데스크톱에서는 카드가 이미지 위로 겹침 (Figma: Image mr -317px) */}
      <Container className="flex flex-col items-stretch lg:flex-row lg:items-center">
        <div className="-mb-14 min-w-0 lg:mr-[-200px] lg:mb-0 lg:flex-[1_1_936px] xl:mr-[-317px]">
          <Image src="/images/about-senior.png" alt="지팡이를 짚고있는 가지 일러스트" width={936} height={630} priority className="h-auto w-full rounded-card object-contain" />
        </div>

        <div className="relative z-10 mx-auto flex w-[calc(100%-2rem)] flex-col justify-center rounded-card border border-line bg-white px-10 py-12 drop-shadow-soft lg:mx-0 lg:w-auto lg:min-h-[434px] lg:flex-[0_0_440px] lg:px-14 lg:py-[62px] xl:flex-[0_0_487px]">
          <h2 className="text-[32px] leading-tight font-extrabold lg:text-[40px] lg:leading-9">
            ‘가지’란?
          </h2>
          <p className="mt-8 text-xl leading-6">
            이보게 젊은이..
            <br />
            나랑 병원좀 같이 <strong className="font-bold">가지</strong>..
          </p>
        </div>
      </Container>
    </section>
  );
}
