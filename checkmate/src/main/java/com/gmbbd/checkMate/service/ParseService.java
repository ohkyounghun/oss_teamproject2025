package com.gmbbd.checkMate.service;

import com.gmbbd.checkMate.exception.ApiException;
import com.gmbbd.checkMate.util.PandocExecutor;
import com.gmbbd.checkMate.util.TextCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@RequiredArgsConstructor
public class ParseService {

    private final PandocExecutor pandocExecutor;
    private final TextCleaner cleaner;

    public String extractText(MultipartFile multipartFile) {

        try {
            String name = multipartFile.getOriginalFilename().toLowerCase();

            if (!(name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".txt"))) {
                throw new ApiException("지원하지 않는 파일 형식입니다. (pdf/docx/txt)");
            }

            File temp = File.createTempFile("upload-", name.substring(name.lastIndexOf(".")));
            multipartFile.transferTo(temp);

            // Pandoc: Into Markdown
            String markdown = pandocExecutor.convertToMarkdown(temp);

            temp.delete();

            // Markdown-safe cleaning
            return cleaner.cleanMarkdown(markdown);

        } catch (Exception e) {
            throw new ApiException("업로드 파일 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
