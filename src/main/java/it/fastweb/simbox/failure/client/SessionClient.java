package it.fastweb.simbox.failure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "simbox-batch")
public class SessionClient {

    //path channelSftp
    private String channelPath;

    //configurazione session
    private String user;
    private int port;
    private String host;

    //configurazione http
    private String post_url;


    public String getChannelPath() {
        return channelPath;
    }

    public void setChannelPath(String channelPath) {
        this.channelPath = channelPath;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPost_url() {
        return post_url;
    }

    public void setPost_url(String post_url) {
        this.post_url = post_url;
    }
}
