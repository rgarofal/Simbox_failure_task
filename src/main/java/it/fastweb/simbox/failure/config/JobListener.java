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

import java.net.URL;

public class JobListener implements JobExecutionListener, StepExecutionListener{

    private final SessionClient sessionClient;
    private Session session;
    private ChannelSftp channelSftp;
    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

    @Autowired
    public JobListener(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    public Session openSession() {

        try {
            final URL resource = getClass().getClassLoader().getResource("rco-sftp.ppk");

            String user = sessionClient.getUser();
            int port = sessionClient.getPort();
            String host = sessionClient.getHost();
            JSch jsch = new JSch();

            jsch.addIdentity(resource.toURI().getPath());
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
