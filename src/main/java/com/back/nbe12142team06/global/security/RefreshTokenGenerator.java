package com.back.nbe12142team06.global.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class RefreshTokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();  // 강한 난수 생성기 사용했습니다.`
    private static final int TOKEN_BYTES = 32;  // 256 비트짜리 난수 생성 (브루트 포싱 방어)

    // 인스턴스 만드는 클래스가 아니라서 생성 시 컴파일 에러 나게 했습니다.
    private RefreshTokenGenerator() {}

    // 난수 생성
    public static String generate(){
        byte[] bytes = new byte[TOKEN_BYTES];

        // 지정한 바이트 길이만큼의 난수 생성
        RANDOM.nextBytes(bytes);

        /*
        난수로 제어 문자나 비출력 문자가 나올 수 있기 때문에
        Base64로 인코딩 getUrlEncoder인 이유는 일반 인코더는 +, /를 사용하는데
        이는 URL이나 쿠키에서 공백이나 경로 구분자로 해석한다고 합니다.
        getUrlEncoder는 이걸 -, _로 바꿔준다고합니다.
         */
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);   // "="으로 패딩하는 것을 방지 (나중에 모르고 =를 기준으로 자를 수도 있어서 추가했습니다.)
    }

    // DB에는 해시값만 저장
    public static String hash(String rawToken) {
        try {
            // SHA-256 해시 알고리즘 사용
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 문자열 난수를 바이트 단위로 변경 후 해싱
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            // 해시값을 16진수 문자열로 반환
            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 미지원", e);
        }
    }
}
