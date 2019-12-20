package it.fastweb.simbox.failure.config;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.export.MBeanExporter;

@Configuration
@Import({ DatabaseConfiguration.class, MBeanExporter.class  })
public class TaskConfiguration extends DefaultTaskConfigurer{
	public TaskConfiguration(@Qualifier("data_batch") DataSource dataSource) 
    {
        super(dataSource);
    }
}
