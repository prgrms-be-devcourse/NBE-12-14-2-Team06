'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { getPostDetail } from '@/features/post';
import { api } from '@/lib/api';
import { cn } from '@/lib/cn';
import { MOCK_CLIENT } from '@/lib/mockSession';
import { REGIONS } from '@/lib/regions';
import {
  DEFAULT_DISTRICTS,
  DISTRICTS,
  EMPTY_FORM,
  PARTY_OPTIONS,
  SAMPLE_FORM,
  TIME_OPTIONS,
  TRANSPORT_OPTIONS,
  diffMinutes,
  estimateAmount,
  formatMinutes,
  parsePay,
  regionCenter,
  toIsoDateTime,
} from '../model/postForm';
import type { PostFormValues } from '../types';
import { CheckField, FIELD, FormRow, FormSection, SearchField, SelectField } from './form/fields';

const GUIDES = [
  '정확한 병원명과 일정을 입력해주세요.',
  '자세히 작성할수록 적합한 매니저가 지원합니다.',
  '시급은 지역과 소요 시간을 고려하여 설정해주세요.',
  '개인 정보(연락처, 주민번호 등)는 작성하지 말아주세요.',
];

const EXAMPLES = [
  { title: '공고 제목 예시', body: '수술 전 검사 동행이 필요합니다.' },
  {
    title: '공고 설명 예시',
    body: '“9월 22일 오전 9시에 삼성서울병원에서 수술 전 검사와 X-ray 촬영이 있습니다. 검사 결과에 대한 의사 상담까지 동행이 필요합니다. 약 3시간 정도 소요될 것 같습니다.”',
  },
  {
    title: '특이사항 예시',
    body: '“대기 시간이 길 수 있습니다. 중간에 간단한 식사를 할 예정이며, 주차는 병원 주차장 이용 가능합니다.”',
  },
];

const ERROR_TEXT = 'px-4 text-sm leading-5 font-medium text-[#b91d1d]';
const LIST_ITEM = 'flex items-center gap-[15px] py-1 text-sm leading-6 font-semibold text-brand';
const CARD_TITLE = 'mb-4 flex items-center gap-[15px] text-2xl leading-6 font-semibold text-brand';

/** 수정 화면에 채워 넣을 값. 공고 상세 모의 데이터가 있으면 그 값을 우선합니다. */
function initialValues(postId?: number): PostFormValues {
  if (postId === undefined) return EMPTY_FORM;
  const post = getPostDetail(postId);
  if (!post) return SAMPLE_FORM;
  return {
    ...SAMPLE_FORM,
    title: post.title,
    hospitalName: post.hospitalName,
    region: post.region,
    district: post.district,
    hourlyPay: post.hourlyPay.toLocaleString(),
    description: post.details.join('\n'),
  };
}

/** 숫자만 남기고 "14,000" 처럼 콤마를 붙입니다. */
function formatPay(value: string): string {
  const digits = value.replace(/[^0-9]/g, '');
  return digits ? Number(digits).toLocaleString() : '';
}

/**
 * 공고 작성 / 수정 — Figma 의뢰인_공고 작성 61:1273 · 입력 예시 506:2944
 *
 * 형식 검사는 브라우저 기본 검사(required)를 씁니다.
 * 등록(POST /api/v1/posts)은 실제 백엔드에 연결되어 있습니다. 로그인(JWT 쿠키)이 아직 없어서
 * 지금은 401("로그인 후 이용해주세요.")이 정상입니다 — 로그인이 붙으면 그대로 동작합니다.
 * ⚠️ 병원명/출발지 입력칸이 아직 주소 검색 연동 전이라 위도·경도는 선택 지역의 중심 좌표로 대체합니다.
 * ⚠️ 수정(PATCH)은 아직 미연결이라 그대로 상세 화면으로만 이동합니다.
 */
