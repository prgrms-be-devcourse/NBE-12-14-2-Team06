package com.back.nbe12142team06.domain.settlement.client;

import java.util.List;

public record SettlementClientRequest(
        // 금융결제원 AccessToken
        String Authorization,
        // 약정 계좌/계정구분
        String cntr_account_type,
        // 약정 계좌/계정 번호
        String cntr_account_num,
        // 입금이체용 암호문구
        String wd_pass_phrase,
        // 출금계좌인자내역
        String wd_print_content,
        // 수취인성명검증 여부(on:검증함) (미지정 시 기본값: "on")
        String name_check_option,
        // 요청일시
        String tran_dtime,
        // 입금요청건수
        int req_cnt,
        // 입금요청목록
        List<Req> req_list
        ) {
    public SettlementClientRequest(String account, String name, int amount) {
        this(
                "Bearer <access_token>",
                "N",
                "1101230000678",
                "790d56ed........821a69",
                "출금계좌인자내역",
                "off",
                "20190910101921",
                1,
                List.of(new Req(
                        1,
                        "F123456789U4BC34239Z",
                        "097",
                        account,
                        name,
                        "동행 매니저 급여",
                        amount,
                        "병원 동행 서비스 가지",
                        "HONGGILDONG1234",
                        "TR"
                        ))
        );
    }

    record Req(
            // 거래순번
            int tran_no,
            // 은행거래고유번호
            String bank_tran_id,
            // 입금은행.표준코드
            String bank_code_std,
            // 계좌번호
            String account_num,
            // 입금계좌예금주명
            String account_holder_name,
            // 입금계좌인자내역
            String print_content,
            // 거래금액
            int tran_amt,
            // 요청고객성명
            String req_client_name,
            // 요청고객회원번호
            String req_client_num,
            // 이체용도
            String transfer_purpose) {
    }
}
