package com.gmbbd.checkMate.util;

import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    public String clean(String raw) {
        if (raw == null) return "";

        String text = raw;

        text = removeBrokenCharacters(text);
        text = normalizeWhitespace(text);
        text = unifyNewlines(text);

        return text.trim();
    }

    private String removeBrokenCharacters(String s) {
        // 글자(문자), 숫자, 구두점, 공백, 개행만 허용
        return s.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "");
    }

    private String normalizeWhitespace(String s) {
        // 연속 공백/탭 → 한 칸
        return s.replaceAll("[ \\t]{2,}", " ");
    }

    private String unifyNewlines(String s) {
        // CRLF/CR → LF
        return s.replaceAll("[\\r\\n]+", "\n");
    }
}
