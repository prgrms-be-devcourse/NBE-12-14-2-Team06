package com.back.nbe12142team06.global.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.web.config.EnableSpringDataWebSupport;
//
//import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;
//
//@Configuration
//@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class DataWebConfig {
}
// 페이징 DTO에 대해서 불필요한 정보까지 나가는 걸 방지할 수 있다고 합니다.
// 월요일에 회의 후 결정하기 위해 일단은 주석 처리 후 기존 방식에 맞게 하겠습니다.
/*
 * [페이징 응답 구조 변경 설정] - 월요일 회의 후 적용 여부 결정
 *
 * 현재: 컨트롤러가 반환한 Page(PageImpl)를 Jackson이 getter 기준으로 그대로 직렬화
 *  - Spring 내부 구현이 그대로 노출되어, Spring Data 버전이 바뀌면 JSON 구조가 예고 없이 달라질 수 있음
 *  - 실행 시 "Serializing PageImpl instances as-is is not supported" 경고 발생
 *
 * 현재 응답 (중복·내부 정보 포함)
 * {
 *   "content": [...],
 *   "empty": false, "first": true, "last": false,
 *   "number": 0,                        // 현재 페이지
 *   "numberOfElements": 10,
 *   "size": 10,                         // 페이지 크기
 *   "totalElements": 20, "totalPages": 2,
 *   "sort": { "empty": false, "sorted": true, "unsorted": false },
 *   "pageable": {                       // ↓ 전부 위 값과 중복이거나 내부 계산용
 *     "offset": 0, "pageNumber": 0, "pageSize": 10,
 *     "paged": true, "unpaged": false,
 *     "sort": { "empty": false, "sorted": true, "unsorted": false }
 *   }
 * }
 *
 * 적용 후 (PagedModel, 구조 고정)
 * {
 *   "content": [...],
 *   "page": { "size": 10, "number": 0, "totalElements": 20, "totalPages": 2 }
 * }
 *
 * 적용 시 영향
 *  - 경로 변경: $.data.number → $.data.page.number (size, totalElements, totalPages 동일)
 *  - 제거되는 필드: first, last, empty, numberOfElements, pageable, sort
 *    → last는 number == totalPages - 1, empty는 content 길이로 판단
 *  - 목록 조회 테스트(공고, 지원, 관리자 회원 등)와 프론트의 페이지 정보 읽는 경로 함께 수정 필요
 */