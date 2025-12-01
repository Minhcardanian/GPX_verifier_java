package org.trail.attemptverifier.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.trail.attemptverifier.model.Attempt;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class AttemptRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttemptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ------------------------------------------------------------
    // RowMapper for DB → Model
    // ------------------------------------------------------------
    private static class AttemptRowMapper implements RowMapper<Attempt> {
        @Override
        public Attempt mapRow(ResultSet rs, int rowNum) throws SQLException {
            Attempt attempt = new Attempt();
            attempt.setId(rs.getLong("id"));
            attempt.setRunnerId(rs.getString("runner_id"));

            Timestamp ts = rs.getTimestamp("timestamp");
            attempt.setAttemptTime(ts != null ? ts.toLocalDateTime() : null);

            attempt.setDistanceKm(rs.getDouble("distance_km"));
            attempt.setElevationGainM(rs.getDouble("elevation_gain_m"));
            attempt.setDifficultyScore(rs.getDouble("difficulty_score"));
            attempt.setResult(rs.getString("result"));
            attempt.setMessage(rs.getString("message"));

            // Route similarity metrics
            attempt.setCoverageRatio(rs.getObject("coverage_ratio", Double.class));
            attempt.setMaxDeviationM(rs.getObject("max_deviation_m", Double.class));

            // Raw GPX blob
            byte[] gpxBytes = rs.getBytes("gpx_data");
            attempt.setGpxData(gpxBytes != null ? gpxBytes : null);

            return attempt;
        }
    }

    // ------------------------------------------------------------
    // INSERT Attempt
    // ------------------------------------------------------------
    public Attempt save(Attempt attempt) {

        String sql = """
            INSERT INTO attempts
            (runner_id, timestamp, distance_km, elevation_gain_m,
             difficulty_score, result, message,
             coverage_ratio, max_deviation_m, gpx_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, attempt.getRunnerId());

            ps.setTimestamp(2,
                    attempt.getAttemptTime() != null
                            ? Timestamp.valueOf(attempt.getAttemptTime())
                            : null);

            ps.setDouble(3, attempt.getDistanceKm());
            ps.setDouble(4, attempt.getElevationGainM());
            ps.setDouble(5, attempt.getDifficultyScore());
            ps.setString(6, attempt.getResult());
            ps.setString(7, attempt.getMessage());

            // coverage + deviation
            if (attempt.getCoverageRatio() != null)
                ps.setDouble(8, attempt.getCoverageRatio());
            else
                ps.setNull(8, Types.DOUBLE);

            if (attempt.getMaxDeviationM() != null)
                ps.setDouble(9, attempt.getMaxDeviationM());
            else
                ps.setNull(9, Types.DOUBLE);

            // GPX bytes
            if (attempt.getGpxData() != null)
                ps.setBytes(10, attempt.getGpxData());
            else
                ps.setNull(10, Types.BLOB);

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            attempt.setId(key.longValue());
        }

        return attempt;
    }

    // ------------------------------------------------------------
    // BASIC QUERIES
    // ------------------------------------------------------------
    public List<Attempt> findAll() {
        String sql = "SELECT * FROM attempts ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, new AttemptRowMapper());
    }

    public Optional<Attempt> findById(Long id) {
        String sql = "SELECT * FROM attempts WHERE id = ?";
        List<Attempt> list = jdbcTemplate.query(sql, new AttemptRowMapper(), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // ------------------------------------------------------------
    // FILTERS
    // ------------------------------------------------------------
    public List<Attempt> findByRunnerId(String runnerId) {
        String sql = """
            SELECT * FROM attempts
            WHERE runner_id = ?
            ORDER BY timestamp DESC
            """;
        return jdbcTemplate.query(sql, new AttemptRowMapper(), runnerId);
    }

    public List<Attempt> findByResult(String result) {
        String sql = """
            SELECT * FROM attempts
            WHERE result = ?
            ORDER BY timestamp DESC
            """;
        return jdbcTemplate.query(sql, new AttemptRowMapper(), result);
    }

    public List<Attempt> findByRunnerIdAndResult(String runnerId, String result) {
        String sql = """
            SELECT * FROM attempts
            WHERE runner_id = ? AND result = ?
            ORDER BY timestamp DESC
            """;
        return jdbcTemplate.query(sql, new AttemptRowMapper(), runnerId, result);
    }

    // ------------------------------------------------------------
    // RESET (used by /api/attempts/reset)
    // ------------------------------------------------------------
    /**
     * Deletes all attempts and resets AUTO_INCREMENT.
     *
     * @return number of rows deleted
     */
    public int resetAll() {
        // Delete rows and capture how many were removed
        int deleted = jdbcTemplate.update("DELETE FROM attempts");
        // Reset auto-increment so IDs start from 1 again
        jdbcTemplate.update("ALTER TABLE attempts AUTO_INCREMENT = 1");
        return deleted;
    }
}
