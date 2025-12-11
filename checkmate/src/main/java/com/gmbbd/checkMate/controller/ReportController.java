package com.gmbbd.checkMate.controller;

import com.gmbbd.checkMate.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService; // 보고서 생성

    @PostMapping(
            value = "/report",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String generateReport(
            @RequestPart("requirements") MultipartFile requirements, // 요구사항 문서 파일
            @RequestPart("submission") MultipartFile submission       // 제출물 문서 파일
    ) {
        // 두 파일을 넘겨서 서비스 계층에서 TXT 리포트 생성 후 그대로 문자열 반환
        return reportService.generateReport(requirements, submission);
    }
}
