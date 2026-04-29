package com.locus.service.impl;

import com.locus.dao.PropertyDAO;
import com.locus.dao.ValuationDAO;
import com.locus.ml.LinearRegressionPredictor;
import com.locus.model.Property;
import com.locus.model.Valuation;
import com.locus.service.ValuationService;
import com.locus.service.validation.InputValidator;

import java.util.List;

/**
 * Real implementation of {@link ValuationService} (UC-1).
 *
 * <p>Uses the trained Ridge regression model via {@link LinearRegressionPredictor}
 * to estimate Fair Market Value (FMV), computes confidence intervals,
 * finds comparable properties, and persists valuations.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class ValuationServiceImpl implements ValuationService {

    private final PropertyDAO propertyDAO;
    private final ValuationDAO valuationDAO;
    private final LinearRegressionPredictor predictor;

    public ValuationServiceImpl(PropertyDAO propertyDAO, ValuationDAO valuationDAO,
                                 LinearRegressionPredictor predictor) {
        this.propertyDAO = propertyDAO;
        this.valuationDAO = valuationDAO;
        this.predictor = predictor;
    }

    @Override
    public Valuation estimateFMV(Property property) {

        // ── Validation ──────────────────────────────
        new InputValidator()
                .validateNotNull("property", property)
                .throwIfInvalid();

        new InputValidator()
                .validateCity(property.getCity())
                .validateNotBlank("locality", property.getLocality())
                .validatePropertyType(property.getPropertyType())
                .validatePositive("area", property.getArea())
                .throwIfInvalid();

        // ── Check locality support ──────────────────
        if (!predictor.isLocalitySupported(property.getLocality())) {
            System.out.println("[ValuationService] Warning: locality '" +
                    property.getLocality() + "' not in training data. Using default encoding.");
        }

        // ── ML Prediction ───────────────────────────
        double fmv = predictor.predict(property);

        // ── Confidence Interval ─────────────────────
        double[] ci = predictor.getConfidenceInterval(fmv);

        // ── Key Factors ─────────────────────────────
        List<String> keyFactors = predictor.getKeyFactors(property);

        // ── Build Valuation ─────────────────────────
        Valuation valuation = new Valuation();
        valuation.setPropertyId(property.getPropertyId());
        valuation.setEstimatedFmv(Math.round(fmv));
        valuation.setConfidenceIntervalLow(Math.round(ci[0]));
        valuation.setConfidenceIntervalHigh(Math.round(ci[1]));
        valuation.setKeyFactors(keyFactors);

        // ── Persist ─────────────────────────────────
        try {
            valuationDAO.insert(valuation);
        } catch (Exception e) {
            System.err.println("[ValuationService] Warning: could not persist valuation: " + e.getMessage());
        }

        return valuation;
    }

    @Override
    public List<Property> findComparables(Property property) {

        new InputValidator()
                .validateNotNull("property", property)
                .validateNotBlank("city", property.getCity())
                .validateNotBlank("locality", property.getLocality())
                .validatePositive("area", property.getArea())
                .throwIfInvalid();

        return propertyDAO.findComparables(
                property.getCity(),
                property.getLocality(),
                property.getPropertyType(),
                property.getArea()
        );
    }
}
