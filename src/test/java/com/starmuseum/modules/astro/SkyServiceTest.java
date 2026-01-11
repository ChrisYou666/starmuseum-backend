package com.starmuseum.modules.astro;

import com.starmuseum.modules.astro.service.SkyService;
import com.starmuseum.modules.astro.vo.StarPositionVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkyServiceTest {

    @Autowired
    private SkyService skyService;

    @Test
    void shouldComputePositionsForStars() {
        // 用 UTC 时间（阶段2 MVP 用 UTC 统一）
        Instant timeUtc = Instant.parse("2026-01-10T12:00:00Z");

        // 上海附近
        double lat = 31.2304;
        double lon = 121.4737;

        List<StarPositionVO> list = skyService.listBrightStarPositions("v2026_01", timeUtc, lat, lon, 50);

        assertNotNull(list);
        assertTrue(list.size() >= 1);

        // 打印前几条看看
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            StarPositionVO s = list.get(i);
            System.out.println(s.getCatalogCode() + " " + s.getName()
                + " alt=" + s.getAltitudeDeg()
                + " az=" + s.getAzimuthDeg()
                + " visible=" + s.getVisible());
            assertNotNull(s.getAltitudeDeg());
            assertNotNull(s.getAzimuthDeg());
        }
    }
}
