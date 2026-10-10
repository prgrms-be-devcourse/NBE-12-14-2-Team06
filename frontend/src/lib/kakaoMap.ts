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

/** kakao.maps.LatLng 인스턴스. 안쪽 구조는 쓰지 않아 불투명하게 둡니다. */
type KakaoLatLng = object;

type KakaoMapInstance = {
  setCenter: (latlng: KakaoLatLng) => void;
  panTo: (latlng: KakaoLatLng) => void;
  relayout: () => void;
};

type KakaoCustomOverlayInstance = {
  setPosition: (latlng: KakaoLatLng) => void;
  setMap: (map: KakaoMapInstance | null) => void;
};

type KakaoPolylineInstance = {
  setPath: (path: KakaoLatLng[]) => void;
  setMap: (map: KakaoMapInstance | null) => void;
};

type KakaoMapsNamespace = {
  load: (callback: () => void) => void;
  LatLng: new (lat: number, lng: number) => KakaoLatLng;
  Map: new (container: HTMLElement, options: { center: KakaoLatLng; level: number }) => KakaoMapInstance;
  CustomOverlay: new (options: {
    position: KakaoLatLng;
    content: HTMLElement;
    xAnchor?: number;
    yAnchor?: number;
    map?: KakaoMapInstance;
  }) => KakaoCustomOverlayInstance;
  Polyline: new (options: {
    map?: KakaoMapInstance;
    path: KakaoLatLng[];
    strokeWeight: number;
    strokeColor: string;
    strokeOpacity: number;
    strokeStyle: string;
  }) => KakaoPolylineInstance;
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

/** 위도·경도 한 점 */
export type LatLngLiteral = { lat: number; lng: number };

/** 실시간 위치 지도를 조작하는 손잡이. SDK 타입을 밖으로 내보내지 않으려고 이 모양으로 감쌌습니다. */
export type LiveMapController = {
  /** 현재 위치 마커를 옮기고 지도 중심을 따라갑니다. trail 이 있으면 지나온 경로 선도 같이 그립니다. */
  update: (position: LatLngLiteral, trail?: LatLngLiteral[]) => void;
  /** 지도 영역의 크기가 바뀌었을 때(처음 보일 때 등) 다시 맞춥니다. */
  relayout: () => void;
};

/** 위치를 아직 모를 때 지도를 먼저 띄워 둘 중심 (서울시청) */
const DEFAULT_CENTER: LatLngLiteral = { lat: 37.5665, lng: 126.978 };
/** 카카오 지도 확대 수준 (숫자가 작을수록 더 확대됩니다) */
const DEFAULT_LEVEL = 3;

/** 현재 위치를 나타내는 동그란 표시. 외부 이미지 없이 스타일만으로 그립니다. */
function createPositionDot(): HTMLElement {
  const dot = document.createElement('div');
  dot.setAttribute('aria-label', '동행 매니저의 현재 위치');
  dot.style.width = '22px';
  dot.style.height = '22px';
  dot.style.borderRadius = '50%';
  dot.style.background = '#353e5c';
  dot.style.border = '3px solid #ffffff';
  dot.style.boxShadow = '0 0 0 4px rgba(53, 62, 92, 0.25)';
  return dot;
}

/**
 * container 안에 지도를 만들고, 현재 위치 표시와 이동 경로를 갱신할 수 있는 손잡이를 돌려줍니다.
 * SDK 를 불러오지 못하면(키 없음·도메인 미등록 등) 프로미스가 실패합니다.
 */
export async function createLiveMap(container: HTMLElement): Promise<LiveMapController> {
  await loadKakaoMaps();

  const { maps } = window.kakao!;
  const toLatLng = (point: LatLngLiteral) => new maps.LatLng(point.lat, point.lng);

  const map = new maps.Map(container, { center: toLatLng(DEFAULT_CENTER), level: DEFAULT_LEVEL });
  let overlay: InstanceType<typeof maps.CustomOverlay> | undefined;
  let line: InstanceType<typeof maps.Polyline> | undefined;

  return {
    update(position, trail) {
      const latlng = toLatLng(position);

      if (!overlay) {
        // 첫 위치는 부드럽게 움직이지 않고 바로 그 자리로 보냅니다.
        overlay = new maps.CustomOverlay({
          position: latlng,
          content: createPositionDot(),
          xAnchor: 0.5,
          yAnchor: 0.5,
          map,
        });
        map.setCenter(latlng);
      } else {
        overlay.setPosition(latlng);
        map.panTo(latlng);
      }

      if (trail && trail.length > 1) {
        const path = trail.map(toLatLng);
        if (!line) {
          line = new maps.Polyline({
            map,
            path,
            strokeWeight: 4,
            strokeColor: '#353e5c',
            strokeOpacity: 0.9,
            strokeStyle: 'solid',
          });
        } else {
          line.setPath(path);
        }
      }
    },
    relayout() {
      map.relayout();
    },
  };
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
