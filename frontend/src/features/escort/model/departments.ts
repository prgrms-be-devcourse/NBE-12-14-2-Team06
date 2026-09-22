/** 진료 과목 선택지. value 는 백엔드 Department ENUM 이름과 정확히 일치해야 합니다. */
export type DepartmentOption = { value: string; label: string };

export const DEPARTMENTS: DepartmentOption[] = [
  { value: 'INTERNAL_MEDICINE', label: '내과' },
  { value: 'SURGERY', label: '외과' },
  { value: 'ORTHOPEDICS', label: '정형외과' },
  { value: 'RADIOLOGY', label: '영상의학과' },
  { value: 'DENTISTRY', label: '치과' },
  { value: 'OPHTHALMOLOGY', label: '안과' },
  { value: 'DERMATOLOGY', label: '피부과' },
  { value: 'ETC', label: '기타' },
];
