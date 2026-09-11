package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.SeasonalValuedMetric;
import com.football_club.Scouting.repository.SeasonalValuedMetricRepository;
import com.football_club.Scouting.service.ISeasonalValuedMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SeasonalValuedMetricService implements ISeasonalValuedMetricService {

    private final SeasonalValuedMetricRepository metricRepository;

    @Override
    @Transactional
    public SeasonalValuedMetric createSeasonalValuedMetric(SeasonalValuedMetric metric) {
        return metricRepository.save(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonalValuedMetric getSeasonalValuedMetricById(Long id) {
        return metricRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Metrika sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalValuedMetric> getAllSeasonalValuedMetrics() {
        return metricRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteSeasonalValuedMetric(Long id) {
        if (!metricRepository.existsById(id)) {
            throw new NoSuchElementException("Metrika sa ID-em " + id + " ne postoji.");
        }
        metricRepository.deleteById(id);
    }
}