'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState, type FormEvent, type ReactNode } from 'react';
import type { CurrentUser } from '@/features/auth';
import { fetchMyProfile } from '@/features/auth';
import { ApiError } from '@/lib/api';
import { cn } from '@/lib/cn';
import { REGIONS } from '@/lib/regions';
import {
  createMyEscortProfile,
  fetchMyEscortProfile,
  updateMyEscortProfile,
  updateMyProfile,
  type EscortProfileWriteRequest,
} from '../api';
import type { EscortProfileDto } from '../types';
import InfoCard from './InfoCard';

const FIELD = 'h-[47px] w-full rounded-[10px] border border-line-soft bg-white px-4 text-sm text-brand shadow-card placeholder:text-brand-muted';
const LABEL = 'mb-1.5 block text-sm font-semibold text-brand';
const ERROR_TEXT = 'text-sm font-medium text-[#b91d1d]';
const GENDER_LABEL: Record<CurrentUser['gender'], string> = { MALE: '남', FEMALE: '여' };

/** "2026-09-22T10:00:00" · "2026-09-22" 를 <input type="date"> 가 먹는 "2026-09-22" 로 */
function toDateInput(value: string): string {
  return value.slice(0, 10);
}

/** 404(아직 프로필을 만든 적 없음)인지 확인 */
function isNotFound(error: unknown): boolean {
  // 500 등 RsData 형식이 아닌 응답(스프링 기본 에러 페이지)이면 statusCode 가 없을 수 있어 옵셔널 체이닝으로 방어합니다.
  return error instanceof ApiError && (error.statusCode?.startsWith('404') ?? false);
}

