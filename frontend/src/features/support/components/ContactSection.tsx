'use client';

import { Container, SectionHeading } from '@/components/ui';
import { CONTACT_CHANNELS } from '../model';

/** 대표 전화 · 대표 이메일 · 운영 시간 카드 */
export default function ContactSection() {
  return (
    <section id="contact" className="bg-white py-14 lg:py-20">
      <Container>
        <SectionHeading
          title="이렇게 연락하세요"
          description={['전화가 편하신 분은 대표 전화로, 자료를 첨부해야 한다면 대표 이메일로 보내주세요.']}
        />

        <ul className="grid grid-cols-1 gap-[22px] lg:grid-cols-3">
          {CONTACT_CHANNELS.map(({ icon: Icon, title, value, description, href }) => (
            <li
              key={title}
              className="flex flex-col items-center gap-5 rounded-card border border-line bg-white px-6 py-[46px] text-center shadow-card"
            >
              <span className="grid size-[58px] shrink-0 place-items-center rounded-[18px] bg-line-soft">
                <Icon className="size-[30px] text-brand" />
              </span>

              <h3 className="text-xl leading-6 font-semibold text-brand">{title}</h3>

              {href ? (
                <a
                  href={href}
                  className="text-2xl leading-8 font-extrabold text-brand underline-offset-4 transition-colors hover:text-brand-hover hover:underline [overflow-wrap:anywhere]"
                >
                  {value}
                </a>
              ) : (
                <p className="text-2xl leading-8 font-extrabold text-brand">{value}</p>
              )}

              <p className="text-base leading-[22px] font-semibold text-brand-muted">{description}</p>
            </li>
          ))}
        </ul>
      </Container>
    </section>
  );
}
