package com.back.nbe12142team06.domain.user.service;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EscortProfileConcurrencyTest {

    // TODO: 교육 이수와 프로필 수정이 겹칠 때
    // TODO: 계좌 변경과 리뷰 평점 반영이 겹칠 때
    // TODO: 리뷰 두 건이 동시에 들어올 때
    // TODO: 리뷰 여러 건이 한꺼번에 몰릴 때
    // TODO: 동행 완료와 노쇼가 겹칠 때


}
