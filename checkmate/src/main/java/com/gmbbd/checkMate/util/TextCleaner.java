package com.gmbbd.checkMate.util;

import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    public String cleanMarkdown(String raw) {
        if (raw == null) return "";

        String text = raw;

        text = normalizeWhitespace(text);
        text = unifyNewlines(text);
        text = removeControlChars(text); // Markdown 문법 보존

        return text.trim();
    }

    private String removeControlChars(String s) {
        // 일반 텍스트에는 거의 나타나지 않는 제어 문자 제거
        return s.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    }

    private String normalizeWhitespace(String s) {
        // 탭 → 공백
        return s.replaceAll("\\t", " ");
    }

    private String unifyNewlines(String s) {
        // CRLF/CR → LF
        return s.replaceAll("\\r\\n?", "\n");
    }
}
