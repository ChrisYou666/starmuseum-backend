package com.starmuseum.modules.catalog.util;

import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 安全解压 zip（防 Zip Slip）
 */
public class SafeZip {

    private SafeZip() {}

    public static Path unzip(InputStream zipIn, Path destDir) throws IOException {
        Files.createDirectories(destDir);

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipIn))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                String name = entry.getName();
                if (!StringUtils.hasText(name)) continue;

                // 统一分隔符
                name = name.replace('\\', '/');

                Path target = destDir.resolve(name).normalize();

                // Zip Slip 防护：确保 target 在 destDir 内
                if (!target.startsWith(destDir)) {
                    throw new IOException("Zip entry outside target dir: " + name);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }

        return destDir;
    }
}
