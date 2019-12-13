package it.fastweb.simbox.failure.batch;

import it.fastweb.simbox.failure.model.SimboxTimestampIdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * eredita una lista di file dal reader e la inserisce a db
 **/
public class SimboxWriter implements ItemWriter<List<SimboxTimestampIdx>> {

    private JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(SimboxWriter.class);


    @Autowired
    public SimboxWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(List<? extends List<SimboxTimestampIdx>> list) throws Exception {
        if(!list.isEmpty()){
            insertDb(list.get(0));
        } else {
            log.info("Non ci sono file da inserire a dB");
        }
    }

    private int[] insertDb(List<SimboxTimestampIdx> simboxTimestampIdx) {
        return jdbcTemplate.batchUpdate("INSERT INTO simbox_timestamp_idx (date, folder, filename, dl) VALUES (?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setTimestamp(1, new java.sql.Timestamp(simboxTimestampIdx.get(i).getDate().getTime()));
                        ps.setString(2, simboxTimestampIdx.get(i).getFolder());
                        ps.setString(3, simboxTimestampIdx.get(i).getFilename());
                        ps.setString(4, simboxTimestampIdx.get(i).getDl());
                    }

                    @Override
                    public int getBatchSize() {
                        return simboxTimestampIdx.size();
                    }
                }
        );
    }
}
