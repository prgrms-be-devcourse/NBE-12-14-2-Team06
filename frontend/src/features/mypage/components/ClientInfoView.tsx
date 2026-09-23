'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState, type FormEvent } from 'react';
import { InfoRow } from '@/components/ui';
import type { CurrentUser } from '@/features/auth';
import { fetchMyProfile } from '@/features/auth';
import { fetchMyPosts, type ClientPost } from '@/features/client';
import { ApiError } from '@/lib/api';
import { cn } from '@/lib/cn';
import { REGIONS } from '@/lib/regions';
import {
  createMyClientProfile,
  fetchMyClientProfile,
  updateMyClientProfile,
  updateMyProfile,
  type ClientProfileWriteRequest,
} from '../api';
import type { ActivityStat, ClientProfileDto } from '../types';
import InfoCard from './InfoCard';

const FIELD = 'h-[47px] w-full rounded-[10px] border border-line-soft bg-white px-4 text-sm text-brand shadow-card placeholder:text-brand-muted';
const LABEL = 'mb-1.5 block text-sm font-semibold text-brand';
const ERROR_TEXT = 'text-sm font-medium text-[#b91d1d]';
const GENDER_LABEL: Record<CurrentUser['gender'], string> = { MALE: '남', FEMALE: '여' };
const ACCOUNT_ACTIONS = ['비밀번호 변경', '이메일 변경', '회원 탈퇴'];

/** "2026-09-22T10:00:00" · "2026-09-22" 를 <input type="date"> 가 먹는 "2026-09-22" 로 */
function toDateInput(value: string): string {
  return value.slice(0, 10);
}

/** 404(아직 프로필을 만든 적 없음)인지 확인 */
function isNotFound(error: unknown): boolean {
  // 500 등 RsData 형식이 아닌 응답(스프링 기본 에러 페이지)이면 statusCode 가 없을 수 있어 옵셔널 체이닝으로 방어합니다.
  return error instanceof ApiError && (error.statusCode?.startsWith('404') ?? false);
}

/** fetchMyPosts() 결과를 postStatus 로 세어 "최근 활동 요약" 세 칸을 채웁니다. */
function toActivityStats(posts: ClientPost[]): ActivityStat[] {
  return [
    { label: '등록한 공고', count: posts.length, icon: '/icons/activity-post.svg' },
    { label: '매칭된 공고', count: posts.filter((post) => post.status === 'matched').length, icon: '/icons/activity-star.svg' },
    { label: '진행 중', count: posts.filter((post) => post.status === 'inProgress').length, icon: '/icons/activity-bell.svg' },
  ];
}

