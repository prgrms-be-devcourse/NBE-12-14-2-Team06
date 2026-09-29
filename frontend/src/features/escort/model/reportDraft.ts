import { DEPARTMENTS } from './departments';

/**
 * 작성 중인 동행 보고서의 임시 저장 (localStorage).
 *
 * 제출 전에 화면을 벗어나도 이어서 쓸 수 있게, 동행 건(applicationId)별로 한 벌씩 보관합니다.
 * 서버에 저장하는 게 아니라 이 브라우저에만 남고, 제출에 성공하면 지웁니다.
 */

/** 저장해 두는 입력값. 키 이름은 작성 폼의 name 과 맞춰 둡니다. */
export type ReportDraftFields = {
  department: string;
  purpose: string;
  summary: string;
  notes: string;
};

export type ReportDraft = ReportDraftFields & {
  /** 마지막 저장 시각 (Date.now()) */
  savedAt: number;
};

const storageKey = (applicationId: number) => `escort:report-draft:${applicationId}`;

/** 사생활 보호 모드 등에서는 localStorage 접근 자체가 예외를 던져서, 없는 셈 치고 넘어갑니다. */
function storage(): Storage | undefined {
  try {
    return typeof window === 'undefined' ? undefined : window.localStorage;
  } catch {
    return undefined;
  }
}

function text(value: unknown): string {
  return typeof value === 'string' ? value : '';
}

/** 입력값이 전부 비어 있으면 저장할 내용이 없다고 봅니다. */
export function isReportDraftEmpty(fields: ReportDraftFields): boolean {
  return !Object.values(fields).some((value) => value.trim());
}

/**
 * 저장된 임시 글. 없거나 읽지 못하면 undefined 입니다.
 * 진료 과목은 선택지에 없는 값이면 버립니다 — select 에 넣어도 고를 수 없는 값이라서요.
 */
export function loadReportDraft(applicationId: number): ReportDraft | undefined {
  if (!Number.isFinite(applicationId)) return undefined;

  const raw = storage()?.getItem(storageKey(applicationId));
  if (!raw) return undefined;

  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch {
    return undefined;
  }
  if (typeof parsed !== 'object' || parsed === null) return undefined;

  const saved = parsed as Partial<ReportDraft>;
  const department = text(saved.department);
  const draft: ReportDraft = {
    department: DEPARTMENTS.some((option) => option.value === department) ? department : '',
    purpose: text(saved.purpose),
    summary: text(saved.summary),
    notes: text(saved.notes),
    savedAt: typeof saved.savedAt === 'number' ? saved.savedAt : Date.now(),
  };
  return isReportDraftEmpty(draft) ? undefined : draft;
}

/** 저장한 시각을 돌려줍니다. 저장할 내용이 없거나 저장에 실패하면 undefined. */
export function saveReportDraft(applicationId: number, fields: ReportDraftFields): number | undefined {
  if (!Number.isFinite(applicationId)) return undefined;
  if (isReportDraftEmpty(fields)) {
    clearReportDraft(applicationId);
    return undefined;
  }

  const savedAt = Date.now();
  try {
    // 용량이 꽉 찼을 때(QuotaExceededError) 작성 자체를 막지는 않습니다.
    storage()?.setItem(storageKey(applicationId), JSON.stringify({ ...fields, savedAt }));
  } catch {
    return undefined;
  }
  return savedAt;
}

export function clearReportDraft(applicationId: number): void {
  if (!Number.isFinite(applicationId)) return;
  try {
    storage()?.removeItem(storageKey(applicationId));
  } catch {
    // 지우지 못해도 화면에서 할 수 있는 일이 없습니다.
  }
}
