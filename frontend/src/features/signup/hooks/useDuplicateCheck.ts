'use client';

import { useState, type RefObject } from 'react';

type Options = {
  /** 검사할 입력창. 서버에 묻기 전 형식 검사(required · pattern · type)에 씁니다. */
  inputRef: RefObject<HTMLInputElement | null>;
  /** 지금 입력값 */
  value: string;
  /** 중복 확인을 통과한 값 (없으면 빈 문자열) */
  verifiedValue: string;
  onVerified: (value: string) => void;
  /** 쓸 수 있으면 true 를 돌려주는 API 호출 */
  check: (value: string) => Promise<boolean>;
  /** 안내 문구에 쓸 이름 (예: "아이디") */
  label: string;
};

export type DuplicateCheckMessage = { ok: boolean; text: string };

/**
 * "중복 확인" 버튼 동작.
 * verified 는 지금 입력값이 확인을 통과했는지입니다. 통과한 뒤 값을 고치면 다시 false 가 됩니다.
 * (다음 단계로 못 넘어가게 막는 일은 화면에서 verified 를 보고 합니다)
 */
export function useDuplicateCheck({ inputRef, value, verifiedValue, onVerified, check, label }: Options) {
  const [checking, setChecking] = useState(false);
  /** 실패 문구. 그 값을 검사했을 때만 보여 주도록 검사한 값과 함께 둡니다. */
  const [failure, setFailure] = useState<{ value: string; text: string } | null>(null);

  const verified = value !== '' && value === verifiedValue;

  const handleCheck = async () => {
    const input = inputRef.current;
    if (!input || checking) return;

    // 형식부터 봅니다. 틀리면 브라우저 기본 말풍선으로 알려 주고 서버에는 묻지 않습니다.
    if (!input.reportValidity()) return;

    const target = value;
    setChecking(true);
    try {
      if (await check(target)) {
        onVerified(target);
        setFailure(null);
      } else {
        setFailure({ value: target, text: `이미 사용 중인 ${label}입니다.` });
      }
    } catch (error) {
      setFailure({
        value: target,
        text: error instanceof Error ? error.message : `${label} 중복 확인에 실패했습니다.`,
      });
    } finally {
      setChecking(false);
    }
  };

  let message: DuplicateCheckMessage | null = null;
  if (verified) message = { ok: true, text: `사용 가능한 ${label}입니다.` };
  else if (failure && failure.value === value) message = { ok: false, text: failure.text };

  return { verified, checking, message, handleCheck };
}
