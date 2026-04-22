package org.example.algorithms.dto;

import org.springframework.web.bind.annotation.RequestParam;

public record RleCompressRequest(String text, Boolean caseSensitive) {
    public RleCompressRequest {
        if (caseSensitive == null) caseSensitive = true;
    }
}
