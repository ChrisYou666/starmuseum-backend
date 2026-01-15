package com.starmuseum.modules.media.service;

import com.starmuseum.modules.media.service.model.ExifSanitizeResult;

import java.nio.file.Path;

public interface ExifSanitizer {

    /**
     * 将 inputPath 的图片进行 EXIF 风险检测 + 清理（输出到 outputPath）
     *
     * @param inputPath     原始文件路径（临时 raw）
     * @param outputPath    清理后的输出路径（sanitized）
     * @param outputFormat  输出格式（如 "jpg","jpeg","png"）
     */
    ExifSanitizeResult sanitize(Path inputPath, Path outputPath, String outputFormat);

}
