/** 조건부 className 합치기 (의존성 없는 최소 구현) */
export function cn(...classes: Array<string | false | null | undefined>) {
  return classes.filter(Boolean).join(' ');
}
