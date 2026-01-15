package com.starmuseum.modules.media.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.starmuseum.modules.media.service.ExifSanitizer;
import com.starmuseum.modules.media.service.model.ExifSanitizeResult;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class ExifSanitizerImpl implements ExifSanitizer {

    @Override
    public ExifSanitizeResult sanitize(Path inputPath, Path outputPath, String outputFormat) {
        if (inputPath == null || outputPath == null) {
            throw new RuntimeException("sanitize: inputPath/outputPath is null");
        }

        boolean hasGps = false;
        boolean hasDevice = false;
        int orientation = 1;

        // 1) 读取 EXIF 元数据（检测风险）
        try (InputStream in = Files.newInputStream(inputPath)) {
            Metadata metadata = ImageMetadataReader.readMetadata(in);

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                hasGps = true;
            }

            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                String make = ifd0.getString(ExifIFD0Directory.TAG_MAKE);
                String model = ifd0.getString(ExifIFD0Directory.TAG_MODEL);
                if ((make != null && !make.isBlank()) || (model != null && !model.isBlank())) {
                    hasDevice = true;
                }

                Integer o = ifd0.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
                if (o != null) orientation = o;
            }

            // 有些机型把 orientation 放在 SubIFD
            if (orientation == 1) {
                ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                if (sub != null) {
                    Integer o = sub.getInteger(ExifSubIFDDirectory.TAG_ORIENTATION);
                    if (o != null) orientation = o;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取图片 EXIF 失败，拒绝上传（合规策略）: " + e.getMessage(), e);
        }

        // 2) ImageIO 读取并重编码输出（输出天然无 EXIF）
        BufferedImage image;
        try (InputStream in = Files.newInputStream(inputPath)) {
            image = ImageIO.read(in);
        } catch (Exception e) {
            throw new RuntimeException("ImageIO 读取图片失败，拒绝上传（可能是不支持的格式）: " + e.getMessage(), e);
        }

        if (image == null) {
            throw new RuntimeException("ImageIO 不支持该图片格式（可能是 HEIC/WEBP 等），已按合规策略拒绝上传");
        }

        BufferedImage rotated = applyOrientation(image, orientation);

        try {
            Files.createDirectories(outputPath.getParent());
            boolean ok = ImageIO.write(rotated, normalizeFormat(outputFormat), outputPath.toFile());
            if (!ok) {
                throw new RuntimeException("ImageIO.write 返回 false，输出格式不被支持: " + outputFormat);
            }
        } catch (Exception e) {
            throw new RuntimeException("写出 sanitized 图片失败: " + e.getMessage(), e);
        }

        return new ExifSanitizeResult(true, hasGps, hasDevice, LocalDateTime.now());
    }

    private String normalizeFormat(String fmt) {
        if (fmt == null) return "jpg";
        String f = fmt.trim().toLowerCase();
        if (f.equals("jpeg")) return "jpg";
        if (f.equals("jpg") || f.equals("png")) return f;
        // 阶段3：只允许 jpg/png（最稳）
        return "jpg";
    }

    /**
     * 根据 EXIF orientation 旋转图片，避免重编码后横竖错误
     */
    private BufferedImage applyOrientation(BufferedImage src, int orientation) {
        // orientation 常见值：1正常，3旋转180，6旋转90CW，8旋转270CW
        if (orientation == 1) return src;

        int w = src.getWidth();
        int h = src.getHeight();

        AffineTransform tx = new AffineTransform();
        int newW = w;
        int newH = h;

        switch (orientation) {
            case 3 -> {
                tx.translate(w, h);
                tx.rotate(Math.toRadians(180));
            }
            case 6 -> {
                newW = h;
                newH = w;
                tx.translate(h, 0);
                tx.rotate(Math.toRadians(90));
            }
            case 8 -> {
                newW = h;
                newH = w;
                tx.translate(0, w);
                tx.rotate(Math.toRadians(270));
            }
            default -> {
                return src; // 其他值先不处理
            }
        }

        BufferedImage dst = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dst.createGraphics();
        g2d.setTransform(tx);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return dst;
    }
}
