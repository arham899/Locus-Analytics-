package com.locus.dao;

import com.locus.model.Valuation;

import java.time.LocalDateTime;
import java.util.List;

public interface ValuationDAO {

    Valuation findByPropertyId(String propertyId);

    boolean insert(Valuation valuation);

    List<Valuation> findByDateRange(LocalDateTime start, LocalDateTime end);
}