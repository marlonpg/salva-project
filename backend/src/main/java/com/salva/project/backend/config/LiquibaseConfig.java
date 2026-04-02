package com.salva.project.backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {

	@Bean
	SpringLiquibase springLiquibase(
		DataSource dataSource,
		@Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.yaml}") String changeLog
	) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog(changeLog);
		liquibase.setShouldRun(true);
		return liquibase;
	}
}
