import { fetchApplicants } from '@/features/application';
import { fetchPosts, type PostFilters } from '@/features/post';
import { fetchProfile } from '@/features/user';
import { toClientPost } from './model/posts';
import type { ClientPost } from './types';

const PAGE_SIZE = 100;
const NO_FILTER: Omit<PostFilters, 'openOnly'> = { keyword: '', region: 'all', period: 'all', pay: 'all', sort: 'latest' };

/** 지원자 목록까지 확인해야 하는 상태 (매칭 완료 이후). 모집 중인 공고는 조회할 필요가 없습니다. */
const MATCHED_OR_LATER = new Set(['매칭 완료', '동행 진행 중', '동행 완료']);

/**
 * 내가 작성한 공고 목록.
 *
 * ⚠️ 서버에 "내 공고만" 조회하는 API 가 없어서, 전체 목록을 받아 화면에서 client_id 로 걸러냅니다.
 *    임시 방편이며 다음 한계가 있습니다.
 *    - 결제(Payment)가 DONE 인 공고만 GET /api/v1/posts 가 내려줍니다. 결제 전 공고는 여기 나타나지 않습니다.
 *    - openOnly=true/false 를 각각 size=100 으로만 불러오므로, 공고가 100건을 넘으면 뒤쪽이 누락될 수 있습니다.
 *    - 서버에 "내 공고 목록" API(예: GET /api/v1/posts/me)가 생기면 이 필터링은 지우고 그걸 쓰면 됩니다.
 */
export async function fetchMyPosts(): Promise<ClientPost[]> {
  const profile = await fetchProfile();

  const [open, notOpen] = await Promise.all([
    fetchPosts({ ...NO_FILTER, openOnly: true }, 0, PAGE_SIZE),
    fetchPosts({ ...NO_FILTER, openOnly: false }, 0, PAGE_SIZE),
  ]);

  const mine = [...open.posts, ...notOpen.posts]
    .filter((post) => post.clientId === profile.username)
    // PostSummary 에는 생성 시각이 없어, id 가 클수록 최근에 만든 공고라고 보고 정렬합니다.
    .sort((a, b) => b.id - a.id);

  return Promise.all(
    mine.map(async (post) => {
      if (!MATCHED_OR_LATER.has(post.postStatus)) return toClientPost(post);

      const applicants = await fetchApplicants(post.id, 0, 100).catch(() => []);
      const accepted = applicants.find((item) => item.status === 'ACCEPTED');
      return toClientPost(post, accepted ? { applicationId: accepted.applicationId, escortName: accepted.escortName } : undefined);
    }),
  );
}
