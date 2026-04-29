"""
LOCUS Analytics — ML Training Pipeline
Author: Arham Manzoor (24i-0640)

Trains a Ridge regression model on PostgreSQL property data and exports
a JSON artifact that the Java predictor (LinearRegressionPredictor) loads at runtime.

Usage:
    pip install -r requirements.txt
    python train_model.py
"""

import json
import os
import sys
from datetime import date

import numpy as np
import pandas as pd
import psycopg2
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

# ─────────────────────────────────────────────
# CONFIG — match config.properties
# ─────────────────────────────────────────────
DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "locus_analytics"
DB_USER = "postgres"
DB_PASS = "password"

OUTPUT_PATH = os.path.join(os.path.dirname(__file__), "model.json")


def load_data():
    """Loads property data from PostgreSQL."""
    conn = psycopg2.connect(
        host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
        user=DB_USER, password=DB_PASS
    )
    sql = """
        SELECT property_id, city, locality, property_type,
               area, price, bedrooms, bathrooms,
               latitude, longitude
        FROM property
        WHERE price > 0 AND area > 0
    """
    df = pd.read_sql(sql, conn)
    conn.close()
    print(f"[LOAD] {len(df)} rows loaded from PostgreSQL")
    return df


def engineer_features(df):
    """
    Feature engineering:
    - Numeric: area, bedrooms, bathrooms
    - One-hot: property_type (house, apartment, plot, commercial)
    - Target encoding: locality → mean log-price per locality
    """

    # Log-transform target (prices are log-normal)
    df["log_price"] = np.log(df["price"])

    # ── Fill missing numeric fields ──────────────
    df["bedrooms"] = df["bedrooms"].fillna(0)
    df["bathrooms"] = df["bathrooms"].fillna(0)

    # ── Target encoding: locality ────────────────
    locality_means = df.groupby("locality")["log_price"].mean().to_dict()
    df["locality_encoded"] = df["locality"].map(locality_means)

    # ── One-hot: property_type ───────────────────
    type_dummies = pd.get_dummies(df["property_type"], prefix="type")
    df = pd.concat([df, type_dummies], axis=1)

    # ── Feature columns ──────────────────────────
    feature_cols = ["area", "bedrooms", "bathrooms", "locality_encoded"]
    for col in type_dummies.columns:
        feature_cols.append(col)

    # ── Drop rows with any remaining NaN in features ──
    df = df.dropna(subset=feature_cols + ["log_price"])
    print(f"[CLEAN] {len(df)} rows after removing missing data")

    return df, feature_cols, locality_means


def train_model(df, feature_cols):
    """Trains a Ridge regression model and returns metrics."""

    X = df[feature_cols].values
    y = df["log_price"].values

    # Stratified-ish split by city
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    model = Ridge(alpha=1.0)
    model.fit(X_train, y_train)

    # ── Predictions ──────────────────────────────
    y_pred_train = model.predict(X_train)
    y_pred_test = model.predict(X_test)

    # ── Metrics (in log space) ───────────────────
    train_r2 = r2_score(y_train, y_pred_train)
    test_r2 = r2_score(y_test, y_pred_test)

    # ── Metrics (in real price space) ────────────
    y_test_real = np.exp(y_test)
    y_pred_real = np.exp(y_pred_test)

    mae = mean_absolute_error(y_test_real, y_pred_real)
    rmse = np.sqrt(mean_squared_error(y_test_real, y_pred_real))

    # ── Residual std (for confidence intervals) ──
    residuals = y_test - y_pred_test
    residual_std = float(np.std(residuals))

    print("\n" + "="*50)
    print("  TRAINING RESULTS")
    print("="*50)
    print(f"  Train R2:        {train_r2:.4f}")
    print(f"  Test R2:         {test_r2:.4f}")
    print(f"  Test MAE:        PKR {mae:,.0f}")
    print(f"  Test RMSE:       PKR {rmse:,.0f}")
    print(f"  Residual Std:    {residual_std:.4f}")
    print("="*50 + "\n")

    return model, residual_std


def export_model(model, feature_cols, locality_means, residual_std):
    """Exports model as JSON for the Java predictor."""

    coefficients = {}
    for i, col in enumerate(feature_cols):
        coefficients[col] = float(model.coef_[i])

    artifact = {
        "intercept": float(model.intercept_),
        "coefficients": coefficients,
        "locality_encoding": {k: float(v) for k, v in locality_means.items()},
        "feature_order": feature_cols,
        "training_residual_std": residual_std,
        "log_transformed": True,
        "trained_date": str(date.today())
    }

    with open(OUTPUT_PATH, "w") as f:
        json.dump(artifact, f, indent=2)

    print(f"[EXPORT] Model saved to {OUTPUT_PATH}")
    print(f"         Intercept: {artifact['intercept']:.4f}")
    print(f"         Features:  {len(feature_cols)}")
    print(f"         Localities: {len(locality_means)}")


def main():
    print("\n" + "="*40)
    print("  LOCUS Analytics - Model Trainer")
    print("="*40 + "\n")

    # 1. Load
    df = load_data()

    if len(df) < 10:
        print("[ERROR] Not enough data to train. Run seed data first.")
        sys.exit(1)

    # 2. Engineer features
    df, feature_cols, locality_means = engineer_features(df)

    # 3. Train
    model, residual_std = train_model(df, feature_cols)

    # 4. Export
    export_model(model, feature_cols, locality_means, residual_std)

    print("\n[OK] Training complete. Java app can now load ml/model.json\n")


if __name__ == "__main__":
    main()
