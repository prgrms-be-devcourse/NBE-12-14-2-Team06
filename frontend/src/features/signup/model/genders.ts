import type { SignupGender } from '../types';

export const GENDER_OPTIONS: { value: SignupGender; label: string }[] = [
  { value: 'MALE', label: '남성' },
  { value: 'FEMALE', label: '여성' },
  { value: 'NONE', label: '선택 안 함' },
];
