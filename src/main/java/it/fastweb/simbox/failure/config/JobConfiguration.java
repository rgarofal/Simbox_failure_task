package it.fastweb.simbox.failure.config;

import it.fastweb.simbox.failure.batch.SimboxReader;
import it.fastweb.simbox.failure.batch.SimboxWriter;
import it.fastweb.simbox.failure.client.SessionClient;
import it.fastweb.simbox.failure.model.SimboxTimestampIdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Import({DatabaseConfiguration.class, MBeanExporter.class})
public class JobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    @Qualifier("data_batch")
    private DataSource dataSource_config;
    @Autowired
    @Qualifier("data_sales")
    private DataSource dataSource_business;
    @Autowired
    private SimpleJobLauncher jobLauncher;
    @Autowired
    private SessionClient sessionClient;

    private static final Logger log = LoggerFactory.getLogger(JobConfiguration.class);
    private static final JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();


    @Bean(name = "jdbcTemplate")
    JdbcTemplate jdbcTemplate(@Qualifier("data_sales")DataSource dataSource_business) {
        return new JdbcTemplate(dataSource_business);
    }

    @Bean
    public ResourcelessTransactionManager transactionManager(){
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobRepository jobRepository(@Qualifier("data_batch")DataSource dataSource_config) throws Exception {
        jobRepositoryFactoryBean.setDataSource(dataSource_config);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager());
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    @Scheduled(cron = "* */15 * * * *")
    public void runJobScheduled() throws Exception {

        log.info("Job Started at :" + new Date());

        JobParameters param = new JobParametersBuilder().addString("simbox_failure", String.valueOf(System.currentTimeMillis())).toJobParameters();
        JobExecution execution = jobLauncher.run(simbox_failure(), param);

        log.info("Job finished with status :" + execution.getStatus());
    }

    @Bean
    public Job simbox_failure() {
        return jobBuilderFactory.get("simbox_failure")
                .incrementer(new RunIdIncrementer())
                .listener(new JobListener(sessionClient))
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<List<SimboxTimestampIdx>, List<SimboxTimestampIdx>>chunk(1)
                .reader(new SimboxReader(dataSource_business, sessionClient))
                .writer(new SimboxWriter(jdbcTemplate(dataSource_business)))
                .startLimit(1)
                .build();
    }
}
