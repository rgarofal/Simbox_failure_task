package it.fastweb.simbox.failure.config;

import it.fastweb.simbox.failure.client.DataClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

//@Configuration
public class DatabaseConfiguration {

    private DataClient dataClient;

    @Autowired
    public DatabaseConfiguration(DataClient dataClient) {

        this.dataClient = dataClient;
    }

    @Bean(name= "data_batch")
    @Primary
    public DataSource batchDataSource() throws SQLException {

        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriver(new com.mysql.cj.jdbc.Driver());
        dataSource.setUrl(dataClient.getUrl_config());
        dataSource.setUsername(dataClient.getUsername_config());
        dataSource.setPassword(dataClient.getPassword_config());
        dataSource.setSchema(dataClient.getSchema_config());
        return dataSource;
    }

    @Bean(name= "data_sales")
    public DataSource salesDataSource() throws SQLException {

        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriver(new com.mysql.cj.jdbc.Driver());
        dataSource.setUrl(dataClient.getUrl_sales());
        dataSource.setUsername(dataClient.getUsername_sales());
        dataSource.setPassword(dataClient.getPassword_sales());
        dataSource.setSchema(dataClient.getSchema_sales());
        return dataSource;
    }
}
