package com.gmbbd.checkMate.controller;

import com.gmbbd.checkMate.model.EvaluationResult;
import com.gmbbd.checkMate.model.SummaryResponse;
import com.gmbbd.checkMate.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalysisService analysisService;

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public SummaryResponse analyze(
            @RequestPart("requirements") MultipartFile req,
            @RequestPart("submission") MultipartFile sub
    ) {
        List<EvaluationResult> results = analysisService.evaluate(req, sub);

        return SummaryResponse.from(results);
    }
}
