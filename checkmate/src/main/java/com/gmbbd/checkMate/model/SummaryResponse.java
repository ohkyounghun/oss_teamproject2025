package com.gmbbd.checkMate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponse {

    private int fulfilled;
    private int partial;
    private int notFulfilled;
    private double score;

    private List<EvaluationResult> details;

    public static SummaryResponse from(List<EvaluationResult> results) {

        int f = 0, p = 0, u = 0;
        double scoreSum = 0;

        for (EvaluationResult r : results) {
            switch (r.getStatus()) {
                case "FULFILLED" -> f++;
                case "PARTIAL" -> p++;
                default -> u++;
            }
            scoreSum += r.getScore();
        }

        int total = results.size();
        double finalScore = total == 0 ? 0 : (scoreSum / total) * 100;

        return new SummaryResponse(f, p, u, finalScore, results);
    }
}
