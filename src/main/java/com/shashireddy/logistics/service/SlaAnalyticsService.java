package com.shashireddy.logistics.service;

import com.shashireddy.logistics.dto.ShipmentDtos.SlaStatsResponse;
import com.shashireddy.logistics.model.CarrierSlaStats;
import com.shashireddy.logistics.repository.CarrierSlaStatsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SlaAnalyticsService {

    private final CarrierSlaStatsRepository statsRepository;

    public SlaAnalyticsService(CarrierSlaStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<SlaStatsResponse> listAll() {
        return statsRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SlaStatsResponse getForCarrier(String carrier) {
        CarrierSlaStats stats = statsRepository.findById(carrier)
                .orElseThrow(() -> new NoSuchElementException("No SLA stats for carrier " + carrier));
        return toResponse(stats);
    }

    private SlaStatsResponse toResponse(CarrierSlaStats stats) {
        return new SlaStatsResponse(
                stats.getCarrier(),
                stats.getDeliveredCount(),
                stats.getOnTimeCount(),
                stats.getLateCount(),
                stats.getOnTimeRate());
    }
}
