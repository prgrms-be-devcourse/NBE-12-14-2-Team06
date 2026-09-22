/** 백엔드 내 정보 응답 (UserResponse) — GET /api/v1/users/profile 이 돌려주는 모양 그대로. */
export type UserProfileDto = {
  /** 회원 번호(id)는 내려오지 않습니다. 본인 확인은 username 으로만 합니다. */
  username: string;
  email: string;
  name: string;
  role: 'CLIENT' | 'ESCORT' | 'ADMIN';
  gender: string;
  birthDate: string;
  phoneNum: string;
  region: string;
  createdAt: string;
};
