'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState, type ChangeEvent, type FormEvent, type ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { useRequireAuth } from '@/features/auth';
import { cn } from '@/lib/cn';
import { showUnimplemented } from '@/lib/unimplemented';
import { writeReport } from '@/features/report';
import { fetchReportTarget } from '../api';
import { formatClockTime } from '../lib/date';
import { DEPARTMENTS } from '../model/departments';
import { clearReportDraft, loadReportDraft, saveReportDraft, type ReportDraft, type ReportDraftFields } from '../model/reportDraft';
import type { ReportTarget } from '../types';

const MAX_PHOTOS = 4;
const GUIDES = [
  '실제 동행한 내용을 바탕으로 작성해주세요.',
  '진료 내용은 구체적으로 작성할수록 좋아요.',
  '의뢰인의 개인정보는 포함하지 말아주세요.',
  '사진은 병원 내부가 노출되지 않도록 주의해주세요.',
  '허위 작성 시 서비스 이용에 제한이 있을 수 있습니다.',
];

const FIELD =
  'w-full rounded-[20px] border border-line-soft bg-white px-4 text-base leading-5 text-brand shadow-card placeholder:text-brand-muted';
const ERROR_TEXT = 'px-4 text-sm leading-5 font-medium text-[#b91d1d]';

/** "라벨 + 입력" 한 줄 (라벨 100px) */
function FieldRow({ label, htmlFor, children }: { label: string; htmlFor: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:gap-2.5">
      <label htmlFor={htmlFor} className="shrink-0 px-4 pt-[18px] text-base leading-5 font-semibold text-brand sm:w-[126px]">
        {label}
      </label>
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}

function SectionTitle({ children }: { children: ReactNode }) {
  return <h2 className="mb-2 text-2xl leading-6 font-semibold text-brand">{children}</h2>;
}

type TargetResult =
  | { status: 'notFound' }
  | { status: 'error'; message: string }
  | { status: 'ready'; target: ReportTarget };

/** 임시 저장 상태. key 는 지금 보고 있는 동행 건 — 다른 건으로 옮겨가면 다시 읽어야 합니다. */
type DraftState = { key: number; loaded?: ReportDraft; savedAt?: number };

function readDraftState(applicationId: number): DraftState {
  const loaded = loadReportDraft(applicationId);
  return { key: applicationId, loaded, savedAt: loaded?.savedAt };
}

/** 폼의 현재 입력값을 임시 저장용 모양으로 (제출할 때는 여기서 공백만 다듬어 씁니다). */
function readFields(form: HTMLFormElement): ReportDraftFields {
  const data = new FormData(form);
  const value = (name: string) => String(data.get(name) ?? '');
  return { department: value('department'), purpose: value('purpose'), summary: value('summary'), notes: value('notes') };
}

/** 저장 시각을 "분"까지만 보여주므로, 같은 분 안에서 또 저장됐으면 다시 그리지 않습니다. */
function sameMinute(a: number | undefined, b: number | undefined): boolean {
  if (a === undefined || b === undefined) return a === b;
  return Math.floor(a / 60_000) === Math.floor(b / 60_000);
}

/**
 * 동행 보고서 작성 — Figma 동행 매니저_보고서 작성 화면 61:1269
 *
 * 기본 정보는 내 지원 목록·공고에서 가져오고(api.ts 의 fetchReportTarget),
 * 제출은 POST /api/v1/applications/{applicationId}/report 입니다.
 * 형식 검사는 브라우저 기본 검사(required)를 씁니다.
 * 첨부 사진은 서버에 업로드 API 가 없어 미리보기만 보여주고 전송하지 않습니다.
 *
 * 입력값은 칠 때마다 localStorage 에 임시 저장해(model/reportDraft.ts) 다시 들어오면 이어서 쓸 수 있고,
 * 제출에 성공하면 지웁니다. 사진은 브라우저 안에서만 사는 blob 이라 임시 저장 대상이 아닙니다.
 */
export default function ReportWritePage() {
  const { loading: authLoading, user } = useRequireAuth('ESCORT');
  const params = useParams<{ applicationId: string }>();
  const router = useRouter();
  const applicationId = Number(params.applicationId);

  const [result, setResult] = useState<{ key?: number; data?: TargetResult }>({});
  const [photos, setPhotos] = useState<{ name: string; url: string }[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [draft, setDraft] = useState<DraftState>(() => readDraftState(applicationId));

  // 같은 화면이 다른 동행 건으로 재사용될 수 있어, 그때는 그 건의 임시 저장본으로 바꿉니다.
  // 주소가 숫자가 아니면 applicationId 가 NaN 이라, !== 대신 Object.is 로 비교해야 무한 렌더가 안 납니다.
  if (!Object.is(draft.key, applicationId)) setDraft(readDraftState(applicationId));

  // 미리보기 주소는 화면을 떠날 때 정리합니다.
  useEffect(() => () => photos.forEach((photo) => URL.revokeObjectURL(photo.url)), [photos]);

  useEffect(() => {
    if (!Number.isFinite(applicationId)) return;
    let ignore = false;

    fetchReportTarget(applicationId)
      .then((target) => {
        if (!ignore) setResult({ key: applicationId, data: target ? { status: 'ready', target } : { status: 'notFound' } });
      })
      .catch((error: unknown) => {
        if (ignore) return;
        setResult({
          key: applicationId,
          data: { status: 'error', message: error instanceof Error ? error.message : '동행 정보를 불러오지 못했습니다.' },
        });
      });

    return () => {
      ignore = true;
    };
  }, [applicationId]);

  const state = result.key === applicationId ? result.data : undefined;

  if (authLoading) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">불러오는 중입니다...</p>
        </section>
      </AppShell>
    );
  }
  if (!user) return null;

  if (!state) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">동행 정보를 불러오는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (state.status !== 'ready') {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center text-xl font-semibold text-brand">
          {state.status === 'notFound' ? '동행 정보를 찾을 수 없습니다.' : state.message}
        </section>
      </AppShell>
    );
  }

  const escort = state.target;

  const handlePhotos = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []).slice(0, MAX_PHOTOS - photos.length);
    setPhotos((prev) => [...prev, ...files.map((file) => ({ name: file.name, url: URL.createObjectURL(file) }))]);
    event.target.value = '';
  };

  /** 입력할 때마다 임시 저장합니다. 칸이 몇 개뿐이라 따로 지연을 두지 않고 바로 씁니다. */
  const handleDraftChange = (event: FormEvent<HTMLFormElement>) => {
    const savedAt = saveReportDraft(applicationId, readFields(event.currentTarget));
    setDraft((prev) => (sameMinute(prev.savedAt, savedAt) ? prev : { ...prev, savedAt }));
  };

  /** 불러온 임시 저장본을 버리고 빈 폼으로 — form 의 key 가 바뀌면서 입력칸도 비워집니다. */
  const handleDiscardDraft = () => {
    if (!window.confirm('임시 저장된 내용을 지우고 새로 작성하시겠습니까?')) return;
    clearReportDraft(applicationId);
    setDraft({ key: applicationId });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!window.confirm('보고서를 제출하시겠습니까? 제출 후에는 수정할 수 없습니다.')) return;
    setSubmitError('');

    const fields = readFields(event.currentTarget);

    setSubmitting(true);
    try {
      await writeReport(escort.applicationId, {
        department: fields.department.trim(),
        purpose: fields.purpose.trim(),
        originContent: fields.summary.trim(),
        notes: fields.notes.trim(),
      });
      clearReportDraft(applicationId);
      router.push(`/escort/${escort.applicationId}/report/done`);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : '보고서 제출에 실패했습니다.');
      setSubmitting(false);
    }
  };

  return (
    <AppShell>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading
            title="동행 보고서 작성"
            description="실제 동행 내용을 기반으로 보고서를 작성해주세요."
            className="mb-6"
          />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <form
              key={`${applicationId}:${draft.loaded ? 'draft' : 'blank'}`}
              onSubmit={handleSubmit}
              onChange={handleDraftChange}
              className="flex min-w-0 flex-col gap-[34px] rounded-[30px] border border-line bg-white px-6 py-8 shadow-card"
            >
              {draft.loaded && (
                <div className="flex flex-wrap items-center justify-between gap-2.5 rounded-[20px] border border-line bg-line-soft px-5 py-4">
                  <p className="text-sm leading-5 font-medium text-brand">
                    {formatClockTime(new Date(draft.loaded.savedAt))}에 임시 저장한 내용을 불러왔습니다.
                  </p>
                  <button type="button" onClick={handleDiscardDraft} className="text-sm leading-5 font-semibold text-brand underline underline-offset-4">
                    새로 작성하기
                  </button>
                </div>
              )}

              <div>
                <SectionTitle>기본 정보</SectionTitle>
                <div className="rounded-[30px] border border-line bg-line-soft px-5 py-5">
                  <dl className="grid gap-x-[22px] lg:grid-cols-[1fr_1px_1fr]">
                    <div className="flex flex-col gap-[3px]">
                      <InfoRow label="공고 제목" labelWidth={92}>{escort.title}</InfoRow>
                      <InfoRow label="병원명" labelWidth={92}>{escort.hospitalName}</InfoRow>
                      <InfoRow label="동행일" labelWidth={92}>{escort.dateLabel}</InfoRow>
                      <InfoRow label="동행시간" labelWidth={92}>{escort.workTime}</InfoRow>
                    </div>
                    <div aria-hidden="true" className="hidden self-center bg-white opacity-50 lg:block lg:h-40" />
                    {/* ⚠️ 의뢰인명은 백엔드 응답에 없어서 뺐습니다 (model/mapper.ts 주석 참고). */}
                    <div className="flex flex-col gap-[3px]">
                      <InfoRow label="병원 주소" labelWidth={92}>{escort.hospitalAddress}</InfoRow>
                      <InfoRow label="특이사항" labelWidth={92}>{escort.note}</InfoRow>
                    </div>
                  </dl>
                </div>
                <p className="mt-2 flex items-center gap-2.5 px-9 text-sm leading-6 font-medium text-brand">
                  <Image src="/icons/escort/report-info.svg" alt="" width={15.5} height={15.5} className="size-3.5 shrink-0" />
                  위 내용은 공고 정보 및 동행 현황을 기반으로 자동으로 입력된 정보입니다.
                </p>
              </div>

              <div>
                <SectionTitle>진료 내용</SectionTitle>
                <div className="flex flex-col gap-[5px]">
                  <FieldRow label="진료 과목*" htmlFor="report-department">
                    <div className="relative">
                      <select
                        id="report-department"
                        name="department"
                        required
                        defaultValue={draft.loaded?.department ?? ''}
                        className={cn(FIELD, 'h-[61px] appearance-none pr-12 invalid:text-brand-muted')}
                      >
                        <option value="" disabled hidden>선택해주세요</option>
                        {DEPARTMENTS.map((department) => (
                          <option key={department.value} value={department.value} className="text-brand">{department.label}</option>
                        ))}
                      </select>
                      <Image src="/icons/escort/report-select.svg" alt="" width={15.5} height={8.5} className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2" />
                    </div>
                  </FieldRow>
                  <FieldRow label="진료 목적*" htmlFor="report-purpose">
                    <input
                      id="report-purpose"
                      name="purpose"
                      required
                      defaultValue={draft.loaded?.purpose}
                      placeholder="예) 수술 전 검사, 정기 검진 등"
                      className={cn(FIELD, 'h-[61px]')}
                    />
                  </FieldRow>
                  <FieldRow label="진료 내용 요약*" htmlFor="report-summary">
                    <textarea
                      id="report-summary"
                      name="summary"
                      required
                      defaultValue={draft.loaded?.summary}
                      placeholder={'진료 과정과 주요 내용을 작성해주세요.\n(예: 검사 항목, 진료 결과, 의사 소견 등)'}
                      className={cn(FIELD, 'h-[120px] resize-none py-[18px]')}
                    />
                  </FieldRow>
                </div>
              </div>

              <div>
                <SectionTitle>특이사항</SectionTitle>
                <textarea
                  id="report-notes"
                  name="notes"
                  aria-label="특이사항"
                  defaultValue={draft.loaded?.notes}
                  placeholder={'동행 중 특이사항이 있다면 입력해주세요.\n(예: 대기 시간, 추가 검사, 의뢰인 상태, 특이 상황 등)'}
                  className={cn(FIELD, 'h-[120px] resize-none py-[18px]')}
                />
              </div>

              {/* 서버 업로드 API가 없어서 미리보기만 되고 실제로 전송되지 않는 기능이라, 발표 전까지는 숨깁니다. */}
              {showUnimplemented() && (
                <div>
                  <SectionTitle>첨부 사진</SectionTitle>
                  <p className="mb-2 px-2 text-sm leading-5 font-medium text-brand-muted">사진 첨부는 추후 지원 예정입니다. 지금 선택한 사진은 서버로 전송되지 않습니다.</p>
                  <div className="flex flex-wrap items-center gap-2">
                    <label className="flex h-[120px] w-full cursor-pointer flex-col items-center justify-center gap-2.5 rounded-[20px] border border-line-soft bg-line-soft px-4 text-center text-base leading-5 text-brand-muted shadow-card sm:w-[calc(100%-320px)] sm:min-w-[220px] lg:w-[378px]">
                      <Image src="/icons/escort/camera.svg" alt="" width={31.65} height={27.65} />
                      <span>
                        사진을 업로드해주세요.
                        <br />
                        (선택, 최대 {MAX_PHOTOS}장)
                      </span>
                      <input
                        type="file"
                        accept="image/*"
                        multiple
                        disabled={photos.length >= MAX_PHOTOS}
                        onChange={handlePhotos}
                        className="sr-only"
                      />
                    </label>
                    {Array.from({ length: MAX_PHOTOS }, (_, index) => {
                      const photo = photos[index];
                      return photo ? (
                        // eslint-disable-next-line @next/next/no-img-element -- 업로드 미리보기(blob 주소)
                        <img key={photo.url} src={photo.url} alt={photo.name} className="size-[72px] rounded-[20px] object-cover shadow-card" />
                      ) : (
                        <Image key={index} src="/icons/escort/photo-slot.svg" alt="" width={80} height={80} className="size-[72px]" />
                      );
                    })}
                  </div>
                </div>
              )}

              {submitError && (
                <p role="alert" className={ERROR_TEXT}>
                  {submitError}
                </p>
              )}

              <div className="-mb-[22px] flex flex-wrap items-center gap-1.5 px-4 text-sm leading-5 font-medium text-brand-muted">
                <span>작성 중인 내용은 이 브라우저에 임시 저장되어, 다시 들어오면 이어서 쓸 수 있습니다.</span>
                <span aria-live="polite">{draft.savedAt !== undefined && `(${formatClockTime(new Date(draft.savedAt))} 저장됨)`}</span>
              </div>

              <div className="flex gap-[15px]">
                <Link href={`/escort/${escort.applicationId}`} className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft">
                  취소
                </Link>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] bg-brand text-base leading-[18px] font-semibold text-white transition-colors hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {submitting ? '제출 중...' : '제출하기'}
                </button>
              </div>
            </form>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className="rounded-[30px] border border-line bg-line-soft px-6 pt-8 pb-6">
                <h2 className="mb-4 flex items-center gap-[15px] text-2xl leading-6 font-semibold text-brand">
                  <Image src="/icons/escort/warning.svg" alt="" width={31} height={31} className="size-7" />
                  작성 안내
                </h2>
                <ul className="flex flex-col px-3">
                  {GUIDES.map((guide) => (
                    <li key={guide} className="flex items-center gap-[15px] py-1 text-sm leading-6 font-semibold text-brand">
                      <Image src="/icons/escort/bullet.svg" alt="" width={6} height={6} className="shrink-0" />
                      {guide}
                    </li>
                  ))}
                </ul>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-6 py-6 shadow-card">
                <h2 className="mb-4 flex items-center gap-[15px] px-2.5 text-2xl leading-6 font-semibold text-brand">
                  <Image src="/icons/escort/clipboard.svg" alt="" width={29} height={36} className="h-8 w-auto" />
                  작성 예시
                </h2>
                <div className="flex flex-col gap-5 rounded-[30px] border border-line bg-line-soft p-5 text-brand">
                  <div>
                    <p className="text-xl leading-6 font-semibold">진료 요약 예시</p>
                    <p className="mt-2.5 text-sm leading-6 font-medium">
                      “혈액 검사, X-ray 촬영 후 의사 상담을 진행했습니다.
                      <br />
                      검사 결과는 이상 없으며, 3개월 후 재검 예정입니다.”
                    </p>
                  </div>
                  <div>
                    <p className="text-xl leading-6 font-semibold">특이사항 예시</p>
                    <p className="mt-2.5 text-sm leading-6 font-medium">
                      “대기 시간이 예상보다 길어서 약 30분 정도 더 대기했습니다.
                      <br />
                      의뢰인께서 다소 피로해하셔서 중간에 휴식을 취했습니다.”
                    </p>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
