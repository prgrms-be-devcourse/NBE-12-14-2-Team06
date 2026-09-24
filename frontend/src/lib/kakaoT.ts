/**
 * 카카오 T 호출 바로가기 링크.
 *
 * https://t.kakao.com/launch 에 출발지·도착지 좌표를 실어 보내면
 * 모바일에서는 카카오 T 앱의 택시 호출 화면이 그 경로로 채워진 채 열립니다.
 */

/** 길찾기 한 지점 (공고의 픽업 주소 · 병원 좌표에서 그대로 옵니다) */
export type RoutePoint = {
  /** 화면 안내 문구용 이름 ("집" · 병원 이름) */
  name: string;
  lat: number;
  lng: number;
};

/** from → to 경로가 미리 채워진 카카오 T 택시 호출 링크 */
export function buildKakaoTCallUrl(from: RoutePoint, to: RoutePoint): string {
  const params = new URLSearchParams({
    type: 'taxi',
    origin_lat: String(from.lat),
    origin_lng: String(from.lng),
    dest_lat: String(to.lat),
    dest_lng: String(to.lng),
  });
  return `https://t.kakao.com/launch?${params.toString()}`;
}
