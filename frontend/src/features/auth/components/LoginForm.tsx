'use client';

import { useState, type ChangeEvent, type FormEvent } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { cn } from '@/lib/cn';
import type { LoginFormValues } from '../types';
import SocialLoginRow from './SocialLoginRow';

/** 입력창 (Figma 564:17974 — 라운드 30, 상하 여백 22, 은은한 그림자) */
const FIELD_CLASS =
  'w-full rounded-[30px] border border-line-soft bg-white px-5 py-[22px] text-base leading-5 text-brand shadow-card placeholder:text-line';

const INITIAL_VALUES: LoginFormValues = { username: '', password: '', rememberMe: false };

/** 로그인 폼 카드 안쪽 (Figma 공통_로그인 564:17969) */
export default function LoginForm() {
  const [values, setValues] = useState<LoginFormValues>(INITIAL_VALUES);

  const handleChange =
    (key: 'username' | 'password') => (event: ChangeEvent<HTMLInputElement>) => {
      setValues((prev) => ({ ...prev, [key]: event.target.value }));
    };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    // TODO: 로그인 API(POST /api/v1/auth/login) 연결
  };

  return (
    <form onSubmit={handleSubmit} className="flex w-full max-w-[424px] flex-col gap-[30px]">
      <h1 className="text-[30px] leading-9 font-extrabold text-brand">로그인</h1>

      <hr className="border-line-soft" />

      <div className="flex flex-col gap-2.5">
        <label htmlFor="login-username" className="sr-only">
          아이디 또는 이메일
        </label>
        <input
          id="login-username"
          name="username"
          autoComplete="username"
          required
          placeholder="아이디 또는 이메일을 입력하세요"
          value={values.username}
          onChange={handleChange('username')}
          className={FIELD_CLASS}
        />

        <label htmlFor="login-password" className="sr-only">
          비밀번호
        </label>
        <input
          id="login-password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          placeholder="비밀번호를 입력하세요"
          value={values.password}
          onChange={handleChange('password')}
          className={FIELD_CLASS}
        />
      </div>

      <div className="flex flex-wrap items-center gap-x-[5px] gap-y-3">
        <label className="flex cursor-pointer items-center gap-[5px]">
          <input
            type="checkbox"
            name="rememberMe"
            checked={values.rememberMe}
            onChange={(event) =>
              setValues((prev) => ({ ...prev, rememberMe: event.target.checked }))
            }
            className="peer sr-only"
          />
          <span
            aria-hidden="true"
            className={cn(
              'grid size-5 shrink-0 place-items-center rounded-[3px] border-2 border-[#545f71]',
              'peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-brand',
              values.rememberMe ? 'bg-[#545f71]' : 'bg-white',
            )}
          >
            {values.rememberMe && (
              <span className="mb-0.5 h-2.5 w-[5px] rotate-45 border-r-2 border-b-2 border-white" />
            )}
          </span>
          <span className="text-sm leading-5 font-semibold whitespace-nowrap text-brand">
            로그인 상태 유지
          </span>
        </label>

        {/* TODO: 아이디/비밀번호 찾기 화면이 생기면 주소를 연결하세요. */}
        <span className="ml-auto flex items-center gap-1.5 text-sm leading-[22px] font-medium whitespace-nowrap text-brand">
          <a href="#" className="transition-colors hover:text-brand-hover">
            아이디 찾기
          </a>
          <span aria-hidden="true">|</span>
          <a href="#" className="transition-colors hover:text-brand-hover">
            비밀번호 찾기
          </a>
        </span>
      </div>

      <button
        type="submit"
        className="flex h-12 w-full items-center justify-center rounded-[30px] bg-brand px-[18px] text-base leading-5 font-semibold text-white drop-shadow-soft transition-colors hover:bg-brand-hover"
      >
        로그인하기
      </button>

      {/* 구분선 · 간편 로그인 · 회원가입 안내 (Figma 564:17986) */}
      <div className="flex flex-col">
        <div className="flex items-center gap-6">
          <span aria-hidden="true" className="h-px flex-1 bg-[#edf2f7]" />
          <span className="text-sm leading-[18px] font-medium tracking-[-0.08px] whitespace-nowrap text-brand">
            또는 간편하게 시작하기
          </span>
          <span aria-hidden="true" className="h-px flex-1 bg-[#edf2f7]" />
        </div>

        {/* 구분선 아래 42px · 아이콘 아래 24px (Figma 564:17987 · 564:17998 의 세로 오프셋) */}
        <div className="mt-6">
          <SocialLoginRow />
        </div>

        <div className="mt-6 flex items-center justify-center">
          <span className="text-sm leading-5 font-medium text-brand-muted">
            아직 계정이 없으신가요?
          </span>
          <Link
            href="/signup"
            className="ml-[5px] flex items-center text-sm leading-[22px] font-medium text-brand transition-colors hover:text-brand-hover"
          >
            회원가입
            <span aria-hidden="true" className="grid size-3 shrink-0 place-items-center">
              <Image src="/icons/arrow-right-soft.svg" alt="" width={9.68566} height={9.52414} />
            </span>
          </Link>
        </div>
      </div>
    </form>
  );
}
