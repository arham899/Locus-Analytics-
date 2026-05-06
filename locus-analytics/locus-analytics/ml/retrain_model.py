"""
LOCUS Analytics — Model Retraining Pipeline
Author: Arham Manzoor (24i-0640)

Called automatically by the ETL pipeline after each data load.
Retrains the Ridge regression model, compares against the previous
model's metrics, and overwrites model.json only if the new model is
at least as accurate.

Logs old vs new metrics to metrics_history.json for audit purposes.

Usage:
    python retrain_model.py [--force]

    --force   overwrite model.json even if new model is worse
"""

import argparse
import json
import os
import sys
from datetime import date, datetime

import numpy as np
import pandas as pd
import psycopg2
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

# ─────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────
DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "locus_analytics"
DB_USER = "postgres"
DB_PASS = "password"

ML_DIR      = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH  = os.path.join(ML_DIR, "model.json")
METRICS_PATH = os.path.join(ML_DIR, "metrics_history.json")


# ─────────────────────────────────────────────
# DATA LOADING
# ─────────────────────────────────────────────

def load_data():
    conn = psycopg2.connect(
        host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
        user=DB_USER, password=DB_PASS
    )
    sql = """
        SELECT property_id, city, locality, property_type,
               area, price, bedrooms, bathrooms
        FROM property
        WHERE price > 0 AND area > 0
    """
    df = pd.read_sql(sql, conn)
    conn.close()
    print(f"[LOAD] {len(df)} rows loaded from PostgreSQL")
    return df


# ─────────────────────────────────────────────
# FEATURE ENGINEERING
# ─────────────────────────────────────────────

def engineer_features(df):
    df = df.copy()
    df["log_price"]  = np.log(df["price"])
    df["bedrooms"]   = df["bedrooms"].fillna(0)
    df["bathrooms"]  = df["bathrooms"].fillna(0)

    locality_means = df.groupby("locality")["log_price"].mean().to_dict()
    df["locality_encoded"] = df["locality"].map(locality_means)

    type_dummies = pd.get_dummies(df["property_type"], prefix="type")
    df = pd.concat([df, type_dummies], axis=1)

    feature_cols = ["area", "bedrooms", "bathrooms", "locality_encoded"]
    for col in type_dummies.columns:
        feature_cols.append(col)

    df = df.dropna(subset=feature_cols + ["log_price"])
    print(f"[CLEAN] {len(df)} rows after removing missing data")
    return df, feature_cols, locality_means


# ─────────────────────────────────────────────
# TRAINING
# ─────────────────────────────────────────────

def train_and_evaluate(df, feature_cols):
    X = df[feature_cols].values
    y = df["log_price"].values

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    model = Ridge(alpha=1.0)
    model.fit(X_train, y_train)

    y_pred_test  = model.predict(X_test)
    y_pred_train = model.predict(X_train)

    # Log-space metrics
    train_r2 = r2_score(y_train, y_pred_train)
    test_r2  = r2_score(y_test,  y_pred_test)

    # Real-price-space metrics
    y_test_real = np.exp(y_test)
    y_pred_real = np.exp(y_pred_test)
    mae  = float(mean_absolute_error(y_test_real, y_pred_real))
    rmse = float(np.sqrt(mean_squared_error(y_test_real, y_pred_real)))

    residual_std = float(np.std(y_test - y_pred_test))

    # Per-city metrics
    city_metrics = {}
    for city in df["city"].unique():
        mask = df["city"] == city
        if mask.sum() < 5:
            continue
        X_c = df.loc[mask, feature_cols].values
        y_c = df.loc[mask, "log_price"].values
        y_p = model.predict(X_c)
        city_metrics[city] = {
            "r2":   round(float(r2_score(y_c, y_p)), 4),
            "mae":  round(float(mean_absolute_error(np.exp(y_c), np.exp(y_p))), 0),
            "rmse": round(float(np.sqrt(mean_squared_error(np.exp(y_c), np.exp(y_p)))), 0),
        }

    metrics = {
        "train_r2":     round(train_r2, 4),
        "test_r2":      round(test_r2, 4),
        "mae":          round(mae, 0),
        "rmse":         round(rmse, 0),
        "residual_std": residual_std,
        "city_metrics": city_metrics,
        "trained_date": str(date.today()),
        "row_count":    len(df),
    }

    return model, metrics


# ─────────────────────────────────────────────
# EXPORT
# ─────────────────────────────────────────────

