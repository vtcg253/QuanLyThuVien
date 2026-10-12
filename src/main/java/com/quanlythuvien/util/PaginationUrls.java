package com.quanlythuvien.util;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public final class PaginationUrls {

    private PaginationUrls() {
    }

    public static String forPath(String path, Map<String, String> queryParameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        queryParameters.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                builder.queryParam(key, value);
            }
        });
        String url = builder.build().encode().toUriString();
        return url + (builder.build().getQueryParams().isEmpty() ? "?page=" : "&page=");
    }
}
