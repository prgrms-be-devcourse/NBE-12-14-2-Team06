package com.back.nbe12142team06.domain.report.service;

import com.back.nbe12142team06.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

// 외부 API 전송 전 식별 정보를 가린다.
// 병명, 처방 등 진료 내용은 요약에 필요하므로 마스킹 대상이 아니다.
@Component
public class ReportMasker {

    // 010-1234-5678, 01012345678 등
    private static final Pattern PHONE = Pattern.compile("01[0-9][-. ]?\\d{3,4}[-. ]?\\d{4}");

    public String mask(String originContent, User client, User escort) {

        String masked = originContent;

        masked = replaceName(masked, client.getName(), "환자분");
        masked = replaceName(masked, escort.getName(), "동행인");

        return PHONE.matcher(masked).replaceAll("[연락처]");
    }

    private String replaceName(String content, String name, String replacement) {
        if (name == null || name.isBlank()) {
            return content;
        }
        return content.replace(name, replacement);
    }
}