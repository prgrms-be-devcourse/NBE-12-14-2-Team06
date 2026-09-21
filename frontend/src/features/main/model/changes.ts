import { ShieldCheckIcon, ManagerIcon, TapIcon, HeartIcon } from '@/components/ui/icons';
import type { ChangeItem } from '../types';

export const CHANGES: ChangeItem[] = [
  { icon: ShieldCheckIcon, title: '안전한 동행', description: ['신원 확인된 매니저와', '안전하게 동행해요'] },
  { icon: ManagerIcon, title: '신뢰할 수 있는 매니저', description: ['검증된 동행 매니저가', '함께해요'] },
  { icon: TapIcon, title: '간편한 이용', description: ['몇 번의 클릭만으로', '쉽게 요청할 수 있어요'] },
  { icon: HeartIcon, title: '더 건강한 일상', description: ['건강을 위해 필요한 곳에,', '필요한 동행으로'] },
];
