'use client';

import Image from 'next/image';
import { useState } from 'react';
import { cn } from '@/lib/cn';
import { searchPlaces, type PlaceSearchResult } from '@/lib/kakaoMap';
import { FIELD } from './fields';

type Props = {
  id: string;
  placeholder: string;
  /** 수정 화면에서 이미 정해진 주소를 보여줄 때 씁니다. */
  defaultValue?: string;
  /** 목록에서 하나를 고르면 호출됩니다. (이름·주소·위도·경도) */
  onSelect: (result: PlaceSearchResult) => void;
  /** 선택 후 입력칸에 보여줄 문구. 기본은 주소이고, 병원명 검색처럼 이름을 보여주고 싶으면 지정하세요. */
  formatSelected?: (result: PlaceSearchResult) => string;
};

/**
 * 카카오맵으로 병원·출발지를 검색하는 입력칸 (Figma "돋보기 아이콘 입력칸"을 대신합니다).
 * 자유 입력만으로는 위도·경도를 알 수 없어서, 목록에서 하나를 선택해야만 값이 채워집니다.
 */
export default function AddressSearchField({ id, placeholder, defaultValue = '', onSelect, formatSelected }: Props) {
  const [query, setQuery] = useState(defaultValue);
  const [results, setResults] = useState<PlaceSearchResult[]>([]);
  const [open, setOpen] = useState(false);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState('');

  const runSearch = async () => {
    if (!query.trim() || searching) return;
    setSearching(true);
    setError('');
    try {
      const found = await searchPlaces(query);
      setResults(found);
      setOpen(true);
      if (found.length === 0) setError('검색 결과가 없습니다. 다른 말로 검색해보세요.');
    } catch (err) {
      setError(err instanceof Error ? err.message : '검색에 실패했습니다.');
    } finally {
      setSearching(false);
    }
  };

  const handleSelect = (result: PlaceSearchResult) => {
    setQuery(formatSelected ? formatSelected(result) : result.address);
    setOpen(false);
    setResults([]);
    setError('');
    onSelect(result);
  };

  return (
    <div className="relative">
      <div className="relative">
        <input
          id={id}
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.preventDefault();
              runSearch();
            }
          }}
          placeholder={placeholder}
          className={cn(FIELD, 'pr-12')}
        />
        <button
          type="button"
          onClick={runSearch}
          disabled={searching}
          aria-label="검색"
          className="absolute top-1/2 right-4 -translate-y-1/2 disabled:opacity-50"
        >
          <Image src="/icons/search.svg" alt="" width={14} height={14} />
        </button>
      </div>
      {error && (
        <p role="alert" className="mt-1.5 px-4 text-sm font-medium text-[#b91d1d]">
          {error}
        </p>
      )}
      {open && results.length > 0 && (
        <ul className="absolute z-10 mt-1.5 max-h-64 w-full overflow-y-auto rounded-[20px] border border-line bg-white p-2 shadow-card">
          {results.map((result) => (
            <li key={`${result.name}-${result.address}`}>
              <button
                type="button"
                onClick={() => handleSelect(result)}
                className={cn('flex w-full flex-col gap-0.5 rounded-[12px] px-3 py-2 text-left', 'hover:bg-line-soft')}
              >
                <span className="text-sm font-semibold text-brand">{result.name || result.address}</span>
                <span className="text-xs text-brand-muted">{result.address}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
