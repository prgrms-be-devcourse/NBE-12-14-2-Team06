'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState, type FormEvent } from 'react';
import { AppShell } from '@/components/layout';
import { Container, SectionHeading } from '@/components/ui';
import { LOGIN_HOME_BY_ROLE, useRequireAuth } from '@/features/auth';
import { createPost, fetchPostRaw, updatePost, type PostDto, type PostWriteRequest } from '@/features/post';
import { cn } from '@/lib/cn';
import type { PlaceSearchResult } from '@/lib/kakaoMap';
import {
  EMPTY_FORM,
  PARTY_OPTIONS,
  SAMPLE_FORM,
  TIME_OPTIONS,
  TRANSPORT_OPTIONS,
  combineDateTime,
  diffMinutes,
  disabledTimesAtOrAfter,
  disabledTimesAtOrBefore,
  estimateAmount,
  formatMinutes,
  minSelectableTime,
  parsePay,
  splitRegion,
  toIsoDateTime,
  todayDateString,
} from '../model/postForm';
import type { PostFormValues } from '../types';
import AddressSearchField from './form/AddressSearchField';
import { CheckField, FIELD, FormRow, FormSection, SelectField } from './form/fields';

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

/** 백엔드 공고 응답을 수정 폼의 초기값으로 바꿉니다. */
function toFormValues(dto: PostDto): PostFormValues {
  const { region, district } = splitRegion(dto.hospitalAddress);
  return {
    title: dto.title,
    hospitalName: dto.hospitalName,
    hospitalAddress: dto.hospitalAddress,
    hospitalLat: dto.hospitalLat,
    hospitalLng: dto.hospitalLng,
    region,
    district,
    departure: dto.pickupAddress,
    pickupLat: dto.pickupLat,
    pickupLng: dto.pickupLng,
    date: dto.escortStartAt.slice(0, 10),
    startTime: dto.escortStartAt.slice(11, 16),
    endTime: dto.escortEndAt.slice(11, 16),
    hourlyPay: dto.hourlyPay.toLocaleString(),
    // ⚠️ 이동수단·동행인원은 백엔드에 없는 값이라 수정 화면에서 다시 선택해야 합니다.
    transportOut: '',
    transportBack: '',
    party: '',
    reportRequested: dto.reportRequired,
    description: dto.content,
    note: dto.patientNote ?? '',
    recruitStartDate: dto.recruitStartAt.slice(0, 10),
    recruitStartTime: dto.recruitStartAt.slice(11, 16),
    recruitEndDate: dto.recruitEndAt.slice(0, 10),
    recruitEndTime: dto.recruitEndAt.slice(11, 16),
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
 * 등록(POST)·수정(PUT) 모두 실제 백엔드에 연결되어 있습니다.
 * ⚠️ 병원명·출발지는 카카오맵 검색 결과에서 골라야만 위도·경도가 채워집니다 (백엔드가 필수로 요구합니다).
 *    그래서 Figma 의 "지역(시/도·구/군) 선택" 칸은 없앴고, 병원 주소에서 자동으로 뽑습니다.
 * TODO: 이동수단·동행인원은 백엔드에 없는 값이라 서버로 보내지 않습니다.
 *
 * 공고 등록·수정은 의뢰인(CLIENT) 또는 관리자(ADMIN)만 할 수 있습니다(백엔드 PostService.write/modify 와 동일 규칙).
 * ⚠️ 이건 UX 용 가드일 뿐입니다 — 실제 차단은 백엔드가 하고, 여기선 로그인 안 했거나 역할이 안 맞는
 *    사용자에게 폼을 보여줬다가 제출 시점에야 에러를 띄우지 않도록 미리 돌려보내는 역할만 합니다.
 */
export default function PostFormPage() {
  const params = useParams<{ postId?: string }>();
  const postId = params.postId ? Number(params.postId) : undefined;
  const editing = postId !== undefined;
  const router = useRouter();

  // 로그인 안 됐으면 useRequireAuth 가 알아서 /login 으로 보냅니다.
  // role 은 훅이 하나만 받을 수 있어서(CLIENT|ADMIN 둘 다 허용해야 함) 여기서 직접 확인합니다.
  const { user, loading: authLoading, unauthenticated } = useRequireAuth();
  const authorized = !!user && (user.role === 'CLIENT' || user.role === 'ADMIN');

  useEffect(() => {
    if (authLoading || unauthenticated || !user || authorized) return;
    router.replace(LOGIN_HOME_BY_ROLE[user.role]);
  }, [authLoading, unauthenticated, user, authorized, router]);

  // result.postId 로 "지금 postId 의 결과인지" 판단합니다. 새로 작성하는 경우는 서버에서 가져올 게 없어 바로 채웁니다.
  const [result, setResult] = useState<{ postId?: number; initial?: PostFormValues; error?: string } | undefined>(
    () => (editing ? undefined : { postId, initial: EMPTY_FORM }),
  );

  useEffect(() => {
    if (!editing) return;
    let ignore = false;
    fetchPostRaw(postId)
      .then((dto) => !ignore && setResult({ postId, initial: toFormValues(dto) }))
      .catch((error: Error) => !ignore && setResult({ postId, error: error.message }));
    return () => {
      ignore = true;
    };
  }, [editing, postId]);

  const initial = result && result.postId === postId ? result.initial : undefined;
  const loadError = result && result.postId === postId ? result.error : undefined;

  // 로그인 확인 중이거나(스피너), 로그인이 안 됐거나(곧 /login 으로 이동), 역할이 안 맞으면(곧 자기 홈으로 이동)
  // 폼을 그리지 않고 기다립니다.
  if (authLoading || unauthenticated || !authorized) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">확인하는 중입니다.</p>
        </section>
      </AppShell>
    );
  }

  if (!initial) {
    return (
      <AppShell>
        <section className="bg-white py-[100px] text-center">
          <p className="text-xl font-semibold text-brand">{loadError ? `불러오지 못했습니다. (${loadError})` : '불러오는 중입니다.'}</p>
          <Link href="/client/posts" className="mx-auto mt-8 flex h-14 w-60 items-center justify-center rounded-[25px] border border-line text-xl font-semibold text-brand">
            작성한 공고로
          </Link>
        </section>
      </AppShell>
    );
  }

  return <PostFormFields key={postId ?? 'new'} postId={postId} initial={initial} sample={editing ? undefined : SAMPLE_FORM} />;
}

