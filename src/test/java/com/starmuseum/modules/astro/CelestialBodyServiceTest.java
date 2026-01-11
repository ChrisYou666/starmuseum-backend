package com.starmuseum.modules.astro;

import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.service.CelestialBodyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CelestialBodyServiceTest {

    @Autowired
    private CelestialBodyService celestialBodyService;

    @Test
    void shouldQueryBrightStarsFromDb() {
        List<CelestialBody> list = celestialBodyService.listBrightStars("v2026_01", 50);

        assertNotNull(list);
        assertTrue(list.size() >= 1, "数据库里应该至少有 1 条亮星数据");
        // 你现在应该有 10 条
        System.out.println("count=" + list.size());
        list.forEach(s -> System.out.println(s.getCatalogCode() + " " + s.getName() + " mag=" + s.getMag()));
    }
}
