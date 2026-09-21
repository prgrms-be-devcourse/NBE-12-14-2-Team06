'use client';

import Image from 'next/image';
import { SOCIAL_PROVIDERS } from '../model';

/** Figma 564:18001 — 흰 원형 버튼에 얹힌 은은한 두 겹 그림자 */
const BUTTON_SHADOW = '0 0 1px 0 rgba(12,26,75,0.2), 0 1px 3px 0 rgba(50,50,71,0.1)';

/** 간편 로그인 버튼 줄 (Figma 564:17998) */
export default function SocialLoginRow() {
  return (
    <div className="flex items-center justify-center gap-8">
      {SOCIAL_PROVIDERS.map(({ id, label, icon, width, height, left, top, badgeColor }) => (
        // TODO: 소셜 로그인 API(OAuth2 인가 요청)가 정해지면 연결하세요.
        <button
          key={id}
          type="button"
          aria-label={label}
          style={{ boxShadow: BUTTON_SHADOW }}
          className="relative size-[52px] shrink-0 rounded-full bg-white transition-transform hover:-translate-y-0.5"
        >
          {badgeColor && (
            <span
              aria-hidden="true"
              style={{ backgroundColor: badgeColor, boxShadow: BUTTON_SHADOW }}
              className="absolute top-[9px] left-[9px] size-[35px] rounded-full"
            />
          )}
          <Image
            src={icon}
            alt=""
            width={width}
            height={height}
            style={{ left, top }}
            className="absolute max-w-none"
          />
        </button>
      ))}
    </div>
  );
}
