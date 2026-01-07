package com.starmuseum.starmuseum.constellation.service.impl;

import com.starmuseum.starmuseum.constellation.dto.ConstellationOptionResponse;
import com.starmuseum.starmuseum.constellation.mapper.ConstellationStatsMapper;
import com.starmuseum.starmuseum.constellation.service.ConstellationStatsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConstellationStatsServiceImpl implements ConstellationStatsService {

    private final ConstellationStatsMapper constellationStatsMapper;

    public ConstellationStatsServiceImpl(ConstellationStatsMapper constellationStatsMapper) {
        this.constellationStatsMapper = constellationStatsMapper;
    }

    @Override
    public List<ConstellationOptionResponse> listOptions() {
        return constellationStatsMapper.selectConstellationOptions();
    }
}
