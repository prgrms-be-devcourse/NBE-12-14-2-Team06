'use client';

import { AppShell } from '@/components/layout';
import { SectionHeading } from '@/components/ui';
import { EFFECTIVE_DATE, INTRO, POLICY_ARTICLES, POLICY_VERSIONS } from '../model/privacy';
import type { PolicyBlock } from '../types';

const CARD = 'rounded-[30px] border border-line bg-white px-6 py-8 shadow-card sm:px-8';
const TITLE = 'text-2xl leading-8 font-semibold text-brand';
const TEXT = 'text-base leading-7 text-brand';

/** 본문 조각 하나를 그립니다. */
function Block({ block }: { block: PolicyBlock }) {
  switch (block.type) {
    case 'p':
      return <p className={TEXT}>{block.text}</p>;
    case 'ul':
      return (
        <ul className={`${TEXT} flex list-disc flex-col gap-1.5 pl-5 marker:text-brand-muted`}>
          {block.items.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      );
    case 'note':
      return <p className="rounded-[20px] border border-line bg-line-soft px-5 py-4 text-sm leading-6 font-medium text-brand">{block.text}</p>;
    case 'table':
      return (
        // 표가 화면보다 넓으면 표 안에서만 가로로 밀립니다.
        <div className="overflow-x-auto rounded-[20px] border border-line">
          <table className="w-full min-w-[520px] border-collapse text-left text-sm leading-6 text-brand">
            <thead className="bg-line-soft">
              <tr>
                {block.head.map((cell) => (
                  <th key={cell} scope="col" className="px-4 py-3 font-semibold whitespace-nowrap">
                    {cell}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {block.rows.map((row) => (
                <tr key={row.join('|')} className="border-t border-line">
                  {row.map((cell, index) =>
                    index === 0 ? (
                      <th key={index} scope="row" className="px-4 py-3 align-top font-semibold">
                        {cell}
                      </th>
                    ) : (
                      <td key={index} className="px-4 py-3 align-top">
                        {cell}
                      </td>
                    ),
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
  }
}

/**
 * 개인정보 처리방침 — /privacy
 *
 * ⚠️ 글 내용은 model/privacy.ts 에 있습니다. 초안이므로 공개 전에 법무 검토와 확정 값(운영 주체·책임자·시행일) 입력이 필요합니다.
 */
export default function PrivacyPolicyPage() {
  return (
    <AppShell>
      <section className="bg-white px-4 py-[50px]">
        <div className="mx-auto flex w-full max-w-[900px] flex-col gap-[22px]">
          <SectionHeading
            title="개인정보 처리방침"
            description="‘가지’가 이용자의 개인정보를 어떻게 처리하고 보호하는지 안내합니다."
            className="mb-2"
          />

          <section className={CARD}>
            <div className="flex flex-col gap-4">
              {INTRO.map((paragraph) => (
                <p key={paragraph} className={TEXT}>
                  {paragraph}
                </p>
              ))}
            </div>
            <p className="mt-6 text-base leading-6 font-semibold text-brand">이 방침은 {EFFECTIVE_DATE}부터 적용됩니다.</p>
          </section>

          <nav aria-label="개인정보 처리방침 목차" className={CARD}>
            <h2 className={`${TITLE} mb-5`}>목차</h2>
            <ol className="grid gap-x-8 gap-y-2 text-base leading-6 font-medium text-brand sm:grid-cols-2">
              {POLICY_ARTICLES.map((article, index) => (
                <li key={article.id}>
                  <a href={`#${article.id}`} className="transition-colors hover:text-brand-hover hover:underline">
                    {index + 1}. {article.title}
                  </a>
                </li>
              ))}
              <li>
                <a href="#history" className="transition-colors hover:text-brand-hover hover:underline">
                  {POLICY_ARTICLES.length + 1}. 개정 이력
                </a>
              </li>
            </ol>
          </nav>

          {POLICY_ARTICLES.map((article, index) => (
            <section key={article.id} id={article.id} aria-labelledby={`${article.id}-title`} className={`${CARD} scroll-mt-6`}>
              <h2 id={`${article.id}-title`} className={`${TITLE} mb-5`}>
                {index + 1}. {article.title}
              </h2>
              <div className="flex flex-col gap-4">
                {article.blocks.map((block, blockIndex) => (
                  <Block key={blockIndex} block={block} />
                ))}
              </div>
            </section>
          ))}

          <section id="history" aria-labelledby="history-title" className={`${CARD} scroll-mt-6`}>
            <h2 id="history-title" className={`${TITLE} mb-5`}>
              {POLICY_ARTICLES.length + 1}. 개정 이력
            </h2>
            <ul className="flex flex-col gap-2">
              {POLICY_VERSIONS.map((version, index) => (
                <li key={version.label} className="flex flex-wrap items-center gap-x-3 gap-y-1 text-base leading-7 text-brand">
                  <span className="font-semibold">
                    {version.label}({version.date})
                  </span>
                  <span>{version.summary}</span>
                  {index === 0 && (
                    <span className="rounded-full bg-[#e8eefa] px-3 py-0.5 text-xs font-semibold text-[#203b9d]">현재 적용</span>
                  )}
                </li>
              ))}
            </ul>
          </section>
        </div>
      </section>
    </AppShell>
  );
}
