package com.gmbbd.checkMate.service;

import com.gmbbd.checkMate.model.EvaluationResult;
import com.gmbbd.checkMate.model.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AnalysisService analysisService;

    public String generateReport(MultipartFile requirements, MultipartFile submission) {

        SummaryResponse summary = SummaryResponse.from(
                analysisService.evaluate(requirements, submission)
        );

        StringBuilder sb = new StringBuilder();

        sb.append("[Checkmate 분석 보고서]\n");
        sb.append("============================================\n\n");

        sb.append("총 요구사항 수 : ").append(summary.getDetails().size()).append("\n");
        sb.append("- 충족(FULFILLED)       : ").append(summary.getFulfilled()).append("\n");
        sb.append("- 부분 충족(PARTIAL)    : ").append(summary.getPartial()).append("\n");
        sb.append("- 미충족(NOT_FULFILLED) : ").append(summary.getNotFulfilled()).append("\n\n");

        sb.append("============================================\n");
        sb.append("         요구사항별 상세 분석 결과\n");
        sb.append("============================================\n\n");

        for (EvaluationResult r : summary.getDetails()) {

            sb.append("■ 요구사항 #").append(r.getRequirementId()).append("\n");
            sb.append("내용   : ").append(r.getRequirementText()).append("\n");
            sb.append("판정   : ").append(r.getStatus()).append("\n");
            sb.append("근거   : ").append(
                    r.getEvidence() != null ? r.getEvidence() : "근거 없음"
            ).append("\n");
            sb.append("--------------------------------------------\n\n");
        }

        return sb.toString();
    }
}
