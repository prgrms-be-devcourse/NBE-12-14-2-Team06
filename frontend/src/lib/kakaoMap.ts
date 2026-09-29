/**
 * 카카오맵 JavaScript SDK 로더 + 장소 검색.
 *
 * 공고 작성 폼에서 병원·출발지를 검색해 정확한 주소와 위도·경도를 받기 위해 씁니다.
 * 키는 .env.local 의 NEXT_PUBLIC_KAKAO_MAP_KEY (브라우저에 노출되는 값이라 NEXT_PUBLIC_ 접두사가 필요합니다).
 */

/** SDK 가 실제로 쓰는 부분만 최소로 적은 타입입니다. (공식 타입 패키지를 새로 설치하지 않았습니다) */
type KakaoPlaceResult = {
  place_name: string;
  /** 지번 주소 */
  address_name: string;
  /** 도로명 주소 (없을 수 있음) */
  road_address_name: string;
  /** 경도(문자열로 내려옵니다) */
  x: string;
  /** 위도(문자열로 내려옵니다) */
  y: string;
};

type KakaoPlacesSearchStatus = 'OK' | 'ZERO_RESULT' | 'ERROR';

type KakaoMapsNamespace = {
  load: (callback: () => void) => void;
  services: {
    Places: new () => {
      keywordSearch: (
        keyword: string,
        callback: (results: KakaoPlaceResult[], status: KakaoPlacesSearchStatus) => void,
      ) => void;
    };
  };
};

declare global {
  interface Window {
    kakao?: { maps: KakaoMapsNamespace };
  }
}

let loadPromise: Promise<void> | null = null;

/** SDK 스크립트를 한 번만 넣고, 준비되면 끝나는 프로미스를 돌려줍니다. */
export function loadKakaoMaps(): Promise<void> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('브라우저에서만 사용할 수 있습니다.'));
  }
  if (window.kakao?.maps?.services) {
    return Promise.resolve();
  }
  if (loadPromise) {
    return loadPromise;
  }

  const key = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY;
  if (!key) {
    return Promise.reject(new Error('카카오 지도 키(NEXT_PUBLIC_KAKAO_MAP_KEY)가 설정되지 않았습니다.'));
  }

  loadPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${key}&autoload=false&libraries=services`;
    script.async = true;
    script.onload = () => window.kakao!.maps.load(() => resolve());
    script.onerror = () => {
      loadPromise = null;
      reject(new Error('카카오 지도 스크립트를 불러오지 못했습니다.'));
    };
    document.head.appendChild(script);
  });
  return loadPromise;
}

/** 화면에서 쓰기 좋게 다듬은 검색 결과 한 줄 */
export type PlaceSearchResult = {
  /** 장소 이름 (병원명 등). 주소만 검색했으면 빈 문자열일 수 있습니다. */
  name: string;
  /** 도로명 주소가 있으면 그것, 없으면 지번 주소 */
  address: string;
  lat: number;
  lng: number;
};

/** 키워드(병원명·주소 등)로 장소를 찾습니다. 결과가 없으면 빈 배열을 돌려줍니다. */
export async function searchPlaces(keyword: string): Promise<PlaceSearchResult[]> {
  const trimmed = keyword.trim();
  if (!trimmed) return [];

  await loadKakaoMaps();

  return new Promise((resolve, reject) => {
    const places = new window.kakao!.maps.services.Places();
    places.keywordSearch(trimmed, (results, status) => {
      if (status === 'OK') {
        resolve(
          results.map((place) => ({
            name: place.place_name,
            address: place.road_address_name || place.address_name,
            lat: Number(place.y),
            lng: Number(place.x),
          })),
        );
      } else if (status === 'ZERO_RESULT') {
        resolve([]);
      } else {
        reject(new Error('주소 검색에 실패했습니다.'));
      }
    });
  });
}
