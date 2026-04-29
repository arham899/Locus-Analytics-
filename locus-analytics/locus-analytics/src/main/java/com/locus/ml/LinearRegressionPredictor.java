package com.locus.ml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.model.Property;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java-side ML predictor that loads a trained Ridge regression model from JSON
 * and performs Fair Market Value (FMV) predictions at runtime.
 *
 * <p>The model JSON is produced by {@code ml/train_model.py} and contains:
 * intercept, coefficients, locality encodings, feature order, and residual std.</p>
 *
 * <p>Thread-safe — loaded once at startup, all methods are read-only.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public class LinearRegressionPredictor {

    private double intercept;
    private Map<String, Double> coefficients;
    private Map<String, Double> localityEncoding;
    private List<String> featureOrder;
    private double residualStd;
    private boolean logTransformed;

    /**
     * Loads the model from a JSON file path.
     *
     * @param modelPath path to model.json (relative or absolute)
     */
    public LinearRegressionPredictor(String modelPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root;

            // Try as file first, then classpath
            File file = new File(modelPath);
            if (file.exists()) {
                root = mapper.readTree(file);
            } else {
                InputStream is = getClass().getClassLoader().getResourceAsStream(modelPath);
                if (is == null) {
                    throw new RuntimeException("Model file not found: " + modelPath);
                }
                root = mapper.readTree(is);
            }

            this.intercept = root.get("intercept").asDouble();
            this.logTransformed = root.get("log_transformed").asBoolean();
            this.residualStd = root.get("training_residual_std").asDouble();

            // Load coefficients
            this.coefficients = new LinkedHashMap<>();
            JsonNode coeffNode = root.get("coefficients");
            coeffNode.fieldNames().forEachRemaining(name ->
                    coefficients.put(name, coeffNode.get(name).asDouble())
            );

            // Load locality encoding
            this.localityEncoding = new LinkedHashMap<>();
            JsonNode locNode = root.get("locality_encoding");
            locNode.fieldNames().forEachRemaining(name ->
                    localityEncoding.put(name, locNode.get(name).asDouble())
            );

            // Load feature order
            this.featureOrder = new ArrayList<>();
            root.get("feature_order").forEach(node -> featureOrder.add(node.asText()));

            System.out.println("[Predictor] Model loaded: " + featureOrder.size() +
                    " features, " + localityEncoding.size() + " localities");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load ML model from: " + modelPath, e);
        }
    }

    /**
     * Predicts the Fair Market Value (FMV) of a property.
     *
     * @param property the property to estimate
     * @return predicted FMV in PKR
     */
    public double predict(Property property) {
        double[] features = encodeFeatures(property);

        // Dot product: intercept + sum(coefficient * feature)
        double prediction = intercept;
        for (int i = 0; i < featureOrder.size(); i++) {
            String featureName = featureOrder.get(i);
            double coeff = coefficients.getOrDefault(featureName, 0.0);
            prediction += coeff * features[i];
        }

        // Reverse log transform
        if (logTransformed) {
            prediction = Math.exp(prediction);
        }

        return prediction;
    }

    /**
     * Returns the confidence interval for a prediction.
     *
     * @param prediction the predicted FMV
     * @return double array [lower, upper] representing 95% confidence interval
     */
    public double[] getConfidenceInterval(double prediction) {
        if (logTransformed) {
            double logPred = Math.log(prediction);
            double lower = Math.exp(logPred - 1.96 * residualStd);
            double upper = Math.exp(logPred + 1.96 * residualStd);
            return new double[]{lower, upper};
        } else {
            double margin = 1.96 * residualStd;
            return new double[]{prediction - margin, prediction + margin};
        }
    }

    /**
     * Returns the top 3 key factors influencing the prediction.
     *
     * @param property the property being estimated
     * @return list of human-readable factor descriptions
     */
    public List<String> getKeyFactors(Property property) {
        double[] features = encodeFeatures(property);

        // Compute |coefficient × feature value| for each feature
        Map<String, Double> impact = new LinkedHashMap<>();
        for (int i = 0; i < featureOrder.size(); i++) {
            String name = featureOrder.get(i);
            double coeff = coefficients.getOrDefault(name, 0.0);
            double value = features[i];
            impact.put(name, Math.abs(coeff * value));
        }

        // Sort by impact descending, take top 3
        return impact.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(e -> formatFactor(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a locality is supported by the model.
     */
    public boolean isLocalitySupported(String locality) {
        return localityEncoding.containsKey(locality);
    }

    /**
     * Returns the default locality encoding value (mean of all known localities).
     */
    public double getDefaultLocalityEncoding() {
        return localityEncoding.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(16.8);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private double[] encodeFeatures(Property property) {
        double[] features = new double[featureOrder.size()];

        for (int i = 0; i < featureOrder.size(); i++) {
            String name = featureOrder.get(i);
            features[i] = switch (name) {
                case "area" -> property.getArea();
                case "bedrooms" -> property.getBedrooms();
                case "bathrooms" -> property.getBathrooms();
                case "locality_encoded" -> localityEncoding.getOrDefault(
                        property.getLocality(), getDefaultLocalityEncoding()
                );
                case "type_apartment" -> "apartment".equalsIgnoreCase(property.getPropertyType()) ? 1.0 : 0.0;
                case "type_commercial" -> "commercial".equalsIgnoreCase(property.getPropertyType()) ? 1.0 : 0.0;
                case "type_house" -> "house".equalsIgnoreCase(property.getPropertyType()) ? 1.0 : 0.0;
                case "type_plot" -> "plot".equalsIgnoreCase(property.getPropertyType()) ? 1.0 : 0.0;
                default -> 0.0;
            };
        }

        return features;
    }

    private String formatFactor(String featureName, double impact) {
        String readable = switch (featureName) {
            case "area" -> "Property Area (sq.ft.)";
            case "bedrooms" -> "Number of Bedrooms";
            case "bathrooms" -> "Number of Bathrooms";
            case "locality_encoded" -> "Locality Premium";
            case "type_apartment" -> "Apartment Type";
            case "type_commercial" -> "Commercial Type";
            case "type_house" -> "House Type";
            case "type_plot" -> "Plot Type";
            default -> featureName;
        };
        return String.format("%s (impact: %.2f)", readable, impact);
    }
}
