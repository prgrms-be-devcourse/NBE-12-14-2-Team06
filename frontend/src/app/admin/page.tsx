import { redirect } from 'next/navigation';

/** /admin 으로 들어오면 첫 화면인 회원 관리로 보냅니다. */
export default function Page() {
  redirect('/admin/members');
}
