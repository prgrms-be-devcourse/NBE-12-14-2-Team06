import type { Member, MemberRole } from '../types';

/** 표의 "역할" 칸과 역할 탭에 쓰는 이름 (Figma 문구 그대로) */
export const ROLE_LABEL: Record<MemberRole, string> = {
  client: '의뢰인',
  manager: '동행 매니저',
};

export type RoleTab = 'all' | MemberRole;

export const ROLE_TABS: { value: RoleTab; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'client', label: '의뢰인' },
  { value: 'manager', label: '동행매니저' },
];

/*
 * ⚠️ 모의 데이터입니다. 앞의 6명은 Figma 화면(571:19168)의 문구이고,
 *    뒤의 8명은 페이지 이동을 확인하려고 만든 예시입니다.
 *    TODO: 회원 목록 API(GET /api/v1/admin/users) 가 연결되면 이 파일은 필요 없어집니다.
 */
export const MEMBERS: Member[] = [
  { id: 'user001', name: '김회원', role: 'client', email: 'user1@test.com', phone: '010-0000-0000', joinedAt: '2025-11-12' },
  { id: 'user002', name: '장회원', role: 'client', email: 'user2@test.com', phone: '010-1111-1111', joinedAt: '2025-11-12' },
  { id: 'user003', name: '최회원', role: 'client', email: 'user3@test.com', phone: '010-2222-2222', joinedAt: '2025-11-12' },
  { id: 'user004', name: '박회원', role: 'manager', email: 'user4@test.com', phone: '010-3333-3333', joinedAt: '2025-11-12' },
  { id: 'user005', name: '나회원', role: 'client', email: 'user5@test.com', phone: '010-4444-4444', joinedAt: '2025-11-12' },
  { id: 'user006', name: '양회원', role: 'manager', email: 'user6@test.com', phone: '010-5555-5555', joinedAt: '2025-11-12' },
  { id: 'user007', name: '이회원', role: 'client', email: 'user7@test.com', phone: '010-6666-6666', joinedAt: '2025-10-30' },
  { id: 'user008', name: '정회원', role: 'manager', email: 'user8@test.com', phone: '010-7777-7777', joinedAt: '2025-10-22' },
  { id: 'user009', name: '강회원', role: 'client', email: 'user9@test.com', phone: '010-8888-8888', joinedAt: '2025-10-08' },
  { id: 'user010', name: '조회원', role: 'manager', email: 'user10@test.com', phone: '010-9999-9999', joinedAt: '2025-09-27' },
  { id: 'user011', name: '윤회원', role: 'client', email: 'user11@test.com', phone: '010-1234-5678', joinedAt: '2025-09-15' },
  { id: 'user012', name: '임회원', role: 'manager', email: 'user12@test.com', phone: '010-2345-6789', joinedAt: '2025-09-01' },
  { id: 'user013', name: '한회원', role: 'client', email: 'user13@test.com', phone: '010-3456-7890', joinedAt: '2025-08-19' },
  { id: 'user014', name: '오회원', role: 'manager', email: 'user14@test.com', phone: '010-4567-8901', joinedAt: '2025-08-04' },
];

export type MemberSort = 'latest' | 'oldest';

export const SORT_OPTIONS: { value: MemberSort; label: string }[] = [
  { value: 'latest', label: '최신 순' },
  { value: 'oldest', label: '오래된 순' },
];

/** 역할 탭 · 검색어 · 정렬을 적용합니다. (목록 API 의 role · keyword · sort 조건과 같은 역할) */
export function filterMembers(
  members: Member[],
  { tab, keyword, sort }: { tab: RoleTab; keyword: string; sort: MemberSort },
): Member[] {
  const trimmed = keyword.trim();

  const filtered = members.filter((member) => {
    if (tab !== 'all' && member.role !== tab) return false;
    if (!trimmed) return true;
    return [member.id, member.name, member.email, member.phone, ROLE_LABEL[member.role]].some((text) =>
      text.includes(trimmed),
    );
  });

  // 가입일이 같으면 목록에 적힌 순서를 그대로 둡니다 (Array.sort 는 안정 정렬).
  return [...filtered].sort((a, b) =>
    sort === 'latest' ? b.joinedAt.localeCompare(a.joinedAt) : a.joinedAt.localeCompare(b.joinedAt),
  );
}
