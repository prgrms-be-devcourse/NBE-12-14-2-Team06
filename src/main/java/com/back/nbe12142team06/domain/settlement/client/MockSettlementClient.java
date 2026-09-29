package com.back.nbe12142team06.domain.settlement.client;

import org.springframework.stereotype.Component;

@Component
public class MockSettlementClient implements SettlementClient{

    @Override
    public SettlementClientResponse settlementRequest(Object request) {

        // 정산 로직 수행 중
        SettlementClientRequest.Req req = ((SettlementClientRequest) request).req_list().getFirst();

        return new SettlementClientResponse(req.account_num(), req.account_holder_name(), req.tran_amt());
    }
}
