package com.clinic.queue.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configures datasource with H2 in-memory fallback when MySQL is unavailable.
 * If DB_URL is set to MySQL but connection fails, falls back to H2.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        try {
            // Try to create datasource with configured properties (MySQL or H2)
            return properties.initializeDataSourceBuilder().build();
        } catch (Exception e) {
            // If MySQL connection fails, fall back to H2 in-memory database
            String url = properties.getUrl();
            if (url != null && url.contains("mysql")) {
                System.out.println("\n========================================");
                System.out.println("MySQL connection failed: " + e.getMessage());
                System.out.println("Falling back to H2 in-memory database...");
                System.out.println("========================================\n");
                
                // Reconfigure for H2
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:h2:mem:mediline;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
                config.setDriverClassName("org.h2.Driver");
                config.setUsername("sa");
                config.setPassword("");
                return new HikariDataSource(config);
            }
            throw new RuntimeException("Failed to initialize datasource", e);
        }
    }
}
