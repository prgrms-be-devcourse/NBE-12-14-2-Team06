'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState, type ChangeEvent, type FormEvent, type ReactNode } from 'react';
import { AppShell } from '@/components/layout';
import { Container, InfoRow, SectionHeading } from '@/components/ui';
import { cn } from '@/lib/cn';
import { MOCK_USER } from '@/lib/mockSession';
import { getEscortCase } from '../model/cases';

const MAX_PHOTOS = 4;
const DEPARTMENTS = ['내과', '외과', '정형외과', '영상의학과', '치과', '안과', '피부과', '기타'];
const GUIDES = [
  '실제 동행한 내용을 바탕으로 작성해주세요.',
  '진료 내용은 구체적으로 작성할수록 좋아요.',
  '의뢰인의 개인정보는 포함하지 말아주세요.',
  '사진은 병원 내부가 노출되지 않도록 주의해주세요.',
  '허위 작성 시 서비스 이용에 제한이 있을 수 있습니다.',
];

const FIELD =
  'w-full rounded-[20px] border border-line-soft bg-white px-4 text-base leading-5 text-brand shadow-card placeholder:text-brand-muted';

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

/**
 * 동행 보고서 작성 — Figma 동행 매니저_보고서 작성 화면 61:1269
 *
 * 형식 검사는 브라우저 기본 검사(required)를 씁니다.
 * ⚠️ 제출해도 서버로 보내지 않고 "제출 완료" 화면으로만 이동합니다. (보고서 API 연결 전)
 */
export default function ReportWritePage() {
  const params = useParams<{ applicationId: string }>();
  const router = useRouter();
  const escort = getEscortCase(Number(params.applicationId));

  const [photos, setPhotos] = useState<{ name: string; url: string }[]>([]);

  // 미리보기 주소는 화면을 떠날 때 정리합니다.
  useEffect(() => () => photos.forEach((photo) => URL.revokeObjectURL(photo.url)), [photos]);

  if (!escort) {
    return (
      <AppShell user={MOCK_USER}>
        <section className="bg-white py-[100px] text-center text-xl font-semibold text-brand">동행 정보를 찾을 수 없습니다.</section>
      </AppShell>
    );
  }

  const handlePhotos = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []).slice(0, MAX_PHOTOS - photos.length);
    setPhotos((prev) => [...prev, ...files.map((file) => ({ name: file.name, url: URL.createObjectURL(file) }))]);
    event.target.value = '';
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    // TODO: 보고서 등록 API 연결 (사진은 파일 업로드 방식 확인 필요)
    router.push(`/escort/${escort.applicationId}/report/done`);
  };

  return (
    <AppShell user={MOCK_USER}>
      <section className="bg-white py-[50px]">
        <Container width="wide">
          <SectionHeading
            title="동행 보고서 작성"
            description="실제 동행 내용을 기반으로 보고서를 작성해주세요."
            className="mb-6"
          />

          <div className="grid items-start gap-[22px] lg:grid-cols-[minmax(0,745px)_minmax(0,511px)] lg:justify-center">
            <form onSubmit={handleSubmit} className="flex min-w-0 flex-col gap-[34px] rounded-[30px] border border-line bg-white px-6 py-8 shadow-card">
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
                    <div className="flex flex-col gap-[3px]">
                      <InfoRow label="의뢰인명" labelWidth={92}>{escort.clientName}</InfoRow>
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
                      <select id="report-department" name="department" required defaultValue="" className={cn(FIELD, 'h-[61px] appearance-none pr-12 invalid:text-brand-muted')}>
                        <option value="" disabled hidden>선택해주세요</option>
                        {DEPARTMENTS.map((department) => (
                          <option key={department} value={department} className="text-brand">{department}</option>
                        ))}
                      </select>
                      <Image src="/icons/escort/report-select.svg" alt="" width={15.5} height={8.5} className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2" />
                    </div>
                  </FieldRow>
                  <FieldRow label="진료 목적*" htmlFor="report-purpose">
                    <input id="report-purpose" name="purpose" required placeholder="예) 수술 전 검사, 정기 검진 등" className={cn(FIELD, 'h-[61px]')} />
                  </FieldRow>
                  <FieldRow label="진료 내용 요약*" htmlFor="report-summary">
                    <textarea
                      id="report-summary"
                      name="summary"
                      required
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
                  placeholder={'동행 중 특이사항이 있다면 입력해주세요.\n(예: 대기 시간, 추가 검사, 의뢰인 상태, 특이 상황 등)'}
                  className={cn(FIELD, 'h-[120px] resize-none py-[18px]')}
                />
              </div>

              <div>
                <SectionTitle>첨부 사진</SectionTitle>
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

              <div className="flex gap-[15px]">
                <Link href={`/escort/${escort.applicationId}`} className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] border border-line bg-white text-base leading-[18px] font-semibold text-brand transition-colors hover:bg-line-soft">
                  취소
                </Link>
                <button type="submit" className="flex h-[55px] flex-1 items-center justify-center rounded-[25px] bg-brand text-base leading-[18px] font-semibold text-white transition-colors hover:bg-brand-hover">
                  제출하기
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
