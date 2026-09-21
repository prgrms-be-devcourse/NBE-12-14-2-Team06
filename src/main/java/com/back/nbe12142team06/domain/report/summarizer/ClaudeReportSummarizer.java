package com.back.nbe12142team06.domain.report.summarizer;

import com.back.nbe12142team06.domain.report.dto.ReportSummary;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude API 로 진료 보고서를 요약한다.
 * <p>
 * 여기로 넘어오는 content 는 이미 {@code ReportMasker} 로 실명·연락처가 가려진 텍스트다.
 * 원문을 그대로 외부로 보내지 않는다.
 */
@Slf4j
@Component
@Profile("!test")
public class ClaudeReportSummarizer implements ReportSummarizer {

    private static final String SYSTEM_PROMPT = """
            너는 고령자 병원 동행 서비스의 진료 보고서를 보호자에게 전달할 수 있게 요약하는 도우미다.
            동행인이 작성한 진료 보고서 원문을 읽고 아래 JSON 형식으로만 답한다.

            {
              "visitPurpose": "방문 목적",
              "diagnosis": "의사 소견",
              "prescription": "처방 내역",
              "nextVisit": "다음 방문 일정",
              "caregiverNote": "보호자가 챙겨야 할 사항"
            }

            규칙
            - 원문에 없는 내용은 절대 지어내지 않는다. 해당 항목이 원문에 없으면 그 필드는 null 로 둔다.
            - 각 필드는 한국어 한두 문장으로 간결하게 쓴다.
            - 진단을 새로 내리거나 의학적 조언을 덧붙이지 않는다. 원문에 적힌 내용만 옮긴다.
            - 원문의 "환자분", "동행인" 같은 표현은 그대로 둔다. 이름을 추측해서 채우지 않는다.
            - 설명이나 인사말 없이, 여는 중괄호로 시작해서 닫는 중괄호로 끝나는 JSON 만 출력한다.

            예시
            원문: 오늘 환자분 모시고 정형외과 다녀왔습니다. 무릎이 계속 아프다고 하셔서요.
            엑스레이 찍었고 퇴행성 관절염 초기라고 하셨습니다. 소염진통제 2주분 받았고,
            2주 뒤에 다시 오라고 하셨어요. 계단은 되도록 피하시는 게 좋다고 합니다.
            출력:
            {
              "visitPurpose": "무릎 통증으로 정형외과 진료를 받았습니다.",
              "diagnosis": "엑스레이 결과 퇴행성 관절염 초기 소견입니다.",
              "prescription": "소염진통제 2주분을 처방받았습니다.",
              "nextVisit": "2주 뒤 재방문 예정입니다.",
              "caregiverNote": "계단 이용은 되도록 피하시는 것이 좋습니다."
            }
            """;

    private final RestClient claudeRestClient;
    private final String model;
    private final int maxTokens;

    // Spring Boot 4 에서는 ObjectMapper 가 자동으로 빈 등록되지 않아 직접 생성한다.
    // 응답에 우리가 모르는 필드가 섞여도 깨지지 않도록 설정한다.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ClaudeReportSummarizer(
            @Qualifier("claudeRestClient") RestClient claudeRestClient,
            @Value("${custom.claude.model}") String model,
            @Value("${custom.claude.max-tokens}") int maxTokens
    ) {
        this.claudeRestClient = claudeRestClient;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @Override
    public ReportSummary summarize(String maskedContent) {

        String responseBody = claudeRestClient.post()
                .uri("/v1/messages")
                .body(buildRequestBody(maskedContent))
                .retrieve()
                .body(String.class);

        return parseSummary(responseBody);
    }

    /**
     * 요청 본문을 만든다.
     * <p>
     * 대화는 반드시 user 메시지로 끝나야 한다.
     * assistant 턴을 미리 채워 넣는 prefill 방식은 이 모델이 지원하지 않는다.
     */
    private Map<String, Object> buildRequestBody(String maskedContent) {
        return Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", SYSTEM_PROMPT,
                "messages", List.of(
                        Map.of("role", "user",
                                "content", "다음 진료 보고서를 요약해줘. JSON 만 출력해.\n\n" + maskedContent)
                )
        );
    }

    /**
     * 응답에서 요약 JSON 을 꺼낸다.
     * 응답 구조: { "content": [ { "type": "text", "text": "..." } ], ... }
     */
    private ReportSummary parseSummary(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("content").path(0).path("text").asText("");

            return objectMapper.readValue(extractJson(text), ReportSummary.class);

        } catch (Exception e) {
            throw new IllegalStateException("Claude 응답을 요약 형식으로 변환하지 못했습니다.", e);
        }
    }

    /**
     * 모델이 JSON 앞뒤에 코드블록 표시나 설명을 붙이는 경우가 있어,
     * 첫 번째 여는 중괄호부터 마지막 닫는 중괄호까지만 잘라낸다.
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalStateException("응답에서 JSON 을 찾지 못했습니다: " + text);
        }
        return text.substring(start, end + 1);
    }
}