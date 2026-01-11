package com.starmuseum.modules.astro.service.impl;

import com.starmuseum.modules.astro.service.AstronomyService;
import com.starmuseum.modules.astro.service.model.AltAzResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
public class AstronomyServiceImpl implements AstronomyService {

    @Override
    public AltAzResult calcAltAz(String time, double lat, double lon, double raDeg, double decDeg) {
        // 入参基础保护：避免 NaN 传播
        if (Double.isNaN(lat) || Double.isNaN(lon) || Double.isNaN(raDeg) || Double.isNaN(decDeg)) {
            return AltAzResult.of(null, null, false);
        }

        Instant instant;
        try {
            // 要求 time 带时区：2026-01-10T12:00:00Z / +08:00 等
            instant = OffsetDateTime.parse(time).toInstant();
        } catch (DateTimeParseException e) {
            // 这里不抛异常，交给你 Controller/全局异常统一处理也行；
            // 但为了不影响你编译先返回空（你也可以改为 throw）
            return AltAzResult.of(null, null, false);
        }

        // 角度转弧度
        double latRad = Math.toRadians(lat);
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);

        // 计算儒略日 JD
        double jd = toJulianDate(instant);

        // 计算格林尼治平恒星时 GMST（小时 -> 弧度）
        double gmstHours = gmstHours(jd);
        double gmstRad = Math.toRadians(gmstHours * 15.0);

        // 本地恒星时 LST = GMST + 经度（经度东为正）
        double lstRad = gmstRad + Math.toRadians(lon);
        lstRad = normalizeRad(lstRad);

        // 时角 H = LST - RA
        double hRad = normalizeRad(lstRad - raRad);

        // 高度角 alt
        double sinAlt = Math.sin(decRad) * Math.sin(latRad)
            + Math.cos(decRad) * Math.cos(latRad) * Math.cos(hRad);
        sinAlt = clamp(sinAlt, -1.0, 1.0);
        double altRad = Math.asin(sinAlt);

        // 方位角 az（0~360）
        // 常用公式：az = atan2( sin(H), cos(H)*sin(lat) - tan(dec)*cos(lat) )
        double y = Math.sin(hRad);
        double x = Math.cos(hRad) * Math.sin(latRad) - Math.tan(decRad) * Math.cos(latRad);
        double azRad = Math.atan2(y, x);

        // 转为“从北起算 0~360”的角度
        double azDeg = (Math.toDegrees(azRad) + 180.0) % 360.0;
        double altDeg = Math.toDegrees(altRad);

        boolean visible = altDeg > 0.0;

        return AltAzResult.of(altDeg, azDeg, visible);
    }

    private static double toJulianDate(Instant instant) {
        // JD of Unix epoch 1970-01-01T00:00:00Z is 2440587.5
        double unixSeconds = instant.getEpochSecond() + instant.getNano() / 1_000_000_000.0;
        return 2440587.5 + unixSeconds / 86400.0;
    }

    private static double gmstHours(double jd) {
        // 近似 GMST 公式（MVP 足够，后续阶段4再升级精度）
        double d = jd - 2451545.0;
        double gmst = 18.697374558 + 24.06570982441908 * d;
        gmst = gmst % 24.0;
        if (gmst < 0) gmst += 24.0;
        return gmst;
    }

    private static double normalizeRad(double rad) {
        double twoPi = Math.PI * 2.0;
        rad = rad % twoPi;
        if (rad < 0) rad += twoPi;
        return rad;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
