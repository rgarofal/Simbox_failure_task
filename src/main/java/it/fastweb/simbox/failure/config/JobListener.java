package it.fastweb.simbox.failure.config;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import it.fastweb.simbox.failure.client.SessionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobListener implements JobExecutionListener, StepExecutionListener{

    private final SessionClient sessionClient;
    //rg
    private URL resource;
    private Session session;
    private ChannelSftp channelSftp;
    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

    @Autowired
    public JobListener(SessionClient sessionClient) throws URISyntaxException {
        this.sessionClient = sessionClient;
    }

    public Session openSession() {

        try {
        	            
            String user = sessionClient.getUser();
            int port = sessionClient.getPort();
            String host = sessionClient.getHost();
            JSch jsch = new JSch();
            
            String path_identity = writeResourceToFile("rco-sftp.ppk");
            jsch.addIdentity(path_identity);
            session = jsch.getSession(user, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Sessione aperta: " + session.isConnected());
        return session;
    }

    public ChannelSftp openChannel() {

        try {
            Channel channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect();

            channelSftp = (ChannelSftp) channel;

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Channel aperto: " + channelSftp.isConnected());
        return channelSftp;
    }
    
    
    private String writeResourceToFile(String resourceName) throws URISyntaxException, IOException
    {
    	ClassLoader CLDR = getClass().getClassLoader();
        log.debug("Class loader = " + CLDR.toString());
        log.debug("Get class Name = " + getClass().getName());
    	log.debug("Resource name = " + resourceName );
        resource = CLDR.getResource(resourceName);
        log.debug("Resource URI  = " + resource.toURI());
        log.debug("Reading path without using URI = " + resource.getPath());
        log.debug("Reading path using URI = " + resource.toURI().getPath() );
        
        InputStream configStream = CLDR.getResourceAsStream(resourceName);
        Path pth =  Paths.get(resourceName);
        Files.deleteIfExists(pth);
        Files.copy(configStream, Files.createFile(pth), StandardCopyOption.REPLACE_EXISTING);
        return pth.toAbsolutePath().toString();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Nullable
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        return null;
    }
}
