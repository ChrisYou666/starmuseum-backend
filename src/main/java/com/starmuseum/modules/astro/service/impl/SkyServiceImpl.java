package com.starmuseum.modules.astro.service.impl;

import com.starmuseum.modules.astro.util.AstronomyCalculator;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.service.CelestialBodyService;
import com.starmuseum.modules.astro.service.SkyService;
import com.starmuseum.modules.astro.vo.BodyDetailVO;
import com.starmuseum.modules.astro.vo.StarPositionVO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SkyServiceImpl implements SkyService {

    private final CelestialBodyService celestialBodyService;

    public SkyServiceImpl(CelestialBodyService celestialBodyService) {
        this.celestialBodyService = celestialBodyService;
    }

    @Override
    public List<StarPositionVO> listBrightStarPositions(String catalogVersionCode,
                                                        Instant timeUtc,
                                                        double latDeg,
                                                        double lonDeg,
                                                        int limit) {

        List<CelestialBody> stars = celestialBodyService.listBrightStars(catalogVersionCode, limit);

        List<StarPositionVO> out = new ArrayList<>(stars.size());
        for (CelestialBody s : stars) {
            if (s.getRaDeg() == null || s.getDecDeg() == null) {
                continue;
            }

            AstronomyCalculator.AltAz altAz = AstronomyCalculator.equatorialToHorizontal(
                timeUtc, latDeg, lonDeg, s.getRaDeg(), s.getDecDeg()
            );

            StarPositionVO vo = new StarPositionVO();
            vo.setId(s.getId());
            vo.setCatalogCode(s.getCatalogCode());
            vo.setName(s.getName());
            vo.setMag(s.getMag());
            vo.setRaDeg(s.getRaDeg());
            vo.setDecDeg(s.getDecDeg());
            vo.setAltitudeDeg(altAz.altitudeDeg());
            vo.setAzimuthDeg(altAz.azimuthDeg());
            vo.setVisible(altAz.visible());

            out.add(vo);
        }
        return out;
    }

    @Override
    public BodyDetailVO getBodyDetail(String catalogVersionCode,
                                      long id,
                                      Instant timeUtc,
                                      double latDeg,
                                      double lonDeg) {

        CelestialBody b = celestialBodyService.getById(id);
        if (b == null) {
            return null;
        }

        if (b.getCatalogVersionCode() != null && !b.getCatalogVersionCode().equals(catalogVersionCode)) {
            return null;
        }

        BodyDetailVO vo = new BodyDetailVO();
        vo.setId(b.getId());
        vo.setCatalogCode(b.getCatalogCode());
        vo.setBodyType(b.getBodyType());

        vo.setName(b.getName());
        vo.setNameZh(b.getNameZh());
        vo.setNameEn(b.getNameEn());
        vo.setNameId(b.getNameId());

        vo.setRaDeg(b.getRaDeg());
        vo.setDecDeg(b.getDecDeg());
        vo.setMag(b.getMag());

        vo.setSpectralType(b.getSpectralType());
        vo.setConstellation(b.getConstellation());
        vo.setWikiUrl(b.getWikiUrl());

        // Phase 5D：扩展字段输出
        vo.setExtraJson(b.getExtraJson());

        if (b.getRaDeg() != null && b.getDecDeg() != null) {
            AstronomyCalculator.AltAz altAz = AstronomyCalculator.equatorialToHorizontal(
                timeUtc, latDeg, lonDeg, b.getRaDeg(), b.getDecDeg()
            );
            vo.setAltitudeDeg(altAz.altitudeDeg());
            vo.setAzimuthDeg(altAz.azimuthDeg());
            vo.setVisible(altAz.visible());
        } else {
            vo.setAltitudeDeg(null);
            vo.setAzimuthDeg(null);
            vo.setVisible(false);
        }

        return vo;
    }
}
