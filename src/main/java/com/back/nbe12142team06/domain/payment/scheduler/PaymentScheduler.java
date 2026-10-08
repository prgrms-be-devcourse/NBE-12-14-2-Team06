package com.back.nbe12142team06.domain.payment.scheduler;

import com.back.nbe12142team06.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "0 * * * * *")
    public void cancelPaymentScheduler() {
        int[] counts = paymentService.cancelPostAndPaymentCallApi();

        if (counts[0] > 0) {
            log.info("공고 삭제 - 결제 취소 스케줄링 총 {}건, 성공 {}건, 실패 {}건", counts[0], counts[1], counts[2]);
        }
    }

}
