/** 백엔드가 항상 감싸서 주는 응답 틀 (백엔드 RsData) */
export type RsData<T> = {
    statusCode: string;
    msg: string;
    data: T;
};

/** 백엔드가 준 실패 응답. msg 뿐 아니라 statusCode 도 같이 들고 있는 에러입니다. */
export class ApiError extends Error {
    readonly statusCode: string;

    constructor(statusCode: string, msg: string) {
        super(msg);
        this.name = 'ApiError';
        this.statusCode = statusCode;
    }
}

/**
 * 백엔드 API 호출 함수.
 * 성공하면 응답의 data 만 돌려주고, 실패하면 ApiError 를 던집니다.
 */
export async function api<T>(path: string, init?: RequestInit): Promise<T> {
    // 1. 서버 호출 (응답이 올 때까지 기다림)
    const res = await fetch(path, init);

    // 2. 응답 본문을 JSON 객체로 바꿈 (RsData 모양)
    const body: RsData<T> = await res.json();

    // 3. 실패(상태 코드가 200번대가 아님)면 에러를 던짐
    if (!res.ok) {
        throw new ApiError(body.statusCode, body.msg);
    }

    // 4. 성공이면 껍질을 벗기고 data 만 돌려줌
    return body.data;
}

/** 백엔드(Spring)가 목록에 붙여 주는 페이지 정보 */
export type SpringPage<T> = {
    content: T[];        // 공고 배열
    totalPages: number;
    totalElements: number;
    number: number;      // 지금 페이지 번호 (0부터)
    size: number;
};