package com.gmbbd.checkMate.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * LLM-Friendly Markdown Cleaner
 * - 표 제거
 * - 이미지 제거
 * - 링크 정제
 * - HTML 태그 제거
 * - 코드블록 제거
 * - footnote 제거
 * - 공백 정리
 */
@Component
public class MarkdownCleaner {

    private static final Pattern TABLE_LINE_PATTERN =
            Pattern.compile(".*\\|.*\\|.*"); // 파이프 2개 이상 포함 라인

    private static final Pattern IMAGE_PATTERN =
            Pattern.compile("!?\\[.*?]\\(.*?\\)");

    private static final Pattern LINK_PATTERN =
            Pattern.compile("\\[(.*?)\\]\\([^\\)]*\\)");

    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<[^>]+>");

    private static final Pattern FOOTNOTE_REF_PATTERN =
            Pattern.compile("\\[\\^\\w+\\]");

    private static final Pattern FOOTNOTE_DEF_PATTERN =
            Pattern.compile("^\\[\\^\\w+]:.*$");

    private static final Pattern CODEBLOCK_PATTERN =
            Pattern.compile("```[\\s\\S]*?```");

    private static final Pattern MULTIPLE_EMPTY_LINES =
            Pattern.compile("\\n{2,}");

    /**
     * 제출물 문서 전체를 정제한다.
     * - Markdown 구조는 유지하되, 불필요 요소를 제거
     */
    public String cleanForLLM(String md) {

        if (md == null || md.isBlank()) {
            return "";
        }

        String cleaned = md;

        // 1) 코드 블록 제거
        cleaned = CODEBLOCK_PATTERN.matcher(cleaned).replaceAll("");

        // 2) HTML 태그 제거
        cleaned = HTML_TAG_PATTERN.matcher(cleaned).replaceAll("");

        // 3) footnote 제거
        cleaned = FOOTNOTE_REF_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = FOOTNOTE_DEF_PATTERN.matcher(cleaned).replaceAll("");

        // 4) 이미지 제거
        cleaned = IMAGE_PATTERN.matcher(cleaned).replaceAll("");

        // 5) 링크 → 텍스트만 남김
        cleaned = LINK_PATTERN.matcher(cleaned).replaceAll("$1");

        // 6) 표 제거
        StringBuilder sb = new StringBuilder();
        for (String line : cleaned.split("\\r?\\n")) {
            if (!TABLE_LINE_PATTERN.matcher(line).matches()) {
                sb.append(line).append("\n");
            }
        }
        cleaned = sb.toString();

        // 7) 연속 빈 줄 정리
        cleaned = MULTIPLE_EMPTY_LINES.matcher(cleaned).replaceAll("\n");

        return cleaned.trim();
    }
}