/** "기본 정보" 카드 — 조회 + 수정 (PATCH /api/v1/users/profile) */
function BasicInfoCard({ user, onUpdated }: { user: CurrentUser; onUpdated: (user: CurrentUser) => void }) {
  const [editing, setEditing] = useState(false);
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState(user.email);
  const [name, setName] = useState(user.name);
  const [birthDate, setBirthDate] = useState(toDateInput(user.birthDate));
  const [phoneNum, setPhoneNum] = useState(user.phoneNum);
  const [region, setRegion] = useState(user.region);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const startEdit = () => {
    setPassword('');
    setEmail(user.email);
    setName(user.name);
    setBirthDate(toDateInput(user.birthDate));
    setPhoneNum(user.phoneNum);
    setRegion(user.region);
    setError('');
    setEditing(true);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      // TODO: 백엔드가 정보 수정 때마다 현재 비밀번호를 다시 요구합니다.
      const updated = await updateMyProfile({ password, email, name, birthDate, phoneNum, region });
      onUpdated(updated);
      setEditing(false);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '수정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (!editing) {
    return (
      <InfoCard title="기본 정보" action="수정하기" onAction={startEdit} className="lg:min-h-[440px]">
        <dl className="mt-[30px] flex flex-col gap-[3px]">
          {[
            ['아이디', user.username],
            ['이메일', user.email],
            ['실명', user.name],
            ['생년월일', user.birthDate],
            ['성별', GENDER_LABEL[user.gender]],
            ['전화번호', user.phoneNum],
            ['거주 지역', user.region],
          ].map(([label, value]) => (
            <div key={label} className="flex min-h-[47px] items-center gap-2.5">
              <dt className="w-[92px] shrink-0 px-4 text-base leading-5 font-semibold text-brand">{label}</dt>
              <dd className="min-w-0 flex-1 px-4 text-sm leading-5 font-medium text-brand">{value}</dd>
            </div>
          ))}
        </dl>
      </InfoCard>
    );
  }

  return (
    <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6 shadow-card lg:min-h-[440px]">
      <h2 className="mb-4 text-2xl leading-6 font-semibold text-brand">기본 정보 수정</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <p className="text-xs font-medium text-brand-muted">아이디·성별은 바꿀 수 없습니다.</p>
        <div>
          <label className={LABEL} htmlFor="edit-email">이메일</label>
          <input id="edit-email" type="email" required value={email} onChange={(event) => setEmail(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-name">실명</label>
          <input id="edit-name" required value={name} onChange={(event) => setName(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-birth">생년월일</label>
          <input id="edit-birth" type="date" required value={birthDate} onChange={(event) => setBirthDate(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-phone">전화번호</label>
          <input id="edit-phone" required value={phoneNum} onChange={(event) => setPhoneNum(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-region">거주 지역</label>
          <select id="edit-region" required value={region} onChange={(event) => setRegion(event.target.value)} className={FIELD}>
            {REGIONS.map((item) => (
              <option key={item} value={item}>{item}</option>
            ))}
          </select>
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-password">현재 비밀번호</label>
          <input id="edit-password" type="password" required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="정보를 바꾸려면 비밀번호를 다시 입력해주세요." className={FIELD} />
        </div>
        {error && <p className={ERROR_TEXT}>{error}</p>}
        <div className="mt-2 flex gap-2.5">
          <button type="button" onClick={() => setEditing(false)} className="h-11 flex-1 rounded-[20px] border border-line text-sm font-semibold text-brand transition-colors hover:bg-line-soft">취소</button>
          <button type="submit" disabled={saving} className="h-11 flex-1 rounded-[20px] bg-brand text-sm font-semibold text-white transition-colors hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-60">
            {saving ? '저장 중…' : '저장'}
          </button>
        </div>
      </form>
    </section>
  );
}

/** "추가 정보" 카드 — 자기소개·정산 계좌 (GET/POST/PUT /api/v1/users/profile/escort) */
function EscortProfileCard({ profile, onSaved }: { profile: EscortProfileDto | null; onSaved: (profile: EscortProfileDto) => void }) {
  const [editing, setEditing] = useState(profile === null);
  const [intro, setIntro] = useState(profile?.intro ?? '');
  const [bankName, setBankName] = useState(profile?.bankName ?? '');
  const [accountHolder, setAccountHolder] = useState(profile?.accountHolder ?? '');
  const [accountNumber, setAccountNumber] = useState(profile?.accountNumber ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const startEdit = () => {
    setIntro(profile?.intro ?? '');
    setBankName(profile?.bankName ?? '');
    setAccountHolder(profile?.accountHolder ?? '');
    setAccountNumber(profile?.accountNumber ?? '');
    setError('');
    setEditing(true);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const request: EscortProfileWriteRequest = { intro, bankName, accountHolder, accountNumber };
    try {
      const saved = profile ? await updateMyEscortProfile(request) : await createMyEscortProfile(request);
      onSaved(saved);
      setEditing(false);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (!editing && profile) {
    return (
      <InfoCard title="추가 정보" action="수정하기" onAction={startEdit} paddingBottom="pb-6" className="lg:min-h-[300px]">
        <div className="mt-[9px] flex flex-col gap-[3px]">
          {([
            ['평점', profile.averageRating ? `${profile.averageRating.toFixed(1)}점` : '아직 없음'],
            ['완료 동행', `${profile.completedCount}회`],
            // 신원 인증 = 교육 이수. 필수 교육 영상을 모두 시청하면 백엔드가 verified 로 바꿉니다.
            [
              '신원 인증',
              profile.verified ? (
                '완료'
              ) : (
                <span className="flex flex-wrap items-center gap-2.5">
                  대기 중
                  <Link
                    href="/mypage/education"
                    className="flex h-[27px] items-center rounded-[30px] bg-brand px-4 text-xs leading-4 font-semibold text-white transition-colors hover:bg-brand-hover"
                  >
                    교육 이수하기
                  </Link>
                </span>
              ),
            ],
            ['은행', profile.bankName || '-'],
            ['예금주', profile.accountHolder || '-'],
            ['계좌번호', profile.accountNumber || '-'],
          ] as [string, ReactNode][]).map(([label, value]) => (
            <div key={label} className="flex min-h-[40px] items-center gap-2.5">
              <dt className="w-[92px] shrink-0 px-4 text-sm font-semibold text-brand">{label}</dt>
              <dd className="min-w-0 flex-1 px-4 text-sm font-medium text-brand">{value}</dd>
            </div>
          ))}
          <div className="px-4 pt-4">
            <p className="text-sm font-semibold text-brand">자기소개</p>
            <p className="mt-1.5 text-xs leading-5 font-medium text-brand">{profile.intro || '작성하지 않았습니다.'}</p>
          </div>
        </div>
      </InfoCard>
    );
  }

  return (
    <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6 shadow-card lg:min-h-[300px]">
      <h2 className="mb-4 text-2xl leading-6 font-semibold text-brand">{profile ? '추가 정보 수정' : '추가 정보 등록'}</h2>
      {!profile && <p className="mb-3 text-xs font-medium text-brand-muted">지원·정산을 받으려면 계좌 정보와 자기소개를 등록해주세요.</p>}
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label className={LABEL} htmlFor="edit-intro">자기소개</label>
          <textarea id="edit-intro" required maxLength={500} value={intro} onChange={(event) => setIntro(event.target.value)} placeholder="경력, 보유 자격증, 성격 등을 적어주세요." className={cn(FIELD, 'h-[92px] resize-none py-3')} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-bank">은행</label>
          <input id="edit-bank" required maxLength={20} value={bankName} onChange={(event) => setBankName(event.target.value)} placeholder="예) 국민은행" className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-holder">예금주</label>
          <input id="edit-holder" required maxLength={50} value={accountHolder} onChange={(event) => setAccountHolder(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-account">계좌번호</label>
          <input id="edit-account" required maxLength={30} value={accountNumber} onChange={(event) => setAccountNumber(event.target.value)} className={FIELD} />
        </div>
        {error && <p className={ERROR_TEXT}>{error}</p>}
        <div className="mt-2 flex gap-2.5">
          {profile && (
            <button type="button" onClick={() => setEditing(false)} className="h-11 flex-1 rounded-[20px] border border-line text-sm font-semibold text-brand transition-colors hover:bg-line-soft">취소</button>
          )}
          <button type="submit" disabled={saving} className="h-11 flex-1 rounded-[20px] bg-brand text-sm font-semibold text-white transition-colors hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-60">
            {saving ? '저장 중…' : profile ? '저장' : '등록하기'}
          </button>
        </div>
      </form>
    </section>
  );
}

/**
 * 동행 매니저 마이페이지 — 내 정보 (실제 API)
 *
 * - 기본 정보: GET/PATCH /api/v1/users/profile
 * - 추가 정보(자기소개·계좌): GET/POST/PUT /api/v1/users/profile/escort
 * ⚠️ "최근 활동 요약"(등록한 공고 수 등)을 주는 API 가 없어 그 섹션은 뺐습니다.
 */
export default function EscortInfoView() {
  const [state, setState] = useState<{ user?: CurrentUser; profile: EscortProfileDto | null; error?: string }>();

  useEffect(() => {
    let ignore = false;
    Promise.all([
      fetchMyProfile(),
      fetchMyEscortProfile().catch((error) => {
        if (isNotFound(error)) return null;
        throw error;
      }),
    ])
      .then(([user, profile]) => !ignore && setState({ user, profile }))
      .catch((error: Error) => !ignore && setState({ profile: null, error: error.message }));
    return () => {
      ignore = true;
    };
  }, []);

  if (!state?.user) {
    return (
      <div className="flex max-w-[858px] flex-col gap-[22px] pt-8 lg:pt-[41px]">
        <p className="text-base font-semibold text-brand">{state?.error ? `불러오지 못했습니다. (${state.error})` : '불러오는 중입니다.'}</p>
      </div>
    );
  }

  const { user, profile } = state;

  return (
    <div className="flex max-w-[858px] flex-col gap-[22px] pt-8 lg:pt-[41px]">
      <section className="flex flex-col gap-8 rounded-[30px] border border-line-soft bg-white p-8 shadow-card sm:flex-row lg:min-h-[210px]">
        <div className="grid size-[101px] shrink-0 place-items-center self-center rounded-[30px] bg-line-soft sm:self-start lg:self-center">
          <Image src="/icons/avatar.svg" alt="" width={36} height={38} />
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-3 lg:gap-[9px]">
          <div className="flex items-center gap-2.5">
            <p className="text-2xl leading-6 font-semibold text-brand">{user.name}</p>
            <span className="inline-flex h-[35px] items-center rounded-[10px] bg-brand px-[14px] text-base font-semibold whitespace-nowrap text-white">동행 매니저</span>
          </div>
          <p className="text-base leading-6 font-semibold text-brand-muted underline">{user.email}</p>
          <p className="text-base leading-6 font-semibold text-brand-muted">{user.phoneNum}</p>
          <p className="text-base leading-6 font-semibold text-brand-muted">{user.region}</p>
        </div>
      </section>

      <div className="grid grid-cols-[minmax(0,1fr)] gap-[22px] lg:grid-cols-[375px_1fr]">
        <BasicInfoCard user={user} onUpdated={(next) => setState((prev) => prev && { ...prev, user: next })} />
        <EscortProfileCard profile={profile} onSaved={(next) => setState((prev) => prev && { ...prev, profile: next })} />
      </div>
    </div>
  );
}
