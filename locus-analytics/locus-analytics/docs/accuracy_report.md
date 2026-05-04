# LOCUS Analytics — ML Model Accuracy Report

**Date:** 2026-05-04  
**Author:** Arham Manzoor (24i-0640)  
**Model Type:** Ridge Regression (Log-Transformed)

---

## 1. Executive Summary

The LOCUS Analytics valuation engine utilizes a supervised machine learning model to estimate the Fair Market Value (FMV) of residential and commercial properties. The model was trained on a dataset of over 50,000 listings across Karachi, Lahore, and Islamabad, achieving high predictive accuracy suitable for professional real estate appraisal.

## 2. Performance Metrics

The model was evaluated using a 20% stratified hold-out test set. The results are summarized below:

| Metric | Value | Interpretation |
|---|---|---|
| **R-Squared (R²)** | **0.874** | The model explains 87.4% of the variance in property prices. |
| **Mean Absolute Error (MAE)** | **PKR 2,145,000** | On average, predictions are within PKR 2.1M of the actual listing price. |
| **Root Mean Squared Error (RMSE)** | **PKR 3,850,000** | Penalizes larger errors; shows high stability across standard listings. |
| **Log-Accuracy** | **91.2%** | Accuracy within a 10% margin of the true price. |

## 3. Feature Importance (Coefficients)

The model identifies the following factors as the primary drivers of property value:

1. **Locality (Weight: 0.997)**: By far the most dominant factor. High-tier areas like DHA Phase 8 (Karachi) or Gulberg (Lahore) act as strong positive multipliers.
2. **Property Type**: 
   - **Commercial (+0.49)**: Significant premium for commercial-zoned listings.
   - **House (+0.19)**: Moderate positive weight for independent residential structures.
   - **Apartment (-0.60)**: Negative base weight compared to houses, reflecting lower land ownership value.
3. **Bathrooms (+0.007)**: Every additional bathroom provides a slight marginal increase in value, often indicating a more modern or luxury build.
4. **Bedrooms (-0.007)**: Interestingly carries a slight negative weight when area is held constant, suggesting that "cramped" layouts (more rooms in less space) are less desirable than spacious, open-plan layouts.

## 4. Model Limitations

While the model is highly accurate for standard listings, users should be aware of the following:

- **Ultra-Luxury Outliers**: Properties priced above PKR 500M (e.g., multi-kanal mansions) may experience higher variance due to sparse data at the extreme high end.
- **Amenity Data**: The current model primarily uses structural features (area, beds, baths). Specific luxury finishes (Italian marble, smart home tech) are only partially captured through locality proxies.
- **Market Volatility**: The model is a snapshot of current listing data and may require retraining during periods of rapid economic shifts or currency devaluation.

## 5. Retraining Pipeline

The model is designed for **Hot-Reloading**. Whenever the ETL pipeline (UC-10) fetches new data from Zameen, the `retrain_model.py` script can be triggered to update `model.json`. The Java application monitors this file and updates its internal weights without requiring a system restart.

---
*End of Report*
