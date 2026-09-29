import { StepBar } from '@/components/ui';
import type { EscortStage } from '../../types';

const STEP_LABELS = ['매칭 완료', '동행 중', '동행 완료'];
const STAGE_INDEX: Record<EscortStage, number> = { ready: 0, ongoing: 1, done: 2 };

/** 위쪽 3단계 표시 (Figma 진행상황: 파란색 계열) */
export default function StageBar({ stage }: { stage: EscortStage }) {
  return (
    <StepBar
      steps={STEP_LABELS}
      current={STAGE_INDEX[stage] + 1}
      label="동행 진행 단계"
      className="mx-auto mb-[30px] max-w-[411px]"
    />
  );
}
