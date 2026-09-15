package com.back.nbe12142team06.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * 여러 명이 동시에 예외 코드를 추가하면서 번호가 겹치는 것을 막기 위한 가드 테스트입니다.
 *
 * UnauthorizedException/NotFoundException/InvalidException/DuplicatedException/
 * InternalServerErrorException 은 new XException(코드, "메시지") 형태로 세부 번호를 붙이는데,
 * 이 번호는 파일/도메인 단위가 아니라 "같은 예외 타입" 기준으로 프로젝트 전체에서 유일해야 합니다.
 * (ResponseAspect가 statusCode의 접두어로 실제 HTTP 상태코드를 결정하기 때문에, 프론트 입장에서는
 * statusCode 전체가 하나의 식별자입니다.)
 *
 * 같은 번호라도 메시지가 완전히 동일하면 "같은 상황을 재사용"한 것으로 보고 통과시키고,
 * 메시지가 다르면 서로 다른 상황인데 번호만 충돌한 것이므로 실패시킵니다.
 *
 * 주의: new XException(...) 호출이 한 줄에 작성돼 있다는 전제로 정규식을 적용합니다.
 * (현재 컨벤션이 전부 한 줄이라 이렇게 구현했습니다. 새 예외를 추가할 때도 한 줄로 유지해주세요.
 * 여러 줄로 쪼개서 쓰면 이 테스트가 해당 호출을 못 잡아낼 수 있습니다.)
 */
class ExceptionCodeDuplicationTest {

    private static final List<String> EXCEPTION_TYPES = List.of(
            "UnauthorizedException",
            "NotFoundException",
            "InvalidException",
            "DuplicatedException",
            "InternalServerErrorException"
    );

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "new\\s+(" + String.join("|", EXCEPTION_TYPES) + ")\\s*\\(\\s*(\\d+)\\s*,(.*)"
    );

    private record Occurrence(String file, int line, String message) {
    }

    @Test
    @DisplayName("예외 코드 번호는 같은 예외 타입 안에서 서로 다른 의미로 중복되면 안 된다")
    void noDuplicatedExceptionCodesWithDifferentMeanings() throws IOException {
        Map<String, Map<Integer, List<Occurrence>>> byTypeAndCode = new LinkedHashMap<>();

        Path root = Paths.get("src/main/java");
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path file : javaFiles) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    Matcher m = LINE_PATTERN.matcher(lines.get(i));
                    if (m.find()) {
                        String type = m.group(1);
                        int code = Integer.parseInt(m.group(2));
                        // 메시지 뒤에 붙는 ), ), ; 등(orElseThrow 같은 중첩 호출의 닫는 괄호 포함)을 모두 걷어내고 비교합니다.
                        String message = m.group(3).replaceAll("[\\s);]+$", "").trim();

                        byTypeAndCode
                                .computeIfAbsent(type, t -> new LinkedHashMap<>())
                                .computeIfAbsent(code, c -> new ArrayList<>())
                                .add(new Occurrence(root.relativize(file).toString(), i + 1, message));
                    }
                }
            }
        }

        StringBuilder failures = new StringBuilder();
        for (Map.Entry<String, Map<Integer, List<Occurrence>>> typeEntry : byTypeAndCode.entrySet()) {
            String type = typeEntry.getKey();
            for (Map.Entry<Integer, List<Occurrence>> codeEntry : typeEntry.getValue().entrySet()) {
                int code = codeEntry.getKey();
                List<Occurrence> occurrences = codeEntry.getValue();

                Set<String> distinctMessages = new LinkedHashSet<>();
                for (Occurrence o : occurrences) {
                    distinctMessages.add(o.message());
                }

                if (distinctMessages.size() > 1) {
                    failures.append(String.format(
                            "%n[중복] %s 코드 %d 이(가) 서로 다른 의미로 %d곳에서 사용됨:%n",
                            type, code, occurrences.size()));
                    for (Occurrence o : occurrences) {
                        failures.append(String.format("  - %s:%d  %s%n", o.file(), o.line(), o.message()));
                    }
                }
            }
        }

        if (!failures.isEmpty()) {
            fail("예외 코드 번호가 서로 다른 상황에 중복 사용되었습니다. 겹치지 않는 번호로 바꿔주세요." + failures);
        }
    }
}
