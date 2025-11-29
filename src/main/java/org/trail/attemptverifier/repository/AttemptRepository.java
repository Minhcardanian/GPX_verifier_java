package org.trail.attemptverifier.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.trail.attemptverifier.model.Attempt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AttemptRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttemptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
            return attempt;
        }
    }

    public Attempt save(Attempt attempt) {
        String sql = """
            INSERT INTO attempts
              (runner_id, timestamp, distance_km, elevation_gain_m,
               difficulty_score, result, message)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, attempt.getRunnerId());
            ps.setTimestamp(2, attempt.getAttemptTime() != null ? Timestamp.valueOf(attempt.getAttemptTime()) : null);
            ps.setDouble(3, attempt.getDistanceKm());
            ps.setDouble(4, attempt.getElevationGainM());
            ps.setDouble(5, attempt.getDifficultyScore());
            ps.setString(6, attempt.getResult());
            ps.setString(7, attempt.getMessage());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            attempt.setId(key.longValue());
        }

        return attempt;
    }

    public List<Attempt> findAll() {
        String sql = "SELECT * FROM attempts ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, new AttemptRowMapper());
    }

    public Optional<Attempt> findById(Long id) {
        String sql = "SELECT * FROM attempts WHERE id = ?";
        List<Attempt> list = jdbcTemplate.query(sql, new AttemptRowMapper(), id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
