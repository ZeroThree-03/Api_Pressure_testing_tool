package com.pressurtest.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CurlParserService {

    private static final Pattern URL_PATTERN = Pattern.compile("(?:curl\\s+)?(?:-X\\s+\\w+\\s+)?(['\"]?)(https?://[^\\s'\"]+)\\1");
    private static final Pattern METHOD_PATTERN = Pattern.compile("-X\\s+(\\w+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("-H\\s+(['\"])([^'\"]+)\\1");
    private static final Pattern BODY_PATTERN = Pattern.compile("-d\\s+(['\"])([^'\"]+)\\1");
    private static final Pattern USER_PATTERN = Pattern.compile("-u\\s+(['\"])([^'\"]+)\\1");

    public Map<String, Object> parse(String curl) {
        if (curl == null || curl.isBlank()) {
            throw new IllegalArgumentException("Curl命令不能为空");
        }

        curl = curl.replaceAll("\\\\\\s*\\n\\s*", " ").trim();

        Map<String, Object> result = new HashMap<>();

        String url = parseUrl(curl);
        result.put("url", url);

        String method = parseMethod(curl);
        result.put("method", method);

        Map<String, String> headers = parseHeaders(curl);
        result.put("headers", headers);

        String body = parseBody(curl);
        if (body != null) {
            result.put("body", body);
        }

        String auth = parseAuth(curl);
        if (auth != null) {
            result.put("auth", auth);
        }

        List<String> params = identifyParams(url, headers, body);
        result.put("params", params);

        return result;
    }

    private String parseUrl(String curl) {
        Matcher matcher = URL_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException("无法解析URL");
    }

    private String parseMethod(String curl) {
        Matcher matcher = METHOD_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        if (parseBody(curl) != null) {
            return "POST";
        }
        return "GET";
    }

    private Map<String, String> parseHeaders(String curl) {
        Map<String, String> headers = new LinkedHashMap<>();
        Matcher matcher = HEADER_PATTERN.matcher(curl);
        while (matcher.find()) {
            String header = matcher.group(2);
            int colonIndex = header.indexOf(':');
            if (colonIndex > 0) {
                String key = header.substring(0, colonIndex).trim();
                String value = header.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String parseBody(String curl) {
        Matcher matcher = BODY_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private String parseAuth(String curl) {
        Matcher matcher = USER_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private List<String> identifyParams(String url, Map<String, String> headers, String body) {
        List<String> params = new ArrayList<>();
        Pattern varPattern = Pattern.compile("\\$\\{([^}]+)\\}");

        Matcher urlMatcher = varPattern.matcher(url);
        while (urlMatcher.find()) {
            params.add(urlMatcher.group(1));
        }

        for (String value : headers.values()) {
            Matcher headerMatcher = varPattern.matcher(value);
            while (headerMatcher.find()) {
                params.add(headerMatcher.group(1));
            }
        }

        if (body != null) {
            Matcher bodyMatcher = varPattern.matcher(body);
            while (bodyMatcher.find()) {
                params.add(bodyMatcher.group(1));
            }
        }

        return params;
    }
}
