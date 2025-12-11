package com.gmbbd.checkMate.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class PandocExecutor {

    public String convertToMarkdown(File input) {
        try {
            File output = File.createTempFile("pandoc-", ".md");

            ProcessBuilder pb = new ProcessBuilder(
                    "pandoc",
                    input.getAbsolutePath(),
                    "-f", "auto",      // 입력 자동 감지(pdf/docx/txt 모두 가능)
                    "-t", "markdown",
                    "-o", output.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exit = p.waitFor();

            if (exit != 0) {
                throw new RuntimeException("Pandoc 변환 실패: exit code = " + exit);
            }

            String result = Files.readString(output.toPath(), StandardCharsets.UTF_8);

            output.delete();
            return result;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Pandoc 실행 오류: " + e.getMessage(), e);
        }
    }
}