def export_model(model, feature_cols, locality_means, metrics):
    coefficients = {col: float(model.coef_[i]) for i, col in enumerate(feature_cols)}

    artifact = {
        "intercept":            float(model.intercept_),
        "coefficients":         coefficients,
        "locality_encoding":    {k: float(v) for k, v in locality_means.items()},
        "feature_order":        feature_cols,
        "training_residual_std": metrics["residual_std"],
        "log_transformed":      True,
        "trained_date":         metrics["trained_date"],
    }

    with open(MODEL_PATH, "w") as f:
        json.dump(artifact, f, indent=2)

    print(f"[EXPORT] model.json written ({len(feature_cols)} features, "
          f"{len(locality_means)} localities)")


# ─────────────────────────────────────────────
# METRICS HISTORY
# ─────────────────────────────────────────────

def load_previous_metrics():
    if not os.path.exists(METRICS_PATH):
        return None
    try:
        with open(METRICS_PATH) as f:
            history = json.load(f)
        return history[-1] if history else None
    except Exception:
        return None


def append_metrics(metrics):
    history = []
    if os.path.exists(METRICS_PATH):
        try:
            with open(METRICS_PATH) as f:
                history = json.load(f)
        except Exception:
            history = []

    metrics["saved_at"] = datetime.now().isoformat()
    history.append(metrics)

    with open(METRICS_PATH, "w") as f:
        json.dump(history, f, indent=2)

    print(f"[METRICS] Appended to metrics_history.json ({len(history)} runs total)")


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────

def print_comparison(prev, new):
    print("\n" + "=" * 55)
    print("  MODEL COMPARISON")
    print("=" * 55)
    print(f"  {'Metric':<20} {'Previous':>12} {'New':>12}  {'Diff':>8}")
    print("  " + "-" * 53)
    for key in ("test_r2", "mae", "rmse"):
        p_val = prev.get(key, "N/A")
        n_val = new.get(key, "N/A")
        if isinstance(p_val, float) and isinstance(n_val, float):
            delta = n_val - p_val
            arrow = "+" if (key == "test_r2" and delta > 0) or (key != "test_r2" and delta < 0) else "-"
            print(f"  {key:<20} {p_val:>12.4f} {n_val:>12.4f}  {arrow}{abs(delta):>7.4f}")
        else:
            print(f"  {key:<20} {'N/A':>12} {n_val:>12.4f}")
    print("=" * 55)

    print("\n  Per-city R2 breakdown:")
    for city, cm in new.get("city_metrics", {}).items():
        prev_r2 = prev.get("city_metrics", {}).get(city, {}).get("r2", None)
        tag = f"(prev {prev_r2:.4f})" if prev_r2 is not None else "(new city)"
        print(f"    {city:<20} R2={cm['r2']:.4f}  {tag}")
    print()


def main():
    parser = argparse.ArgumentParser(description="LOCUS model retraining pipeline")
    parser.add_argument("--force", action="store_true",
                        help="Overwrite model.json even if new metrics are worse")
    args = parser.parse_args()

    print("\n" + "=" * 45)
    print("  LOCUS Analytics — Model Retraining")
    print("=" * 45 + "\n")

    df = load_data()
    if len(df) < 50:
        print("[ERROR] Insufficient data (< 50 rows). Aborting retraining.")
        sys.exit(1)

    df, feature_cols, locality_means = engineer_features(df)
    model, new_metrics = train_and_evaluate(df, feature_cols)

    prev_metrics = load_previous_metrics()

    if prev_metrics:
        print_comparison(prev_metrics, new_metrics)

        prev_r2 = prev_metrics.get("test_r2", 0.0)
        new_r2  = new_metrics["test_r2"]

        if not args.force and new_r2 < prev_r2 - 0.01:
            print(f"[SKIP] New model R²={new_r2:.4f} is worse than previous R²={prev_r2:.4f}.")
            print("       Pass --force to overwrite anyway.")
            append_metrics({**new_metrics, "saved_model": False})
            sys.exit(0)
    else:
        print("[INFO] No previous model found — writing initial model.")
        print(f"       Test R²={new_metrics['test_r2']:.4f}  "
              f"MAE=PKR {new_metrics['mae']:,.0f}  "
              f"RMSE=PKR {new_metrics['rmse']:,.0f}")

    export_model(model, feature_cols, locality_means, new_metrics)
    append_metrics({**new_metrics, "saved_model": True})

    print("\n[OK] Retraining complete. Java app will hot-reload model.json on next prediction.\n")


if __name__ == "__main__":
    main()
