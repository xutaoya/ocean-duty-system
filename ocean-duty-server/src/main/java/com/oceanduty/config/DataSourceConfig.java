package com.oceanduty.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;

/**
 * 数据源配置
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Bean
    public DataSource dataSource() {
        ensureDataDirExists();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName("org.sqlite.JDBC");
        return dataSource;
    }

    /**
     * 确保 SQLite 数据库目录存在
     */
    private void ensureDataDirExists() {
        String path = jdbcUrl.replace("jdbc:sqlite:", "");
        File dbFile = Paths.get(path).toFile();
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
