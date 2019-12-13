package it.fastweb.simbox.failure.batch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import it.fastweb.simbox.failure.client.SessionClient;
import it.fastweb.simbox.failure.config.JobListener;
import it.fastweb.simbox.failure.config.SimboxHttp;
import it.fastweb.simbox.failure.config.SimboxRowMapper;
import it.fastweb.simbox.failure.model.SimboxTimestampIdx;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimboxReader implements ItemReader<List<SimboxTimestampIdx>> {

    private final SessionClient sessionClient;
    private Vector<ChannelSftp.LsEntry> fileList;
    private Date currentFileDate;
    private SimboxTimestampIdx s;
    private DataSource dataSource;
    private ChannelSftp channel;
    private Session session;
    private static final Logger log = LoggerFactory.getLogger(SimboxReader.class);

    @Autowired
    public SimboxReader(DataSource dataSource, SessionClient sessionClient) {
        this.dataSource = dataSource;
        this.sessionClient = sessionClient;
    }

    /**
     * si collega alla directory esterna e ne salva il contenuto in un @vector che scorre prendendo solo i file
     * recenti e salvandoli in una @list da passare al writer
     **/
    @Nullable
    @Override
    public List<SimboxTimestampIdx> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        JobListener jobListener = new JobListener(sessionClient);
        session = jobListener.openSession();
        channel = jobListener.openChannel();

        try {
            channel.cd(sessionClient.getChannelPath());
            fileList = channel.ls("*.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<SimboxTimestampIdx> newFileList = new ArrayList<>();

        Date maxDate = queryDate(dataSource); //query DB
        log.info("Ultimi dati inseriti a Db in data: " + maxDate);


        if (fileList.capacity() == 0) {
            log.info("************************* Cartella vuota, file non trovati");
            return null;
        } else {
            log.info("************************* TOTALE FILES NELLA CARTELLA : " + fileList.size());
            fileList.forEach(f -> {
                String dateFile = f.getAttrs().getMtimeString();

                try {
                    currentFileDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(dateFile);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                SimboxTimestampIdx s = new SimboxTimestampIdx();

                if (maxDate == null || maxDate.compareTo(currentFileDate) < 0) {
                    s.setDate(currentFileDate);
                    s.setFolder("failure_csv");
                    s.setFilename(f.getFilename());
                    s.setDl("CSB.SimShelf@fastweb.it");

                    newFileList.add(s);

                    readFile(f); //legge il file e lo invia al TMT
                }
            });
            log.info("************************* TOTALE FILE DA CARICARE : " + newFileList.size());
        }

        if (newFileList.size() == 0) {
            log.info("Non ci sono file da mandare al writer");
            session.disconnect();
            channel.exit();
            log.info("Sessione " + session.isConnected());
            log.info("Channel " + channel.isConnected());
            return null;
        } else {
            newFileList.forEach(f -> {
            });
            session.disconnect();
            channel.exit();
            log.info("Sessione " + session.isConnected());
            log.info("Channel " + channel.isConnected());
            return newFileList;
        }
    }

    private Date queryDate(final DataSource dataSource) {

        JdbcCursorItemReader itemReader = new JdbcCursorItemReader();
        itemReader.setDataSource(dataSource);
        itemReader.setSql("SELECT max(date) date FROM simbox_timestamp_idx s WHERE s.folder = 'failure_csv';");
        itemReader.setRowMapper(new SimboxRowMapper());
        ExecutionContext executionContext = new ExecutionContext();
        itemReader.open(executionContext);

        try {
            s = (SimboxTimestampIdx) itemReader.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        itemReader.close();

        return s.getDate();
    }

    private String readFile(ChannelSftp.LsEntry f) {

        File tmp = null;
        String splitName = f.getFilename();
        String csvFile = "";

        try {
            tmp = File.createTempFile(splitName, ".tmp");
//            String [] data = splitName.split("(?<=.csv)");

            InputStream in = null;
            OutputStream out = null;

            in = channel.get(sessionClient.getChannelPath() + "/" + f.getFilename());
            out = new FileOutputStream(tmp);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            Path p = Paths.get(tmp.getAbsolutePath());
            File file = new File(String.valueOf(p));

            csvFile = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            SimboxHttp simboxHttp = new SimboxHttp(sessionClient);
            simboxHttp.sendTicket(csvFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return csvFile;
    }
}
