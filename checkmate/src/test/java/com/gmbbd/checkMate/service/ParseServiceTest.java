package com.gmbbd.checkMate.service;

import com.gmbbd.checkMate.exception.ApiException;
import com.gmbbd.checkMate.util.TextCleaner;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParseServiceTest {

    private final TextCleaner cleaner = mock(TextCleaner.class);
    private final ParseService parseService = new ParseService(cleaner);

    @Test
    void extractText_shouldThrowException_whenFileIsNull() {
        assertThrows(ApiException.class, () -> parseService.extractText(null));
    }

    @Test
    void extractText_shouldThrowException_whenUnsupportedFile() {
        MockMultipartFile file =
                new MockMultipartFile("file", "hello.xyz", "text/plain", "hi".getBytes());

        assertThrows(ApiException.class, () -> parseService.extractText(file));
    }

    @Test
    void extractText_txtFile_shouldReturnCleanedText() {

        MockMultipartFile file =
                new MockMultipartFile("file", "sample.txt", "text/plain", "hello world".getBytes());

        // cleaner.clean() mocking
        when(cleaner.clean("hello world")).thenReturn("cleaned text");

        String result = parseService.extractText(file);

        assertEquals("cleaned text", result);
    }
}