export default function PostFormPage() {
  const params = useParams<{ postId?: string }>();
  const router = useRouter();
  const postId = params.postId ? Number(params.postId) : undefined;
  const editing = postId !== undefined;
  const initial = initialValues(postId);

  const [region, setRegion] = useState(initial.region);
  const [district, setDistrict] = useState(initial.district);
  const [startTime, setStartTime] = useState(initial.startTime);
  const [endTime, setEndTime] = useState(initial.endTime);
  const [hourlyPay, setHourlyPay] = useState(initial.hourlyPay);
  const [recruitStartDate, setRecruitStartDate] = useState(initial.recruitStartDate);
  const [recruitStartTime, setRecruitStartTime] = useState(initial.recruitStartTime);
  const [recruitEndDate, setRecruitEndDate] = useState(initial.recruitEndDate);
  const [recruitEndTime, setRecruitEndTime] = useState(initial.recruitEndTime);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const minutes = diffMinutes(startTime, endTime);
  const amount = estimateAmount(hourlyPay, minutes);
  const timeError = startTime && endTime && minutes <= 0 ? '종료 시간은 시작 시간보다 늦어야 합니다.' : '';

  let recruitError = '';
  if (recruitStartDate && recruitEndDate) {
    if (recruitEndDate < recruitStartDate) {
      recruitError = '모집 종료일은 시작일보다 빠를 수 없습니다.';
    } else if (recruitEndDate === recruitStartDate && recruitStartTime && recruitEndTime && recruitEndTime <= recruitStartTime) {
      recruitError = '모집 종료는 모집 시작보다 늦어야 합니다.';
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (editing) {
      // TODO: 공고 수정 API(PATCH /api/v1/posts/{postId}) 연결
      router.push(`/client/posts/${postId}`);
      return;
    }

    setSubmitError('');

    // 폼에 이름(name)이 붙은 입력값을 전부 가져옵니다 (state로 관리하지 않는 필드 포함).
    const form = new FormData(event.currentTarget);
    const value = (name: string) => String(form.get(name) ?? '').trim();

    const center = regionCenter(region);
    const hospitalAddress = [region, district].filter(Boolean).join(' ') || value('hospitalName');
    const pickupAddress = value('departure');

    const payload = {
      title: value('title'),
      content: value('description'),
      region,
      hospitalName: value('hospitalName'),
      hospitalAddress,
      hospitalLat: center.lat,
      hospitalLng: center.lng,
      pickupAddress,
      pickupLat: center.lat,
      pickupLng: center.lng,
      hourlyPay: parsePay(hourlyPay),
      recruitStartAt: toIsoDateTime(recruitStartDate, recruitStartTime),
      recruitEndAt: toIsoDateTime(recruitEndDate, recruitEndTime),
      escortStartAt: toIsoDateTime(value('date'), startTime),
      escortEndAt: toIsoDateTime(value('date'), endTime),
      patientNote: value('note') || null,
      reportRequired: form.get('reportRequested') === 'on',
    };

    setSubmitting(true);
    try {
      const created = await api<{ id: number }>('/api/v1/posts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const query = new URLSearchParams({
        postId: String(created.id),
        amount: String(amount),
        pay: String(estimateAmount(hourlyPay, 60)),
      });
      router.push(`/client/posts/new/payment?${query.toString()}`);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : '공고 등록에 실패했습니다.');
      setSubmitting(false);
    }
  };

  return (
    <AppShell user={MOCK_CLIENT}>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading
            title={editing ? '공고 수정' : '공고 작성'}
            description="필요한 동행 내용을 작성하여 동행 매니저를 모집해보세요."
            className="mb-6"
          />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <form onSubmit={handleSubmit} className="flex min-w-0 flex-col gap-10 rounded-[30px] border border-line bg-white px-6 py-8 shadow-card">
              <FormSection title="기본 정보">
                <FormRow label="공고 제목*" htmlFor="post-title">
                  <input id="post-title" name="title" required defaultValue={initial.title} placeholder="예) 수술 전 검사 동행이 필요합니다." className={FIELD} />
                </FormRow>
                <FormRow label="병원명*" htmlFor="post-hospital">
                  <SearchField id="post-hospital" name="hospitalName" placeholder="병원명을 입력해주세요." defaultValue={initial.hospitalName} />
                </FormRow>
                <FormRow label="지역*" htmlFor="post-region">
                  <div className="grid gap-2.5 sm:grid-cols-2">
                    <SelectField
                      id="post-region"
                      name="region"
                      placeholder="시/도 선택"
                      options={[...REGIONS]}
                      value={region}
                      onChange={(value) => {
                        setRegion(value);
                        setDistrict('');
                      }}
                      required
                    />
                    <SelectField
                      id="post-district"
                      name="district"
                      placeholder="구/군 선택"
                      options={DISTRICTS[region] ?? DEFAULT_DISTRICTS}
                      value={district}
                      onChange={setDistrict}
                      disabled={!region}
                      required
                    />
                  </div>
                </FormRow>
                <FormRow label="출발지*" htmlFor="post-departure">
                  <SearchField id="post-departure" name="departure" placeholder="출발지를 입력해주세요." defaultValue={initial.departure} />
                </FormRow>
              </FormSection>

              <FormSection title="일정 정보">
                <FormRow label="동행 날짜*" htmlFor="post-date">
                  <input id="post-date" name="date" type="date" required defaultValue={initial.date} className={cn(FIELD, 'sm:w-[201px]')} />
                </FormRow>
                <div className="grid gap-[5px] sm:grid-cols-2 sm:gap-x-[11px]">
                  <FormRow label="시작 시간*" htmlFor="post-start">
                    <SelectField id="post-start" name="startTime" placeholder="시간 선택" options={TIME_OPTIONS} value={startTime} onChange={setStartTime} required />
                  </FormRow>
                  <FormRow label="예상 종료 시간*" htmlFor="post-end">
                    <SelectField id="post-end" name="endTime" placeholder="시간 선택" options={TIME_OPTIONS} value={endTime} onChange={setEndTime} required customMessage={timeError} />
                  </FormRow>
                </div>
                <FormRow label="예상 소요 시간" htmlFor="post-duration">
                  <input
                    id="post-duration"
                    readOnly
                    tabIndex={-1}
                    value={formatMinutes(minutes)}
                    placeholder="예) 3시간 5분"
                    className={cn(FIELD, 'sm:w-[201px]')}
                  />
                </FormRow>
                {timeError && (
                  <p role="alert" className={ERROR_TEXT}>
                    {timeError}
                  </p>
                )}
              </FormSection>

              <FormSection title="요청 조건">
                <FormRow label="시급(금액)*" htmlFor="post-pay">
                  <div className="flex flex-wrap items-center gap-x-6 gap-y-2">
                    <div className="relative min-w-0 flex-1 sm:max-w-[389px]">
                      <input
                        id="post-pay"
                        name="hourlyPay"
                        required
                        inputMode="numeric"
                        value={hourlyPay}
                        onChange={(event) => setHourlyPay(formatPay(event.target.value))}
                        placeholder="예) 12,000"
                        className={cn(FIELD, 'pr-12')}
                      />
                      <span className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2 text-base text-brand-muted">원</span>
                    </div>
                    <CheckField name="negotiable" label="협의 가능" defaultChecked={initial.negotiable} />
                  </div>
                </FormRow>
                <FormRow label="이동수단*" htmlFor="post-transport-out">
                  <div className="grid gap-2.5 sm:grid-cols-2">
                    <SelectField id="post-transport-out" name="transportOut" placeholder="출발 시 이동수단을 선택해주세요" options={TRANSPORT_OPTIONS} defaultValue={initial.transportOut} required />
                    <SelectField id="post-transport-back" name="transportBack" placeholder="복귀 시 이동수단을 선택해주세요" options={TRANSPORT_OPTIONS} defaultValue={initial.transportBack} required />
                  </div>
                </FormRow>
                <FormRow label="동행인원" htmlFor="post-party">
                  <div className="flex flex-wrap items-center gap-x-5 gap-y-2">
                    <div className="w-full sm:w-[273px]">
                      <SelectField id="post-party" name="party" placeholder="1명 (본인만)" options={PARTY_OPTIONS} defaultValue={initial.party} />
                    </div>
                    <p className="text-base leading-5 text-brand-muted">본인을 포함한 총 인원입니다.</p>
                  </div>
                </FormRow>
                <FormRow label="보고서 요청" className="sm:min-h-[61px]">
                  <CheckField name="reportRequested" label="요청" defaultChecked={initial.reportRequested} />
                </FormRow>
              </FormSection>

              <FormSection title="상세 내용">
                <FormRow label="공고 설명*" htmlFor="post-description" className="sm:items-start">
                  <textarea
                    id="post-description"
                    name="description"
                    required
                    defaultValue={initial.description}
                    placeholder={'동행이 필요한 상황을 자세히 작성해주세요.\n(예: 어떤 진료인지, 어떤 도움이 필요한지 등)'}
                    className={cn(FIELD, 'h-[120px] resize-none py-[18px]')}
                  />
                </FormRow>
                <FormRow label="특이사항" htmlFor="post-note" className="sm:items-start">
                  <textarea
                    id="post-note"
                    name="note"
                    defaultValue={initial.note}
                    placeholder={'추가로 전달하고 싶은 내용이 있다면 작성해주세요.\n(예: 대기 시간, 준비물, 주차 가능 여부 등)'}
                    className={cn(FIELD, 'h-[120px] resize-none py-[18px]')}
                  />
                </FormRow>
              </FormSection>

              <FormSection title="모집 일정">
                <div className="grid gap-[5px] sm:grid-cols-2 sm:gap-x-[11px]">
                  <FormRow label="시작 날짜*" htmlFor="recruit-start-date">
                    <input id="recruit-start-date" name="recruitStartDate" type="date" required value={recruitStartDate} onChange={(event) => setRecruitStartDate(event.target.value)} className={FIELD} />
                  </FormRow>
                  <FormRow label="종료 날짜*" htmlFor="recruit-end-date">
                    <input
                      id="recruit-end-date"
                      name="recruitEndDate"
                      type="date"
                      required
                      value={recruitEndDate}
                      onChange={(event) => setRecruitEndDate(event.target.value)}
                      ref={(element) => element?.setCustomValidity(recruitError)}
                      className={FIELD}
                    />
                  </FormRow>
                  <FormRow label="시작 시간*" htmlFor="recruit-start-time">
                    <SelectField id="recruit-start-time" name="recruitStartTime" placeholder="시간 선택" options={TIME_OPTIONS} value={recruitStartTime} onChange={setRecruitStartTime} required />
                  </FormRow>
                  <FormRow label="종료 시간*" htmlFor="recruit-end-time">
                    <SelectField id="recruit-end-time" name="recruitEndTime" placeholder="시간 선택" options={TIME_OPTIONS} value={recruitEndTime} onChange={setRecruitEndTime} required />
                  </FormRow>
                </div>
                {recruitError && (
                  <p role="alert" className={ERROR_TEXT}>
                    {recruitError}
                  </p>
                )}
              </FormSection>

              {submitError && (
                <p role="alert" className={ERROR_TEXT}>
                  {submitError}
                </p>
              )}

              <div className="flex gap-2.5">
                <Link
                  href={editing ? `/client/posts/${postId}` : '/client/posts'}
                  className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] border border-line bg-white text-xl leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft"
                >
                  취소
                </Link>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] bg-brand text-base leading-[18px] font-semibold text-white transition-colors hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {submitting ? '등록 중...' : editing ? '수정하기' : '등록하기'}
                </button>
              </div>
            </form>

            <div className="flex min-w-0 flex-col gap-[22px]">
              <section className="rounded-[30px] border border-line bg-line-soft px-6 pt-8 pb-6">
                <h2 className={CARD_TITLE}>
                  <Image src="/icons/escort/warning.svg" alt="" width={31} height={31} className="size-7" />
                  작성 시 참고해주세요!
                </h2>
                <ul className="flex flex-col px-3">
                  {GUIDES.map((guide) => (
                    <li key={guide} className={LIST_ITEM}>
                      <Image src="/icons/escort/bullet.svg" alt="" width={6} height={6} className="shrink-0" />
                      {guide}
                    </li>
                  ))}
                </ul>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-6 py-8 shadow-card">
                <h2 className="mb-2.5 flex items-center gap-[15px] px-2.5 text-2xl leading-6 font-semibold text-brand">
                  <Image src="/icons/escort/clipboard.svg" alt="" width={29} height={36} className="h-8 w-auto" />
                  작성 예시
                </h2>
                <div className="flex flex-col gap-2.5">
                  {EXAMPLES.map((example) => (
                    <div key={example.title} className="flex flex-col gap-[15px] rounded-[30px] border border-line bg-line-soft p-5 text-brand">
                      <p className="text-xl leading-6 font-semibold">{example.title}</p>
                      <p className="text-sm leading-5 font-medium">{example.body}</p>
                    </div>
                  ))}
                </div>
              </section>

              <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6 shadow-card lg:min-h-[232px]">
                <h2 className={CARD_TITLE}>
                  <Image src="/icons/escort/warning.svg" alt="" width={31} height={31} className="size-7" />
                  결제 예상 금액
                </h2>
                <ul className="flex flex-col px-3">
                  <li className={LIST_ITEM}>
                    <Image src="/icons/escort/bullet.svg" alt="" width={6} height={6} className="shrink-0" />
                    {amount.toLocaleString()}원
                  </li>
                  <li className={LIST_ITEM}>
                    <Image src="/icons/escort/bullet.svg" alt="" width={6} height={6} className="shrink-0" />
                    실제 동행 종료 시간에 따라 추가 결제 또는 부분 취소가 발생할 수 있습니다.
                  </li>
                </ul>
              </section>
            </div>
          </div>
        </Container>
      </section>
    </AppShell>
  );
}
