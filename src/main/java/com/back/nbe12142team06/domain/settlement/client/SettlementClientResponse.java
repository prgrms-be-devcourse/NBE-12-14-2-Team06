package com.back.nbe12142team06.domain.settlement.client;

import java.util.List;

public record SettlementClientResponse(
        // 거래고유번호(API)
        String api_tran_id,
        // 거래일시(밀리세컨드)
        String api_tran_dtm,
        // 응답코드(API)
        String rsp_code,
        // 응답메시지(API)
        String rsp_message,
        // 출금기관.표준코드
        String wd_bank_code_std,
        // 출금기관.점별코드
        String wd_bank_code_sub,
        // 출금기관명
        String wd_bank_name,
        // 출금계좌번호(출력용)
        String wd_account_num_masked,
        // 출금계좌인자내역
        String wd_print_content,
        // 송금인성명
        String wd_account_holder_name,
        // 입금건수
        int res_cnt,
        // 입금목록
        List<Req> res_list
        ) {
    public SettlementClientResponse(String account, String name, int amount) {
        this(
                "2ffd133a-d17a-431d-a6a5",
                "20190910101921567",
                "A0000",
                "",
                "097",
                "1230001",
                "오픈은행",
                "000-1230000-***",
                "%s 동행 매니저님 정산 완료".formatted(name),
                "병원 동행 서비스 가지",
                1,
                List.of(new Req(
                        1,
                        "F123456789U4BC34239Z",
                        20190910,
                        "097",
                        "000",
                        "",
                        "097",
                        "1230001",
                        "오픈은행",
                        "오픈저축은행",
                        account,
                        "001",
                        "%s-%s-***".formatted(account.substring(0,3), account.substring(3, account.length()-3)),
                        "쇼핑몰환불",
                        name,
                        amount,
                        "93848103221"
                ))
        );
    }

    record Req(
            // 거래순번
            int tran_no,
            // 거래고유번호(참가은행)
            String bank_tran_id,
            // 거래일자(참가은행)
            int bank_tran_date,
            // 응답코드를 부여한 참가은행.표준코드
            String bank_code_tran,
            // 응답코드(참가은행)
            String bank_rsp_code,
            // 응답메시지(참가은행)
            String bank_rsp_message,
            // 입금(개설)기관.표준코드
            String bank_code_std,
            // 입금(개설)기관.점별코드
            String bank_code_sub,
            // 입금(개설)기관명
            String bank_name,
            // 개별저축은행명
            String savings_bank_name,
            // 입금계좌번호
            String account_num,
            // 회차번호
            String account_seq,
            // 입금계좌번호(출력용)
            String account_num_masked,
            // 입금계좌인자내역
            String print_content,
            // 수취인성명
            String account_holder_name,
            // 거래금액
            int tran_amt,
            // CMS 번호
            String cms_num
    ) {}
}
