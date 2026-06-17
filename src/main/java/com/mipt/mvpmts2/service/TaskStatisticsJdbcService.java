package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.dto.TaskStatisticsDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class TaskStatisticsJdbcService {

    private final JdbcTemplate jdbcTemplate;

    public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TaskStatisticsDto> getTasksCountByPriority() {
        String sql = "SELECT priority, COUNT(*) as task_count FROM tasks GROUP BY priority";

        RowMapper<TaskStatisticsDto> rowMapper = new RowMapper<>() {
            @Override
            public TaskStatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TaskStatisticsDto(
                        rs.getString("priority"),
                        rs.getLong("task_count")
                );
            }
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
}
