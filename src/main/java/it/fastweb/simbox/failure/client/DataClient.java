package it.fastweb.simbox.failure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "simbox-batch")
public class DataClient {

    //configurazione db batch
    private String url_config;
    private String username_config;
    private String password_config;
    private String schema_config;

    //configurazione db tmt
    private String url_sales;
    private String username_sales;
    private String password_sales;
    private String schema_sales;


    public String getUrl_config() {
        return url_config;
    }

    public void setUrl_config(String url_config) {
        this.url_config = url_config;
    }

    public String getUsername_config() {
        return username_config;
    }

    public void setUsername_config(String username_config) {
        this.username_config = username_config;
    }

    public String getPassword_config() {
        return password_config;
    }

    public void setPassword_config(String password_config) {
        this.password_config = password_config;
    }

    public String getSchema_config() {
        return schema_config;
    }

    public void setSchema_config(String schema_config) {
        this.schema_config = schema_config;
    }

    public String getUrl_sales() {
        return url_sales;
    }

    public void setUrl_sales(String url_sales) {
        this.url_sales = url_sales;
    }

    public String getUsername_sales() {
        return username_sales;
    }

    public void setUsername_sales(String username_sales) {
        this.username_sales = username_sales;
    }

    public String getPassword_sales() {
        return password_sales;
    }

    public void setPassword_sales(String password_sales) {
        this.password_sales = password_sales;
    }

    public String getSchema_sales() {
        return schema_sales;
    }

    public void setSchema_sales(String schema_sales) {
        this.schema_sales = schema_sales;
    }
}
