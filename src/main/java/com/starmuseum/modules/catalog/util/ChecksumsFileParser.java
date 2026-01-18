package com.starmuseum.modules.catalog.util;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析 checksums.sha256：
 * 常见格式：
 *   <sha256>  <filename>
 */
public class ChecksumsFileParser {

    private ChecksumsFileParser() {}

    public static Map<String, String> parse(InputStream in) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Map<String, String> map = new LinkedHashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                String hash = parts[0].trim();
                String file = parts[1].trim();

                if (!StringUtils.hasText(hash) || !StringUtils.hasText(file)) continue;
                map.put(normalize(file), hash.toLowerCase());
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("parse checksums.sha256 failed", e);
        }
    }

    private static String normalize(String p) {
        return p.replace('\\', '/');
    }
}
