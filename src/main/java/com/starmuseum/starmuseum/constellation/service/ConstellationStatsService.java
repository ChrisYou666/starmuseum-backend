package com.starmuseum.starmuseum.constellation.service;

import com.starmuseum.starmuseum.constellation.dto.ConstellationOptionResponse;

import java.util.List;

public interface ConstellationStatsService {

    List<ConstellationOptionResponse> listOptions();
}