/** "기본 정보" 카드 — 조회 + 수정 (PATCH /api/v1/users/profile). EscortInfoView 의 BasicInfoCard 와 동일합니다. */
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
      <InfoCard title="기본 정보" action="수정하기" onAction={startEdit} className="lg:min-h-[527px]">
        <dl className="mt-[30px] flex flex-col gap-[3px]">
          <InfoRow label="아이디" labelWidth={92}>{user.username}</InfoRow>
          <InfoRow label="이메일" labelWidth={92}>{user.email}</InfoRow>
          <InfoRow label="실명" labelWidth={92}>{user.name}</InfoRow>
          <InfoRow label="생년월일" labelWidth={92}>{user.birthDate}</InfoRow>
          <InfoRow label="성별" labelWidth={92}>{GENDER_LABEL[user.gender]}</InfoRow>
          <InfoRow label="전화번호" labelWidth={92}>{user.phoneNum}</InfoRow>
          <InfoRow label="거주 지역" labelWidth={92}>{user.region}</InfoRow>
        </dl>
      </InfoCard>
    );
  }

  return (
    <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6 shadow-card lg:min-h-[527px]">
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

/** "추가 정보" 카드 — 보호자 정보 (GET/POST/PUT /api/v1/users/profile/client) */
function GuardianInfoCard({ profile, onSaved }: { profile: ClientProfileDto | null; onSaved: (profile: ClientProfileDto) => void }) {
  const [editing, setEditing] = useState(profile === null);
  const [emergencyContactName, setEmergencyContactName] = useState(profile?.emergencyContactName ?? '');
  const [emergencyContactPhone, setEmergencyContactPhone] = useState(profile?.emergencyContactPhone ?? '');
  const [careNote, setCareNote] = useState(profile?.careNote ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const startEdit = () => {
    setEmergencyContactName(profile?.emergencyContactName ?? '');
    setEmergencyContactPhone(profile?.emergencyContactPhone ?? '');
    setCareNote(profile?.careNote ?? '');
    setError('');
    setEditing(true);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const request: ClientProfileWriteRequest = { emergencyContactName, emergencyContactPhone, careNote };
    try {
      const saved = profile ? await updateMyClientProfile(request) : await createMyClientProfile(request);
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
      <InfoCard title="추가 정보" action="수정하기" onAction={startEdit} paddingBottom="pb-6" className="lg:min-h-[256px]">
        <dl className="mt-[9px] flex flex-col gap-[3px]">
          <InfoRow label="보호자 실명" labelWidth={138}>{profile.emergencyContactName}</InfoRow>
          <InfoRow label="보호자 전화번호" labelWidth={138}>{profile.emergencyContactPhone}</InfoRow>
          <InfoRow label="특이사항" labelWidth={138}>{profile.careNote || '작성하지 않았습니다.'}</InfoRow>
        </dl>
      </InfoCard>
    );
  }

  return (
    <section className="rounded-[30px] border border-line bg-white px-6 pt-8 pb-6 shadow-card lg:min-h-[256px]">
      <h2 className="mb-4 text-2xl leading-6 font-semibold text-brand">{profile ? '추가 정보 수정' : '추가 정보 등록'}</h2>
      {!profile && <p className="mb-3 text-xs font-medium text-brand-muted">아직 등록하지 않았습니다. 보호자 정보를 등록해주세요.</p>}
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label className={LABEL} htmlFor="edit-guardian-name">보호자 실명</label>
          <input id="edit-guardian-name" required maxLength={50} value={emergencyContactName} onChange={(event) => setEmergencyContactName(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-guardian-phone">보호자 전화번호</label>
          <input id="edit-guardian-phone" required maxLength={20} value={emergencyContactPhone} onChange={(event) => setEmergencyContactPhone(event.target.value)} className={FIELD} />
        </div>
        <div>
          <label className={LABEL} htmlFor="edit-care-note">특이사항</label>
          <textarea id="edit-care-note" maxLength={500} value={careNote} onChange={(event) => setCareNote(event.target.value)} placeholder="거동, 지병 등 동행 시 참고할 내용을 적어주세요." className={cn(FIELD, 'h-[92px] resize-none py-3')} />
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
 * 의뢰인 마이페이지 — 내 정보 (실제 API)
 *
 * - 기본 정보: GET/PATCH /api/v1/users/profile
 * - 추가 정보(보호자 연락처·특이사항): GET/POST/PUT /api/v1/users/profile/client
 * - 최근 활동 요약: "등록한 공고"·"매칭된 공고"·"진행 중"은 fetchMyPosts() 결과를 postStatus 로 세어 채웁니다.
 *   ⚠️ "작성한 리뷰" 칸은 뺐습니다 — 내가 쓴 리뷰 목록을 내려주는 API 가 없습니다.
 *      (GET /api/v1/users/{userId}/reviews 는 동행 매니저가 "받은" 리뷰만 돌려줍니다.)
 */
export default function ClientInfoView() {
  const [state, setState] = useState<{
    user?: CurrentUser;
    clientProfile: ClientProfileDto | null;
    posts: ClientPost[];
    error?: string;
  }>({ clientProfile: null, posts: [] });

  useEffect(() => {
    let ignore = false;
    Promise.all([
      fetchMyProfile(),
      fetchMyClientProfile().catch((error) => {
        if (isNotFound(error)) return null;
        throw error;
      }),
      fetchMyPosts(),
    ])
      .then(([user, clientProfile, posts]) => !ignore && setState({ user, clientProfile, posts }))
      .catch((error: Error) => !ignore && setState({ clientProfile: null, posts: [], error: error.message }));
    return () => {
      ignore = true;
    };
  }, []);

  if (!state.user) {
    return (
      <div className="flex max-w-[858px] flex-col gap-[22px] pt-8 lg:pt-[41px]">
        <p className="text-base font-semibold text-brand">{state.error ? `불러오지 못했습니다. (${state.error})` : '불러오는 중입니다.'}</p>
      </div>
    );
  }

  const { user, clientProfile, posts } = state;
  const stats = toActivityStats(posts);

  return (
    <div className="flex max-w-[858px] flex-col gap-[22px] pt-8 lg:pt-[41px]">
      <section className="flex flex-col gap-8 rounded-[30px] border border-line-soft bg-white p-8 shadow-card sm:flex-row lg:min-h-[210px]">
        <div className="grid size-[101px] shrink-0 place-items-center self-center rounded-[30px] bg-line-soft sm:self-start lg:self-center">
          <Image src="/icons/avatar.svg" alt="" width={36} height={38} />
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-3 lg:gap-[9px]">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2.5">
              <p className="text-2xl leading-6 font-semibold text-brand">{user.name}</p>
              <span className="inline-flex h-[35px] items-center rounded-[10px] bg-brand px-[14px] text-base font-semibold whitespace-nowrap text-white">
                의뢰인
              </span>
            </div>
          </div>
          <p className="text-base leading-6 font-semibold text-brand-muted underline">{user.email}</p>
          <p className="text-base leading-6 font-semibold text-brand-muted">{user.phoneNum}</p>
          <p className="text-base leading-6 font-semibold text-brand-muted">{user.region}</p>
        </div>
      </section>

      <div className="grid grid-cols-[minmax(0,1fr)] gap-[22px] lg:grid-cols-[375px_1fr]">
        <BasicInfoCard user={user} onUpdated={(next) => setState((prev) => ({ ...prev, user: next }))} />
        <div className="flex flex-col gap-[22px]">
          <GuardianInfoCard profile={clientProfile} onSaved={(next) => setState((prev) => ({ ...prev, clientProfile: next }))} />
          <InfoCard title="계정 관리" paddingBottom="pb-6" className="lg:min-h-[250px]">
            <ul className="mt-[9px] flex flex-col gap-[3px]">
              {ACCOUNT_ACTIONS.map((label) => (
                // TODO: 각 기능이 정해지면 연결하세요.
                <li key={label}>
                  <button
                    type="button"
                    className="flex h-[47px] w-full items-center justify-between px-4 text-left text-base leading-5 font-semibold text-brand transition-colors hover:text-brand-hover"
                  >
                    {label}
                    <Image src="/icons/chevron-right.svg" alt="" width={24} height={24} />
                  </button>
                </li>
              ))}
            </ul>
          </InfoCard>
        </div>
      </div>

      <section className="rounded-[30px] border border-line bg-white px-6 py-8 shadow-card lg:min-h-[207px] lg:px-[37px]">
        <div className="flex h-[47px] items-center justify-between">
          <h2 className="text-2xl leading-6 font-semibold text-brand">최근 활동 요약</h2>
          <Link
            href="/client/posts"
            className="flex items-center gap-4 text-base leading-5 font-semibold text-brand transition-colors hover:text-brand-hover"
          >
            전체 보기
            <Image src="/icons/arrow-right.svg" alt="" width={24} height={24} />
          </Link>
        </div>
        <ul className="mt-2 grid grid-cols-2 gap-4 lg:grid-cols-3 lg:gap-[22px]">
          {stats.map((stat) => (
            <li
              key={stat.label}
              className="flex h-[83px] items-center gap-3 rounded-[20px] border border-line-soft bg-white px-4 shadow-card"
            >
              <Image src={stat.icon} alt="" width={50} height={50} className="size-[50px] shrink-0" />
              <div className="min-w-0 flex-1">
                <p className="text-xs leading-3 font-semibold text-brand">{stat.label}</p>
                <p className="mt-1.5 flex items-center justify-between text-xl leading-6 font-bold text-brand">
                  {stat.count}
                  <Image src="/icons/activity-chevron.svg" alt="" width={12} height={12} />
                </p>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