type FormFieldsProps = {
  /** 있으면 수정, 없으면 새로 작성 */
  postId?: number;
  initial: PostFormValues;
  /** 작성 예시 카드에 나올 값 (수정 화면에서는 안 씀) */
  sample?: PostFormValues;
};

function PostFormFields({ postId, initial, sample }: FormFieldsProps) {
  const router = useRouter();
  const editing = postId !== undefined;

  const [hospitalName, setHospitalName] = useState(initial.hospitalName);
  const [hospitalAddress, setHospitalAddress] = useState(initial.hospitalAddress);
  const [hospitalLat, setHospitalLat] = useState(initial.hospitalLat);
  const [hospitalLng, setHospitalLng] = useState(initial.hospitalLng);
  const [region, setRegion] = useState(initial.region);

  const [departure, setDeparture] = useState(initial.departure);
  const [pickupLat, setPickupLat] = useState(initial.pickupLat);
  const [pickupLng, setPickupLng] = useState(initial.pickupLng);

  const [date, setDate] = useState(initial.date);
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

  // 동행 날짜가 오늘이면 지금 이전 시간은 "시작 시간" 에서 아예 선택 못하게 막습니다.
  const startTimeBound = minSelectableTime(date);
  const startDisabled = startTimeBound ? TIME_OPTIONS.filter((option) => option <= startTimeBound) : [];
  // "예상 종료 시간" 은 시작 시간보다 늦어야 하니, 시작 시간(없으면 위 기준 시각) 이하는 막습니다.
  const endTimeBound = startTime || startTimeBound;
  const endDisabled = endTimeBound ? TIME_OPTIONS.filter((option) => option <= endTimeBound) : [];

  const handleDateChange = (value: string) => {
    setDate(value);
    const bound = minSelectableTime(value);
    const effectiveStartTime = bound && startTime && startTime <= bound ? '' : startTime;
    if (effectiveStartTime !== startTime) setStartTime('');
    if (bound && endTime && endTime <= bound) setEndTime('');

    // 모집 일정은 항상 동행 시작보다 이전이어야 합니다. 동행 날짜가 당겨져 더 이상 안 맞으면 초기화합니다.
    const escortStartAt = combineDateTime(value, effectiveStartTime);
    if (escortStartAt && combineDateTime(recruitEndDate, recruitEndTime) >= escortStartAt) {
      setRecruitEndDate('');
      setRecruitEndTime('');
    }
    if (escortStartAt && combineDateTime(recruitStartDate, recruitStartTime) >= escortStartAt) {
      setRecruitStartDate('');
      setRecruitStartTime('');
      setRecruitEndDate('');
      setRecruitEndTime('');
    }
  };

  const handleStartTimeChange = (value: string) => {
    setStartTime(value);
    if (endTime && endTime <= value) setEndTime('');

    const escortStartAt = combineDateTime(date, value);
    if (escortStartAt && combineDateTime(recruitEndDate, recruitEndTime) >= escortStartAt) {
      setRecruitEndTime('');
    }
    if (escortStartAt && combineDateTime(recruitStartDate, recruitStartTime) >= escortStartAt) {
      setRecruitStartTime('');
      setRecruitEndTime('');
    }
  };

  // 모집 시작: 오늘 이전 날짜·시간은 선택 못하게(백엔드: 모집 시작은 현재 시간보다 이후), 동행 시작보다는 항상 빨라야 합니다.
  const recruitStartTimeBound = minSelectableTime(recruitStartDate);
  const recruitStartDisabled = Array.from(
    new Set([
      ...(recruitStartTimeBound ? TIME_OPTIONS.filter((option) => option <= recruitStartTimeBound) : []),
      ...disabledTimesAtOrAfter(recruitStartDate, date, startTime),
    ]),
  );

  const handleRecruitStartDateChange = (value: string) => {
    setRecruitStartDate(value);
    const bound = minSelectableTime(value);
    const effectiveRecruitStartTime = bound && recruitStartTime && recruitStartTime <= bound ? '' : recruitStartTime;
    if (effectiveRecruitStartTime !== recruitStartTime) setRecruitStartTime('');

    const recruitStartAt = combineDateTime(value, effectiveRecruitStartTime);
    if (recruitStartAt && combineDateTime(recruitEndDate, recruitEndTime) <= recruitStartAt) {
      setRecruitEndDate('');
      setRecruitEndTime('');
    }
  };

  const handleRecruitStartTimeChange = (value: string) => {
    setRecruitStartTime(value);
    const recruitStartAt = combineDateTime(recruitStartDate, value);
    if (recruitStartAt && combineDateTime(recruitEndDate, recruitEndTime) <= recruitStartAt) {
      setRecruitEndTime('');
    }
  };

  // 모집 마감: 모집 시작보다 늦어야 하고, 동행 시작보다는 빨라야 합니다(백엔드: 모집 마감 < 동행 시작).
  const recruitEndDisabled = Array.from(
    new Set([...disabledTimesAtOrBefore(recruitEndDate, recruitStartDate, recruitStartTime), ...disabledTimesAtOrAfter(recruitEndDate, date, startTime)]),
  );

  const handleRecruitEndDateChange = (value: string) => {
    setRecruitEndDate(value);
    if (recruitEndTime) {
      const invalid =
        disabledTimesAtOrBefore(value, recruitStartDate, recruitStartTime).includes(recruitEndTime) ||
        disabledTimesAtOrAfter(value, date, startTime).includes(recruitEndTime);
      if (invalid) setRecruitEndTime('');
    }
  };

  let recruitError = '';
  if (recruitStartDate && recruitStartTime) {
    const bound = minSelectableTime(recruitStartDate);
    if (bound && recruitStartTime <= bound) {
      recruitError = '모집 시작 시간은 현재 시간보다 이후여야 합니다.';
    }
  }
  if (!recruitError && recruitStartDate && recruitEndDate) {
    if (recruitEndDate < recruitStartDate) {
      recruitError = '모집 종료일은 시작일보다 빠를 수 없습니다.';
    } else if (recruitEndDate === recruitStartDate && recruitStartTime && recruitEndTime && recruitEndTime <= recruitStartTime) {
      recruitError = '모집 종료는 모집 시작보다 늦어야 합니다.';
    }
  }
  if (!recruitError && date && recruitEndDate) {
    if (recruitEndDate > date) {
      recruitError = '모집 마감일은 동행 날짜보다 늦을 수 없습니다.';
    } else if (recruitEndDate === date && startTime && recruitEndTime && recruitEndTime >= startTime) {
      recruitError = '모집 마감 시간은 동행 시작 시간보다 빨라야 합니다.';
    }
  }

  const handleHospitalSelect = (place: PlaceSearchResult) => {
    setHospitalName(place.name || hospitalName);
    setHospitalAddress(place.address);
    setHospitalLat(place.lat);
    setHospitalLng(place.lng);
    setRegion(splitRegion(place.address).region);
  };

  const handleDepartureSelect = (place: PlaceSearchResult) => {
    setDeparture(place.address);
    setPickupLat(place.lat);
    setPickupLng(place.lng);
  };

  const locationError =
    hospitalLat === null || hospitalLng === null
      ? '병원을 검색 결과에서 선택해주세요.'
      : pickupLat === null || pickupLng === null
        ? '출발지를 검색 결과에서 선택해주세요.'
        : '';

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError('');

    if (hospitalLat === null || hospitalLng === null || pickupLat === null || pickupLng === null) {
      setSubmitError(locationError || '병원·출발지의 위치가 확인되지 않았습니다.');
      return;
    }

    // 폼에 이름(name)이 붙은 입력값을 전부 가져옵니다 (state로 관리하지 않는 필드 포함).
    const form = new FormData(event.currentTarget);
    const value = (name: string) => String(form.get(name) ?? '').trim();

    const payload: PostWriteRequest = {
      title: value('title'),
      content: value('description'),
      region,
      hospitalName,
      hospitalAddress,
      hospitalLat,
      hospitalLng,
      pickupAddress: departure,
      pickupLat,
      pickupLng,
      hourlyPay: parsePay(hourlyPay),
      recruitStartAt: toIsoDateTime(recruitStartDate, recruitStartTime),
      recruitEndAt: toIsoDateTime(recruitEndDate, recruitEndTime),
      escortStartAt: toIsoDateTime(date, startTime),
      escortEndAt: toIsoDateTime(date, endTime),
      patientNote: value('note') || null,
      reportRequired: form.get('reportRequested') === 'on',
    };

    setSubmitting(true);
    try {
      if (editing) {
        // TODO: 수정 API(PUT /api/v1/posts/{postId})는 작성자 본인 로그인 쿠키가 있어야 하고, 모집 시작 전까지만 됩니다.
        await updatePost(postId, payload);
        router.push(`/client/posts/${postId}`);
      } else {
        // TODO: 등록 API(POST /api/v1/posts)는 의뢰인(CLIENT) 로그인 쿠키가 있어야 합니다.
        const created = await createPost(payload);
        // paymentId 는 공고 등록 시 함께 생성된 결제(Payment) 의 id 입니다. 결제 승인 때 필요합니다.
        const query = new URLSearchParams({
          postId: String(created.id),
          paymentId: String(created.paymentId),
          amount: String(amount),
          pay: String(estimateAmount(hourlyPay, 60)),
        });
        router.push(`/client/posts/new/payment?${query.toString()}`);
      }
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : editing ? '공고 수정에 실패했습니다.' : '공고 등록에 실패했습니다.');
      setSubmitting(false);
    }
  };

  return (
    <AppShell>
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
                  <AddressSearchField
                    id="post-hospital"
                    placeholder="병원명을 검색해주세요. (예: 서울아산병원)"
                    defaultValue={initial.hospitalName}
                    formatSelected={(place) => place.name}
                    onSelect={handleHospitalSelect}
                  />
                </FormRow>
                {hospitalAddress && <p className="px-4 text-sm font-medium text-brand-muted">주소: {hospitalAddress}</p>}
                <FormRow label="출발지*" htmlFor="post-departure">
                  <AddressSearchField
                    id="post-departure"
                    placeholder="출발지를 검색해주세요. (예: 자택 주소)"
                    defaultValue={initial.departure}
                    onSelect={handleDepartureSelect}
                  />
                </FormRow>
              </FormSection>

              <FormSection title="일정 정보">
                <FormRow label="동행 날짜*" htmlFor="post-date">
                  <input
                    id="post-date"
                    name="date"
                    type="date"
                    required
                    min={todayDateString()}
                    value={date}
                    onChange={(event) => handleDateChange(event.target.value)}
                    className={cn(FIELD, 'sm:w-[201px]')}
                  />
                </FormRow>
                <div className="grid gap-[5px] sm:grid-cols-2 sm:gap-x-[11px]">
                  <FormRow label="시작 시간*" htmlFor="post-start">
                    <SelectField
                      id="post-start"
                      name="startTime"
                      placeholder="시간 선택"
                      options={TIME_OPTIONS}
                      value={startTime}
                      onChange={handleStartTimeChange}
                      disabledOptions={startDisabled}
                      required
                    />
                  </FormRow>
                  <FormRow label="예상 종료 시간*" htmlFor="post-end">
                    <SelectField
                      id="post-end"
                      name="endTime"
                      placeholder="시간 선택"
                      options={TIME_OPTIONS}
                      value={endTime}
                      onChange={setEndTime}
                      disabledOptions={endDisabled}
                      required
                      customMessage={timeError}
                    />
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
                  <div className="relative min-w-0 sm:max-w-[389px]">
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
                    <input
                      id="recruit-start-date"
                      name="recruitStartDate"
                      type="date"
                      required
                      min={todayDateString()}
                      max={date || undefined}
                      value={recruitStartDate}
                      onChange={(event) => handleRecruitStartDateChange(event.target.value)}
                      className={FIELD}
                    />
                  </FormRow>
                  <FormRow label="종료 날짜*" htmlFor="recruit-end-date">
                    <input
                      id="recruit-end-date"
                      name="recruitEndDate"
                      type="date"
                      required
                      min={recruitStartDate || todayDateString()}
                      max={date || undefined}
                      value={recruitEndDate}
                      onChange={(event) => handleRecruitEndDateChange(event.target.value)}
                      ref={(element) => element?.setCustomValidity(recruitError)}
                      className={FIELD}
                    />
                  </FormRow>
                  <FormRow label="시작 시간*" htmlFor="recruit-start-time">
                    <SelectField
                      id="recruit-start-time"
                      name="recruitStartTime"
                      placeholder="시간 선택"
                      options={TIME_OPTIONS}
                      value={recruitStartTime}
                      onChange={handleRecruitStartTimeChange}
                      disabledOptions={recruitStartDisabled}
                      required
                    />
                  </FormRow>
                  <FormRow label="종료 시간*" htmlFor="recruit-end-time">
                    <SelectField
                      id="recruit-end-time"
                      name="recruitEndTime"
                      placeholder="시간 선택"
                      options={TIME_OPTIONS}
                      value={recruitEndTime}
                      onChange={setRecruitEndTime}
                      disabledOptions={recruitEndDisabled}
                      required
                    />
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
                  {submitting ? (editing ? '수정 중...' : '등록 중...') : editing ? '수정하기' : '등록하기'}
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

              {sample && (
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
              )}

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
