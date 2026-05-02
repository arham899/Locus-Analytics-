package com.locus.dao.impl;

import com.locus.config.DBConnection;
import com.locus.dao.PropertyDAO;
import com.locus.dao.DataAccessException;
import com.locus.model.Property;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.LocalityMetric;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.locus.model.dto.TrendPoint;

public class PropertyDAOImpl implements PropertyDAO {

    private final DataSource dataSource = DBConnection.getDataSource();

    // ─────────────────────────────────────────────
    // 1. FIND BY ID
    // ─────────────────────────────────────────────
    @Override
    public Optional<Property> findById(String id) {

        String sql = "SELECT * FROM property WHERE property_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("FindById failed: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ─────────────────────────────────────────────
    // 2. SEARCH (PRODUCTION SAFE VERSION)
    // ─────────────────────────────────────────────
    @Override
    public List<Property> search(SearchFilter filter) {

        List<Property> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            // =========================
            // 1. BUILD FILTERS (shared)
            // =========================
            QueryParts qp = buildWhereClause(filter);
            StringBuilder whereSql = new StringBuilder(qp.whereSql);
            List<Object> filterParams = qp.params;

            if (filter.getCity() != null) {
                whereSql.append(" AND city = ?");
                filterParams.add(filter.getCity());
            }

            if (filter.getMinPrice() != null) {
                whereSql.append(" AND price >= ?");
                filterParams.add(filter.getMinPrice());
            }

            if (filter.getMaxPrice() != null) {
                whereSql.append(" AND price <= ?");
                filterParams.add(filter.getMaxPrice());
            }

            if (filter.getLocality() != null) {
                whereSql.append(" AND locality = ?");
                filterParams.add(filter.getLocality());
            }

            if (filter.getPropertyType() != null) {
                whereSql.append(" AND property_type = ?");
                filterParams.add(filter.getPropertyType());
            }

            if (filter.getBedrooms() != null) {
                whereSql.append(" AND bedrooms = ?");
                filterParams.add(filter.getBedrooms());
            }

            if (filter.getBathrooms() != null) {
                whereSql.append(" AND bathrooms = ?");
                filterParams.add(filter.getBathrooms());
            }

            // =========================
            // 2. COUNT QUERY
            // =========================
//            String countSql = "SELECT COUNT(*) FROM property" + whereSql;
//
//            int totalCount = 0;
//
//            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
//
//                for (int i = 0; i < filterParams.size(); i++) {
//                    countStmt.setObject(i + 1, filterParams.get(i));
//                }
//
//                ResultSet rs = countStmt.executeQuery();
//                if (rs.next()) {
//                    totalCount = rs.getInt(1);
//                }
//            }

            // =========================
            // 3. DATA QUERY
            // =========================
            StringBuilder dataSql = new StringBuilder("SELECT * FROM property");
            dataSql.append(whereSql);

            // safe sorting
            String sortBy = switch (filter.getSortBy() == null ? "price" : filter.getSortBy()) {
                case "price", "area", "listing_date" -> filter.getSortBy();
                default -> "price";
            };

            dataSql.append(" ORDER BY ").append(sortBy);

            if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
                dataSql.append(" DESC");
            } else {
                dataSql.append(" ASC");
            }

            dataSql.append(" LIMIT ? OFFSET ?");

            List<Object> queryParams = new ArrayList<>(filterParams);
            queryParams.add(filter.getPageSize());
            queryParams.add(filter.getOffset());

            try (PreparedStatement stmt = conn.prepareStatement(dataSql.toString())) {

                for (int i = 0; i < queryParams.size(); i++) {
                    stmt.setObject(i + 1, queryParams.get(i));
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }

            // (optional: you can return totalCount later in response object)

        } catch (SQLException e) {
            throw new DataAccessException("Search failed: " + e.getMessage(), e);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // 3. INSERT
    // ─────────────────────────────────────────────
    @Override
    public boolean insert(Property property) {

        String sql = """
            INSERT INTO property (
                property_id, city, locality, property_type,
                area, price, bedrooms, bathrooms,
                listing_date, latitude, longitude, url_hash
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, property.getPropertyId());
            stmt.setString(2, property.getCity());
            stmt.setString(3, property.getLocality());
            stmt.setString(4, property.getPropertyType());
            stmt.setDouble(5, property.getArea());
            stmt.setDouble(6, property.getPrice());
            stmt.setInt(7, property.getBedrooms());
            stmt.setInt(8, property.getBathrooms());

            if (property.getListingDate() != null) {
                stmt.setDate(9, Date.valueOf(property.getListingDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setDouble(10, property.getLatitude());
            stmt.setDouble(11, property.getLongitude());
            stmt.setString(12, property.getUrlHash());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Insert failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // 4. UPDATE
    // ─────────────────────────────────────────────
    @Override
    public boolean update(Property property) {

        String sql = """
            UPDATE property
            SET city = ?,
                locality = ?,
                property_type = ?,
                area = ?,
                price = ?,
                bedrooms = ?,
                bathrooms = ?,
                latitude = ?,
                longitude = ?,
                url_hash = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE property_id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, property.getCity());
            stmt.setString(2, property.getLocality());
            stmt.setString(3, property.getPropertyType());
            stmt.setDouble(4, property.getArea());
            stmt.setDouble(5, property.getPrice());
            stmt.setInt(6, property.getBedrooms());
            stmt.setInt(7, property.getBathrooms());
            stmt.setDouble(8, property.getLatitude());
            stmt.setDouble(9, property.getLongitude());
            stmt.setString(10, property.getUrlHash());

            stmt.setString(11, property.getPropertyId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // 5. DELETE
    // ─────────────────────────────────────────────
    @Override
    public boolean delete(String propertyId) {

        String sql = "DELETE FROM property WHERE property_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Delete failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // 6. FIND COMPARABLES (NOT IMPLEMENTED)
    // ─────────────────────────────────────────────
    @Override
    public List<Property> findComparables(String city, String locality, String propertyType, double area) {

        List<Property> results = new ArrayList<>();

        String sql = """
        SELECT *
        FROM property
        WHERE city = ?
          AND locality = ?
          AND property_type = ?
          AND area BETWEEN ? AND ?
        ORDER BY listing_date DESC
        LIMIT 5
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);
            stmt.setString(2, locality);
            stmt.setString(3, propertyType);
            stmt.setDouble(4, area * 0.8);
            stmt.setDouble(5, area * 1.2);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("findComparables failed: " + e.getMessage(), e);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // 7. FIND BY LOCALITY
    // ─────────────────────────────────────────────
    @Override
    public List<Property> findByLocality(String city, String locality) {

        List<Property> results = new ArrayList<>();

        String sql = """
        SELECT *
        FROM property
        WHERE city = ?
          AND locality = ?
        ORDER BY listing_date DESC
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);
            stmt.setString(2, locality);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("findByLocality failed: " + e.getMessage(), e);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // 8. TREND ANALYTICS
    // ─────────────────────────────────────────────
    @Override
    public List<TrendPoint> findAggregatedByMonth(
            String city,
            String locality,
            String propertyType,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {

        List<TrendPoint> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT 
            DATE_TRUNC('month', listing_date) AS period,
            AVG(price) AS average_price,
            COUNT(*) AS listing_count
        FROM property
        WHERE listing_date IS NOT NULL
    """);

        List<Object> params = new ArrayList<>();

        if (city != null) {
            sql.append(" AND city = ?");
            params.add(city);
        }

        if (locality != null) {
            sql.append(" AND locality = ?");
            params.add(locality);
        }

        if (propertyType != null) {
            sql.append(" AND property_type = ?");
            params.add(propertyType);
        }

        if (startDate != null) {
            sql.append(" AND listing_date >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND listing_date <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }

        sql.append("""
        GROUP BY period
        ORDER BY period
    """);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                TrendPoint tp = new TrendPoint();

                tp.setPeriod(rs.getTimestamp("period").toString());
                tp.setAveragePrice(rs.getDouble("average_price"));
                tp.setListingCount(rs.getLong("listing_count"));

                results.add(tp);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Aggregation failed: " + e.getMessage(), e);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────────
    private Property mapRow(ResultSet rs) throws SQLException {

        Property p = new Property();

        p.setPropertyId(rs.getString("property_id"));
        p.setCity(rs.getString("city"));
        p.setLocality(rs.getString("locality"));
        p.setPropertyType(rs.getString("property_type"));
        p.setArea(rs.getDouble("area"));
        p.setPrice(rs.getDouble("price"));
        p.setBedrooms(rs.getInt("bedrooms"));
        p.setBathrooms(rs.getInt("bathrooms"));
        p.setListingDate(rs.getDate("listing_date") != null
                ? rs.getDate("listing_date").toLocalDate()
                : null);

        p.setLatitude(rs.getDouble("latitude"));
        p.setLongitude(rs.getDouble("longitude"));
        p.setUrlHash(rs.getString("url_hash"));

        return p;
    }

    @Override
    public List<LocalityMetric> getLocalityMetrics(String city, int periodYears, int minListingCount) {

        List<LocalityMetric> results = new ArrayList<>();

        LocalDate now = LocalDate.now();
        LocalDate pastStart = now.minusYears(periodYears);
        LocalDate midPoint = now.minusYears(periodYears / 2);
        String sql = """
WITH base AS (
    SELECT
        locality,
        price,
        listing_date,
        CASE
            WHEN listing_date >= CURRENT_DATE - (? * INTERVAL '1 year') / 2::int
                THEN 'recent'
            ELSE 'past'
        END AS period
    FROM property
    WHERE city = ?
      AND listing_date >= CURRENT_DATE - (? * INTERVAL '1 year')
),
agg AS (
    SELECT
        locality,
        period,
        AVG(price) AS avg_price,
        COUNT(*) AS cnt
    FROM base
    GROUP BY locality, period
)
SELECT
    locality,
    MAX(CASE WHEN period = 'past' THEN avg_price END) AS past_price,
    MAX(CASE WHEN period = 'recent' THEN avg_price END) AS recent_price,
    MAX(CASE WHEN period = 'past' THEN cnt END) AS past_count,
    MAX(CASE WHEN period = 'recent' THEN cnt END) AS recent_count
FROM agg
GROUP BY locality
HAVING
    COALESCE(MAX(CASE WHEN period = 'recent' THEN cnt END), 0) >= ?
""";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // ⚠️ IMPORTANT: PostgreSQL doesn't allow ? inside INTERVAL directly
            // So we pass numeric and build interval using multiplication

//            stmt.setString(1, city);
//
//// past
//            stmt.setDate(2, Date.valueOf(pastStart));
//            stmt.setDate(3, Date.valueOf(midPoint));
//
//// recent
//            stmt.setString(4, city);
//            stmt.setDate(5, Date.valueOf(midPoint));
//
//            stmt.setInt(6, minListingCount);

            stmt.setInt(1, periodYears);
            stmt.setString(2, city);
            stmt.setInt(3, periodYears);
            stmt.setInt(4, minListingCount);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                LocalityMetric m = new LocalityMetric();

                double pastPrice = rs.getDouble("past_price");
                double recentPrice = rs.getDouble("recent_price");

                long pastCount = rs.getLong("past_count");
                long recentCount = rs.getLong("recent_count");

                m.setLocality(rs.getString("locality"));
                m.setAvgPricePast(pastPrice);
                m.setAvgPriceRecent(recentPrice);
                m.setPastCount(pastCount);
                m.setRecentCount(recentCount);

                // compute in Java (cleaner + safer)
                double priceAppreciation =
                        (pastPrice == 0)
                                ? 0.0
                                : ((recentPrice - pastPrice) / pastPrice) * 100;
                double volumeGrowth;

                if (pastCount == 0) {
                    volumeGrowth = (recentCount == 0) ? 0.0 : 100.0;
                } else {
                    volumeGrowth = ((double)(recentCount - pastCount) / pastCount) * 100;
                }

                m.setPriceAppreciation(priceAppreciation);
                m.setVolumeGrowth(volumeGrowth);

                results.add(m);
            }

        } catch (SQLException e) {
            throw new DataAccessException("getLocalityMetrics failed: " + e.getMessage(), e);
        }

        return results;
    }

    private static class QueryParts {
        String whereSql;
        List<Object> params;
    }

    private QueryParts buildWhereClause(SearchFilter filter) {

        StringBuilder whereSql = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.getCity() != null) {
            whereSql.append(" AND city = ?");
            params.add(filter.getCity());
        }

        if (filter.getMinPrice() != null) {
            whereSql.append(" AND price >= ?");
            params.add(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            whereSql.append(" AND price <= ?");
            params.add(filter.getMaxPrice());
        }

        if (filter.getLocality() != null) {
            whereSql.append(" AND locality = ?");
            params.add(filter.getLocality());
        }

        if (filter.getPropertyType() != null) {
            whereSql.append(" AND property_type = ?");
            params.add(filter.getPropertyType());
        }

        if (filter.getBedrooms() != null) {
            whereSql.append(" AND bedrooms = ?");
            params.add(filter.getBedrooms());
        }

        if (filter.getBathrooms() != null) {
            whereSql.append(" AND bathrooms = ?");
            params.add(filter.getBathrooms());
        }

        QueryParts qp = new QueryParts();
        qp.whereSql = whereSql.toString();
        qp.params = params;

        return qp;
    }

    @Override
    public int countByFilter(SearchFilter filter) {

        try (Connection conn = dataSource.getConnection()) {

            QueryParts qp = buildWhereClause(filter);

            String sql = "SELECT COUNT(*) FROM property" + qp.whereSql;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < qp.params.size(); i++) {
                    stmt.setObject(i + 1, qp.params.get(i));
                }

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("countByFilter failed: " + e.getMessage(), e);
        }

        return 0;
    }

}