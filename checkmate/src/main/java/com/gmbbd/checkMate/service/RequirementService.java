package com.gmbbd.checkMate.service;

import com.gmbbd.checkMate.model.Requirement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class RequirementService {


    private static final Pattern REQUIREMENT_HEADER_PATTERN = Pattern.compile(
            "^\\s*#{1,6}\\s*.*(요구사항|요구 사양|요구 조건|Requirements?)\\s*$"
    );


    private static final Pattern ANY_HEADER_PATTERN = Pattern.compile(
            "^\\s*#{1,6}\\s+.*$"
    );


    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile(
            "^\\s*(?:[-*+]\\s+|\\d+\\.\\s+|[\\[\\(]?(\\d+|[A-Za-z])[\\]\\)]\\s+)?.+"
    );


    private static final Pattern REQUIREMENT_SENTENCE_PATTERN = Pattern.compile(
            ".*(해야 한다|해야 함|해야 합니다|필수|반드시|요구된다|요구됨).*"
    );


    private static final Pattern SEPARATOR_LINE_PATTERN = Pattern.compile(
            "^\\s*[-=*_]{3,}\\s*$"
    );


    public List<Requirement> extractRequirements(String markdown) {
        List<Requirement> result = new ArrayList<>();
        if (markdown == null || markdown.isBlank()) {
            return result;
        }

        String[] lines = markdown.split("\\r?\\n");

        boolean inRequirementSection = false;
        Set<String> dedupSet = new LinkedHashSet<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line == null || line.trim().isEmpty() || SEPARATOR_LINE_PATTERN.matcher(line).matches()) {
                continue;
            }

            if (REQUIREMENT_HEADER_PATTERN.matcher(line).matches()) {
                inRequirementSection = true;
                continue;
            }

            if (inRequirementSection && ANY_HEADER_PATTERN.matcher(line).matches()) {
                if (!REQUIREMENT_HEADER_PATTERN.matcher(line).matches()) {
                    inRequirementSection = false;
                } else {
                    inRequirementSection = true;
                }
                continue;
            }

            if (inRequirementSection) {
                handleLineAsRequirementCandidate(line, dedupSet);
            } else {
                if (looksLikeGlobalRequirementLine(line)) {
                    handleLineAsRequirementCandidate(line, dedupSet);
                }
            }
        }

        long id = 1L;
        for (String text : dedupSet) {
            result.add(new Requirement(id++, text));
        }

        return result;
    }

    private void handleLineAsRequirementCandidate(String line, Set<String> dedupSet) {
        if (line == null) {
            return;
        }
        String trimmed = line.trim();
        if (trimmed.length() < 10) {
            return;
        }

        if (looksLikeTableLine(trimmed)) {
            return;
        }

        String cleaned = stripListPrefix(trimmed);

        if (cleaned.length() < 5) {
            return;
        }

        dedupSet.add(cleaned);
    }

    private boolean looksLikeGlobalRequirementLine(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.length() < 10) {
            return false;
        }

        if (REQUIREMENT_SENTENCE_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (LIST_ITEM_PATTERN.matcher(trimmed).matches()
                && REQUIREMENT_SENTENCE_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        return false;
    }

    private boolean looksLikeTableLine(String line) {
        int pipeCount = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '|') {
                pipeCount++;
                if (pipeCount >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private String stripListPrefix(String line) {
        if (line == null) {
            return "";
        }
        String trimmed = line.trim();

        if (trimmed.matches("^[-*+]\\s+.*")) {
            return trimmed.replaceFirst("^[-*+]\\s+", "").trim();
        }

        if (trimmed.matches("^\\d+\\.\\s+.*")) {
            return trimmed.replaceFirst("^\\d+\\.\\s+", "").trim();
        }

        if (trimmed.matches("^[\\[\\(]?(\\d+|[A-Za-z])[\\]\\)]\\s+.*")) {
            return trimmed.replaceFirst("^[\\[\\(]?(\\d+|[A-Za-z])[\\]\\)]\\s+", "").trim();
        }

        return trimmed;
    }
}
