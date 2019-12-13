package it.fastweb.simbox.failure.config;

import it.fastweb.simbox.failure.model.SimboxTimestampIdx;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimboxRowMapper implements RowMapper<SimboxTimestampIdx> {

    private static final String DATE = "date";


    public SimboxTimestampIdx mapRow(ResultSet rs, int rowNum) throws SQLException {

        SimboxTimestampIdx simbox = new SimboxTimestampIdx();

          simbox.setDate(rs.getTimestamp(DATE));

        return simbox;
    }
}

