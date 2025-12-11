package com.gmbbd.checkMate.service;

import com.gmbbd.checkMate.model.EvaluationResult;
import com.gmbbd.checkMate.model.Requirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * AnalysisService
 *  - 파일 검증
 *  - Pandoc 기반 텍스트(Markdown) 추출
 *  - 요구사항 추출(RequirementService)
 *  - LLM 평가 + 키워드 기반 평가(CompareService)를 결합해 최종 EvaluationResult 생성
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ValidationService validationService;
    private final ParseService parseService;
    private final RequirementService requirementService;
    private final LlmService llmService;
    private final CompareService compareService;

    public List<EvaluationResult> evaluate(MultipartFile requirements,
                                           MultipartFile submission) {

        // 1) 파일 검증
        validationService.validateFile(requirements);
        validationService.validateFile(submission);

        // 2) Markdown 텍스트 추출
        String requirementMarkdown = parseService.extractText(requirements);
        String submissionMarkdown = parseService.extractText(submission);

        // 3) 요구사항 추출 (Markdown 기반)
        List<Requirement> reqList = requirementService.extractRequirements(requirementMarkdown);

        List<EvaluationResult> results = new ArrayList<>();

        // 4) 각 요구사항에 대해 LLM + 키워드 평가 후 병합
        for (Requirement req : reqList) {
            if (req == null) {
                continue;
            }

            // LLM 의미 기반 평가
            EvaluationResult llmResult =
                    llmService.evaluateRequirement(req.getRawText(), submissionMarkdown);

            // 키워드 기반 평가
            EvaluationResult keywordResult =
                    compareService.compare(req, submissionMarkdown);

            // 최종 병합
            EvaluationResult finalResult =
                    mergeResults(req, llmResult, keywordResult);

            results.add(finalResult);
        }

        return results;
    }

    /**
     * LLM 결과 + 키워드 기반 결과를 하나의 EvaluationResult로 병합
     */
    private EvaluationResult mergeResults(Requirement req,
                                          EvaluationResult llmResult,
                                          EvaluationResult keywordResult) {

        if (llmResult == null && keywordResult == null) {
            return new EvaluationResult(
                    req.getId(),
                    req.getRawText(),
                    "NOT_FULFILLED",
                    0.0,
                    0,
                    0,
                    "평가 결과를 생성하지 못했다.",
                    "평가 결과를 생성하지 못했다."
            );
        }

        String llmStatus = safeStatus(llmResult != null ? llmResult.getStatus() : null);
        String keywordStatus = safeStatus(keywordResult != null ? keywordResult.getStatus() : null);

        int matched = keywordResult != null ? keywordResult.getMatchedKeywordCount() : 0;
        int total = keywordResult != null ? keywordResult.getTotalKeywordCount() : 0;
        double score = keywordResult != null ? keywordResult.getScore() : 0.0;

        // 1) status 병합
        String finalStatus = mergeStatus(llmStatus, keywordStatus);

        // 2) evidence 병합
        String llmEvidence = llmResult != null ? nullToEmpty(llmResult.getEvidence()) : "";
        StringBuilder evidenceBuilder = new StringBuilder();

        if (!llmEvidence.isBlank()) {
            evidenceBuilder.append(llmEvidence.trim());
        }

        if (total > 0) {
            if (evidenceBuilder.length() > 0) {
                evidenceBuilder.append(" ");
            }
            evidenceBuilder
                    .append("키워드 매칭 기준으로는 총 ")
                    .append(total)
                    .append("개 중 ")
                    .append(matched)
                    .append("개가 제출문에서 확인되었다.");
        }

        String finalEvidence = evidenceBuilder.length() > 0
                ? evidenceBuilder.toString()
                : "제출문에서 요구사항과 직접적으로 연관된 내용을 충분히 찾지 못했다.";

        // 3) EvaluationResult 생성
        EvaluationResult r = new EvaluationResult();
        r.setRequirementId(req.getId());
        r.setRequirementText(req.getRawText());
        r.setStatus(finalStatus);
        r.setScore(score);
        r.setMatchedKeywordCount(matched);
        r.setTotalKeywordCount(total);
        r.setEvidence(finalEvidence);
        r.setReason(finalEvidence); // reason은 evidence와 동일하게 둠

        return r;
    }

    /**
     * LLM status와 키워드 status 병합
     *  - 두 평가가 모두 FULFILLED이면 FULFILLED
     *  - LLM이 FULFILLED지만 키워드는 PARTIAL/NOT_FULFILLED → PARTIAL로 다운그레이드
     *  - LLM이 PARTIAL이고 키워드는 NOT_FULFILLED → NOT_FULFILLED로 다운그레이드
     *  - LLM이 NOT_FULFILLED인데 키워드는 FULFILLED → PARTIAL로 업그레이드
     *  - 그 외에는 LLM status 그대로 사용
     */
    private String mergeStatus(String llmStatus, String keywordStatus) {

        // 둘 다 FULFILLED
        if ("FULFILLED".equals(llmStatus) && "FULFILLED".equals(keywordStatus)) {
            return "FULFILLED";
        }

        // LLM은 충족이라고 보지만 키워드는 불충족/부분충족 → 한 단계 다운
        if ("FULFILLED".equals(llmStatus)
                && ("PARTIAL".equals(keywordStatus) || "NOT_FULFILLED".equals(keywordStatus))) {
            return "PARTIAL";
        }

        // LLM은 PARTIAL인데 키워드는 NOT_FULFILLED → NOT_FULFILLED로 다운
        if ("PARTIAL".equals(llmStatus) && "NOT_FULFILLED".equals(keywordStatus)) {
            return "NOT_FULFILLED";
        }

        // LLM은 NOT_FULFILLED인데 키워드는 FULFILLED → PARTIAL로 업
        if ("NOT_FULFILLED".equals(llmStatus) && "FULFILLED".equals(keywordStatus)) {
            return "PARTIAL";
        }

        return llmStatus;
    }

    private String safeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "NOT_FULFILLED";
        }
        return status.trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
